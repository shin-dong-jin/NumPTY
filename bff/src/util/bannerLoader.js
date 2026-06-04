import { readFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { dirname, resolve } from "path";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

export const printBanner = async (filename, defaultLogo) => {
  try {
    const bannerPath = resolve(__dirname, `../../banner/${filename}`);

    let banner = await readFile(bannerPath, "utf-8");

    banner = banner.replace(/\r\n/g, "\n").replace(/\n/g, "\r\n");

    console.clear();
    console.log(banner);
  } catch {
    console.clear();
    console.log(defaultLogo.join("\r\n") + "\r\n");
  }
};
