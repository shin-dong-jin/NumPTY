import { fileURLToPath } from "url";
import { dirname, resolve } from "path";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const osRoot = resolve(__dirname, "../os");
const customBinPath = resolve(__dirname, "../bin");
const zshPath = resolve(__dirname, "../zsh");

export const appProperties = {
  server: { port: 3000 },
  pty: {
    file: "/bin/zsh",
    args: ["-d", "-i", "-r"],
    options: {
      name: "xterm-256color",
      cols: 80,
      rows: 24,
      cwd: `${osRoot}/home/user/workspace`,
      env: {
        ...process.env,
        PATH: `${osRoot}/bin:${osRoot}/home/user/.local/bin`,
        SHELL: "/bin/zsh",
        ZDOTDIR: `${osRoot}/home/user`,
        STARSHIP_CONFIG: `${osRoot}/home/user/.config/starship.toml`,
        HOME: `${osRoot}/home/user`,
      },
    },
  },
};
