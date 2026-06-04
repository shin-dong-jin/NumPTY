module.exports = {
  apps: [
    {
      name: "numpty-bff-app",
      script: "./app.js",
      cwd: "/opt/numpty/bff",
      instances: 1,
      autorestart: true,
    },
    {
      name: "numpty-bff-daemon",
      script: "./daemon.js",
      cwd: "/opt/numpty/bff",
      instances: 1,
      autorestart: true,
    },
  ],
};
