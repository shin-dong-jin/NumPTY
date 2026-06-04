export default {
  server: {
    proxy: {
      // WebSocket 프록시 설정
      "/ws": {
        target: "ws://localhost:3000",
        ws: true, // WebSocket 활성화
      },
    },
    host: true, // 외부 접속 허용
    port: 5173, // 프론트엔드 포트
  },
};
