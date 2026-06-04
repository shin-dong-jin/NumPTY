const FONT_CONFIG = [
  {
    name: "D2Coding",
    url: "/fonts/D2Coding.woff2",
    format: "woff2",
    weight: "normal",
    style: "normal",
  },
  {
    name: "D2Coding",
    url: "/fonts/D2Coding-Bold.woff2",
    format: "woff2",
    weight: "bold",
    style: "normal",
  },
];

export const loadAllFonts = async () => {
  const loadedFaces = FONT_CONFIG.map((font) => {
    const face = new FontFace(
      font.name,
      `url(${font.url}) format(${font.format})`,
      {
        weight: font.weight || "normal",
        style: font.style || "normal",
      },
    );

    return face.load().then((loadedFace) => {
      document.fonts.add(loadedFace);
      return loadedFace;
    });
  });

  await Promise.all([
    ...loadedFaces,
    document.fonts.load("16px 'Nanum Gothic Coding'"),
  ]);

  await document.fonts.ready;
};
