import { createService } from "./service/connectionService";
import { createTerminal } from "./infra/terminal";
import { createSessionHandler } from "./handler/sessionHandler";

const service = createService();
const terminal = createTerminal("terminal");
const handler = createSessionHandler(service, terminal);

export const start = () => handler.start();
export const dispose = () => handler.dispose();
