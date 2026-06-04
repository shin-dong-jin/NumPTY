import winston from "winston";
import DailyRotateFile from "winston-daily-rotate-file";

const COLORS = {
  ERROR: "\x1b[1;31m",
  WARN: "\x1b[31m",
  INFO: "\x1b[34m",
  RESET: "\x1b[0m",
  DEBUG: "\x1b[0m",
  CONTEXT: "\x1b[36m",
};

const { combine, timestamp, printf } = winston.format;

const consoleFormat = printf(
  ({ timestamp, process, level, context, message, ...meta }) => {
    const lvl = level.toUpperCase();
    const levelColor = COLORS[lvl] ?? "";
    const ctx = context ?? "app.Unknown";
    const extra = Object.keys(meta).length ? `${JSON.stringify(meta)}` : "";

    return `${timestamp} [${process}] ${levelColor}${lvl}${COLORS.RESET} ${COLORS.CONTEXT}${ctx}${COLORS.RESET} - ${message}${extra}`;
  },
);

const fileFormat = printf(({ timestamp, level, context, message, ...meta }) => {
  const lvl = level.toUpperCase();
  const ctx = context ?? "app.Unknown";
  const extra = Object.keys(meta).length ? ` ${JSON.stringify(meta)}` : "";
  return `${timestamp} [main] ${lvl} ${ctx} - ${message}${extra}`;
});

const transports = [
  new winston.transports.Console({
    format: consoleFormat,
  }),
];

if (process.env.NODE_ENV === "production") {
  transports.push(
    new DailyRotateFile({
      filename: "/var/log/app/app-%DATE%.log",
      datePattern: "YYYY-MM-DD",
      maxSize: "100m",
      maxFiles: "30d",
      format: fileFormat,
    }),
    new DailyRotateFile({
      filename: "/var/log/app/error-%DATE%.log",
      datePattern: "YYYY-MM-DD",
      maxSize: "50m",
      maxFiles: "30d",
      level: "error",
      format: fileFormat,
    }),
  );
}

const logger = winston.createLogger({
  level: process.env.LOG_LEVEL ?? "info",
  format: combine(timestamp({ format: "YYYY-MM-DDTHH:mm:ss.SSS" })),
  transports,
});

export const getLogger = (process, context) => {
  if (context.startsWith("file://")) {
    const filePath = new URL(context).pathname;
    const srcIndex = filePath.indexOf("/src/");
    const relative =
      srcIndex !== -1
        ? filePath.slice(srcIndex + 5)
        : filePath.split("/").slice(-2).join("/");

    context = relative.replace(/\.js$/, "").replaceAll("/", ".");
  }

  return logger.child({ process, context });
};

export default logger;
