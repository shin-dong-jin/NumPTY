import http from "http";

export const createServer = () => {
  const server = http.createServer((req, res) => {
    if (req.url === "/health-check" && req.method === "GET") {
      res.writeHead(200, { "Content-Type": "text/plain" });
      res.end("OK");
      return;
    }

    res.writeHead(404);
    res.end();
  });

  return server;
};
