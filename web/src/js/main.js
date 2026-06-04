import { loadAllFonts } from "./util/fontLoader";
import { start } from "./context";

const bootstrap = async () => {
  await loadAllFonts().catch((err) => {
    console.warn("Failed to load font files, using 'monospace':", err);
  });

  start();
};

bootstrap();
