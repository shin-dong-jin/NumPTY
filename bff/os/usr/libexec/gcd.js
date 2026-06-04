#!/usr/bin/env node
import net from "net";
import { ulid } from "ulid";

const targetA = process.argv[2];
const targetB = process.argv[3];

if (
  !targetA ||
  !/^-?\d+$/.test(targetA) ||
  targetA === "0" ||
  !targetB ||
  !/^-?\d+$/.test(targetB) ||
  targetB === "0"
) {
  console.error("\x1b[1;31mUsage: gcd <integer> <integer>\x1b[0m");
  process.exit(1);
}

const formattedTargetA = BigInt(targetA).toLocaleString();
const formattedTargetB = BigInt(targetB).toLocaleString();

const API_BASE = process.env.API_BASE || "http://localhost:8080";
const method = "POST";
const headers = { "Content-Type": "application/json" };
const token = process.env.NUMPTY_TOKEN;
const SOCKET_PATH = "/tmp/numpty.sock";

let spinnerInterval;
let currentState = "PENDING";

const TYPE_LABELS = {
  GCD: "Greatest Common Divisor",
  LCM: "Least Common Multiple",
  EXTENDED_GCD: "Greatest Common Divisor (Extended)",
  PRIMALITY_TEST: "Primality Test",
  FACTORIZE: "Prime Factorization",
};

const ALGORITHM_LABELS = {
  EUCLIDEAN: "Euclidean",
  EXTENDED_EUCLIDEAN: "Extended Euclidean",
  SIEVE: "Sieve of Eratosthenes",
  MILLER_RABIN: "Miller-Rabin",
  TRIAL: "Trial division",
  POLLARD_RHO: "Pollard's rho",
  ECM: "Elliptic-curve factorization method",
  UNKNOWN_ALGORITHM: "Unknown",
};

const PENDING_CHARS = ["⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"];
const WORKING_CHARS = ["/", "-", "\\", "|"];
const DOTS = ["   ", "   ", ".  ", ".  ", ".. ", ".. ", "...", "..."];

const startSpinner = () => {
  process.stdout.write("\x1b[?25l");
  let idx = 0;

  spinnerInterval = setInterval(() => {
    const dots = DOTS[idx % DOTS.length];

    if (currentState === "PENDING") {
      const char = PENDING_CHARS[idx % PENDING_CHARS.length];

      process.stdout.write(`\r\x1b[1;33m[ ${char} ] PENDING${dots} \x1b[0m`);
    } else if (currentState === "WORKING") {
      const char = WORKING_CHARS[idx % WORKING_CHARS.length];

      process.stdout.write(
        `\r\x1b[1;36m[ ${char} ] CALCULATING GCD(${formattedTargetA}, ${formattedTargetB})${dots} \x1b[0m`,
      );
    }
    idx++;
  }, 100);
};

const transitionTo = (nextState) => {
  clearInterval(spinnerInterval);
  process.stdout.write("\r\x1b[K");

  if (currentState === "PENDING" && nextState === "WORKING") {
    process.stdout.write("\x1b[1;32m[ ✓ ] PENDED\x1b[0m\r\n");
    currentState = "WORKING";
    startSpinner();
  }
};

const finalize = (taskData) => {
  clearInterval(spinnerInterval);
  process.stdout.write("\r\x1b[K");

  if (taskData.status === "COMPLETED") {
    const formattedResult =
      "GCD(" +
      formattedTargetA +
      ", " +
      formattedTargetB +
      ") = " +
      BigInt(taskData.result).toLocaleString();

    const formattedType = TYPE_LABELS[taskData.type];

    const formattedAlgorithms = taskData.algorithms
      .split(",")
      .map((algorithm) => ALGORITHM_LABELS[algorithm] ?? algorithm)
      .join(", ");

    const formattedElapsed = taskData.elapsedMs + "ms";

    process.stdout.write("\x1b[1;32m[ ✓ ] COMPLETED\x1b[0m\r\n\r\n");
    process.stdout.write("  Calculation summary\r\n");
    process.stdout.write(
      `\x1b[0;1m    Result     ${formattedResult}\x1b[0m\r\n`,
    );
    process.stdout.write(`    Type       ${formattedType}\r\n`);
    process.stdout.write(`    Algorithm  ${formattedAlgorithms}\r\n`);
    process.stdout.write(`    Elapsed    ${formattedElapsed}\r\n\r\n`);
  } else {
    process.stdout.write("\x1b[1;31m[ ✗ ] FAILED\x1b[0m\r\n");
    process.stdout.write(
      `\x1b[1;31m[ ERROR ] ${taskData.message || "Unknown"}\x1b[0m\r\n\r\n`,
    );
  }

  process.stdout.write("\x1b[?25h");
};

const execute = async () => {
  let socket;

  try {
    const taskId = ulid();
    let buffer = "";
    let finished = false;

    socket = await new Promise((resolve, reject) => {
      const s = net.createConnection(SOCKET_PATH, () => {
        resolve(s);
      });

      s.on("error", (err) => {
        reject(
          new Error(
            "Daemon connection refused. Please check redis stream daemon.",
          ),
        );
      });
    });

    startSpinner();

    socket.write(taskId);
    const taskPromise = new Promise((resolve, reject) => {
      socket.on("data", (data) => {
        buffer += data.toString();
        const parts = buffer.split("\n");
        buffer = parts.pop();

        for (const part of parts) {
          if (!part.trim()) {
            continue;
          }

          let taskData;
          try {
            taskData = JSON.parse(part);
          } catch {
            reject(new Error("Malformed response from daemon"));
            return;
          }

          if (taskData.status === "PENDING") {
            continue;
          }

          if (taskData.status === "WORKING") {
            if (currentState === "PENDING") {
              transitionTo("WORKING");
            }

            continue;
          }

          if (finished) {
            continue;
          }

          finished = true;

          if (currentState === "PENDING") {
            transitionTo("WORKING");
          }

          resolve(taskData);
        }
      });

      socket.on("error", (err) => reject(err));
    });

    const res = await fetch(`${API_BASE}/api/tasks/gcd`, {
      method: method,
      headers: { ...headers, Authorization: `Bearer ${token}` },
      body: JSON.stringify({
        _id: taskId,
        targetA: targetA,
        targetB: targetB,
      }),
    });

    if (!res.ok) {
      throw new Error(`WAS refused request (${res.status}).`);
    }

    const taskData = await taskPromise;
    finalize(taskData);
    socket.destroy();
    process.exit(taskData.status === "COMPLETED" ? 0 : 1);
  } catch (err) {
    clearInterval(spinnerInterval);
    socket?.destroy();
    process.stdout.write("\x1b[?25h");
    console.error(`\r\x1b[1;31m[ ERROR ] ${err.message}\x1b[0m\r\n`);
    process.exit(1);
  }
};

execute();
