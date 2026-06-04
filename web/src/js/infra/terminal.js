import { Terminal } from "@xterm/xterm";
import { FitAddon } from "@xterm/addon-fit";
import { WebLinksAddon } from "@xterm/addon-web-links";
import { SearchAddon } from "@xterm/addon-search";
import { Unicode11Addon } from "@xterm/addon-unicode11";
import { COLOR_THEMES } from "./themes";

const DEFAULT_OPTIONS = {
  allowTransparency: true,
  fontFamily: "D2Coding, 'Nanum Gothic Coding', monospace",
  fontSize: 20,
  fontWeight: 400,
  fontWeightBold: 700,
  lineHeight: 1.0,
  letterSpacing: 0,
  allowProposedApi: true,
  convertEol: true,
  cursorStyle: "block",
  cursorInactiveStyle: "block",
  cursorBlink: true,
  theme: COLOR_THEMES.retro,
};

const copyToClipboard = (text) => {
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text);
    return;
  }

  const textarea = document.createElement("textarea");
  textarea.value = text;
  textarea.style.cssText = "position:fixed;opacity:0";
  document.body.appendChild(textarea);
  textarea.select();
  document.execCommand("copy");
  document.body.removeChild(textarea);
};

export const createTerminal = (containerId, options = {}) => {
  const term = new Terminal({ ...DEFAULT_OPTIONS, ...options });

  const addons = {
    fit: new FitAddon(),
    webLinks: new WebLinksAddon(),
    search: new SearchAddon(),
    unicode11: new Unicode11Addon(),
  };

  Object.values(addons).forEach((addon) => term.loadAddon(addon));
  term.unicode.activeVersion = "11";

  const container = document.getElementById(containerId);
  if (!container) {
    throw new Error(`Container #${containerId} not found.`);
  }
  term.open(container);

  term.onSelectionChange(() => {
    if (!term.hasSelection()) {
      return;
    }

    copyToClipboard(term.getSelection());
  });

  let viewportMarker = null;

  const updateViewportMarker = () => {
    viewportMarker?.dispose();

    const cursorY = term.buffer.active.cursorY;
    const baseY = term.buffer.active.baseY;
    const viewportY = term.buffer.active.viewportY;
    const viewportBottom = viewportY + term.rows - 1;

    viewportMarker = term.registerMarker(viewportBottom - (baseY + cursorY));
  };

  const fit = () => addons.fit.fit();

  const proposeDimensions = () => addons.fit.proposeDimensions();

  /**
   * xterm.js viewport scroll workaround
   *
   * dynamic flex 레이아웃에서 resize 시 두 가지 문제가 있음:
   *
   * 1) viewport 스크롤 영역 깨짐
   *    - _sync()로 스크롤 영역 크기 재계산
   *    - scrollToLine(ydisp)로 DOM scrollTop 강제 동기화
   *    => 6.0.0에서도 해결 안 됨. 공식 API 없음, private 접근 불가피.
   *
   * 2) reflow 후 스크롤 위치 이탈
   *    - registerMarker로 뷰포트 하단을 추적
   *    - reflow 후 marker.line이 자동 갱신되므로 역산하여 scrollToLine
   *    => 공식 API로 해결
   *
   * 실행 순서가 중요함: _sync → scrollToLine(ydisp) → 마커 기반 스크롤
   * DOM이 바로잡힌 후에야 공식 API 스크롤이 정상 동작함.
   *
   * 관련: https://github.com/xtermjs/xterm.js/issues/3584
   * xterm 버전 업그레이드 시 반드시 재검증 필요.
   */
  term.onResize(() => {
    requestAnimationFrame(() => {
      term._core._viewport?._sync();
      term._core._viewport?.scrollToLine(
        term._core._bufferService.buffer.ydisp,
      );

      if (
        viewportMarker &&
        !viewportMarker.isDisposed &&
        viewportMarker.line >= 0
      ) {
        const viewportTop = Math.max(0, viewportMarker.line - term.rows + 1);
        term.scrollToLine(viewportTop);
      } else {
        term.scrollToBottom();
      }
    });
  });

  const applyTheme = (theme) => {
    document.documentElement.dataset.theme = theme;
    term.options.theme = COLOR_THEMES[theme];
  };

  return {
    term,
    container,
    fit,
    proposeDimensions,
    updateViewportMarker,
    applyTheme: (theme) => applyTheme(theme),
    dispose: () => {
      viewportMarker?.dispose();
      viewportMarker = null;
      term.dispose();
    },
  };
};
