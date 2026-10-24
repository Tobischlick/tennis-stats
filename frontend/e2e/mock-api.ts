import { createServer, type IncomingMessage, type ServerResponse } from "node:http";
import { doublesPartners, headToHead, matches, overview, players, seasons, stats, UNREACHABLE_PLAYER_ID } from "./fixtures";

export const MOCK_API_PORT = 3101;

function json(res: ServerResponse, status: number, body: unknown, headers: Record<string, string> = {}) {
  res.writeHead(status, { "Content-Type": "application/json", ...headers }).end(JSON.stringify(body));
}

function handle(req: IncomingMessage, res: ServerResponse) {
  const url = new URL(req.url ?? "/", "http://localhost");
  const path = url.pathname.replace(/^\/api\/v1/, "");
  const [, resource, id, sub, opponentId] = path.split("/");

  if (path === "/overview") return json(res, 200, overview);

  if (path === "/players") {
    const query = url.searchParams.get("query")?.toLowerCase() ?? "";
    return json(res, 200, players.filter((p) => p.name.toLowerCase().includes(query)));
  }

  if (resource === "players" && id === UNREACHABLE_PLAYER_ID) return req.socket.destroy();

  if (resource === "players" && stats[id]) {
    if (sub === "stats") return json(res, 200, stats[id]);
    if (sub === "matches") {
      const all = matches[id] ?? [];
      const offset = Number(url.searchParams.get("offset") ?? 0);
      const limit = Number(url.searchParams.get("limit") ?? 10);
      return json(res, 200, all.slice(offset, offset + limit), { "X-Total-Count": String(all.length) });
    }
    if (sub === "seasons") return json(res, 200, seasons[id] ?? []);
    if (sub === "doubles-partners") {
      const { playerId, player } = stats[id];
      const none = { playerId, player, matchesPlayed: 0, hasEnoughData: false, partners: [] };
      return json(res, 200, doublesPartners[id] ?? none);
    }
    const h2h = sub === "head-to-head" && headToHead[`${id}/${opponentId}`];
    if (h2h) return json(res, 200, h2h);
  }

  json(res, 404, { message: `No mock for ${path}` });
}

export default async function startMockApi() {
  const server = createServer(handle);
  await new Promise<void>((resolve) => server.listen(MOCK_API_PORT, "127.0.0.1", resolve));
  return () => new Promise<void>((resolve) => server.close(() => resolve()));
}
