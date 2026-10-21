import "server-only";
import { cache } from "react";
import createClient from "openapi-fetch";
import type { paths } from "@/lib/api/schema";
import type { HeadToHead, Overview, PlayerStats, PlayerSummary, RecentMatch } from "./types";

export type * from "./types";

const baseUrl = process.env.TENNIS_STATS_API_BASE_URL ?? "http://localhost:8080/api/v1";

// Every page is rendered per request anyway, and a cached response would hide the daily crawl's new results.
const client = createClient<paths>({ baseUrl, cache: "no-store" });

type Result<T> = { data?: T; response: Response };

async function send<T>(operation: string, request: () => Promise<Result<T>>): Promise<Result<T>> {
  try {
    return await request();
  } catch (cause) {
    throw new Error(`${operation}: backend not reachable at ${baseUrl}`, { cause });
  }
}

function orNotFound<T>(operation: string, { data, response }: Result<T>): T | undefined {
  if (response.status === 404) return undefined;
  if (!response.ok || data === undefined) throw new Error(`${operation}: backend answered HTTP ${response.status}`);
  return data;
}

export async function searchPlayers(query: string): Promise<PlayerSummary[]> {
  const q = query.trim();
  if (!q) return [];
  const { data, response } = await send("searchPlayers", () =>
    client.GET("/players", { params: { query: { query: q } } }),
  );
  if (response.status === 400) return [];
  if (!response.ok || data === undefined) throw new Error(`searchPlayers: backend answered HTTP ${response.status}`);
  return data;
}

// cache() because generateMetadata and the page both ask for the same stats in one request.
/** `undefined` when there is no player with this id. */
export const getPlayerStats = cache(async (id: string): Promise<PlayerStats | undefined> => {
  const result = await send("getPlayerStats", () =>
    client.GET("/players/{id}/stats", { params: { path: { id } } }),
  );
  return orNotFound("getPlayerStats", result);
});

/** Newest first; `undefined` when there is no player with this id. */
export async function getRecentMatches(id: string): Promise<RecentMatch[] | undefined> {
  const result = await send("getRecentMatches", () =>
    client.GET("/players/{id}/matches", { params: { path: { id } } }),
  );
  return orNotFound("getRecentMatches", result);
}

/** One page of matches, newest first, plus the total across all pages; `undefined` when there is no player with this id. */
export async function getMatchesPage(
  id: string,
  offset: number,
  limit: number,
): Promise<{ matches: RecentMatch[]; total: number } | undefined> {
  const result = await send("getMatchesPage", () =>
    client.GET("/players/{id}/matches", { params: { path: { id }, query: { offset, limit } } }),
  );
  const matches = orNotFound("getMatchesPage", result);
  if (!matches) return undefined;
  const total = Number(result.response.headers.get("X-Total-Count") ?? NaN);
  if (!Number.isInteger(total) || total < 0) throw new Error("getMatchesPage: backend sent no X-Total-Count header");
  return { matches, total };
}

/** `undefined` when either player does not exist. */
export async function getHeadToHead(id: string, opponentId: string): Promise<HeadToHead | undefined> {
  const result = await send("getHeadToHead", () =>
    client.GET("/players/{id}/head-to-head/{opponentId}", { params: { path: { id, opponentId } } }),
  );
  return orNotFound("getHeadToHead", result);
}

export async function getOverview(): Promise<Overview> {
  const { data, response } = await send("getOverview", () => client.GET("/overview"));
  if (!response.ok || data === undefined) throw new Error(`getOverview: backend answered HTTP ${response.status}`);
  return data;
}
