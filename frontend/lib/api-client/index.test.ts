import { beforeEach, describe, expect, test, vi } from "vitest";
import type { Overview, PlayerSeason, PlayerStats, PlayerSummary } from "./types";

// openapi-fetch captures globalThis.fetch when the client is created at import time.
const fetchMock = vi.hoisted(() => {
  const mock = vi.fn<typeof fetch>();
  vi.stubGlobal("fetch", mock);
  return mock;
});

import {
  getHeadToHead,
  getMatchesPage,
  getOverview,
  getPlayerSeasons,
  getPlayerStats,
  getRecentMatches,
  searchPlayers,
} from "./index";

function respond(status: number, body?: unknown, headers: Record<string, string> = {}) {
  fetchMock.mockResolvedValueOnce(
    new Response(body === undefined ? null : JSON.stringify(body), {
      status,
      headers: { "Content-Type": "application/json", ...headers },
    }),
  );
}

const anna: PlayerSummary = { id: "p1", name: "Anna Schmidt", club: "TC Blau-Weiss Karlsruhe" };

beforeEach(() => {
  fetchMock.mockReset();
});

describe("searchPlayers", () => {
  test("returns the players on 200", async () => {
    respond(200, [anna]);
    await expect(searchPlayers("Anna")).resolves.toEqual([anna]);
  });

  test("returns an empty list on 400", async () => {
    respond(400, { title: "Bad Request" });
    await expect(searchPlayers("A")).resolves.toEqual([]);
  });

  test("does not call the backend for a blank query", async () => {
    await expect(searchPlayers("   ")).resolves.toEqual([]);
    expect(fetchMock).not.toHaveBeenCalled();
  });

  test("throws on other non-2xx responses", async () => {
    respond(500);
    await expect(searchPlayers("Anna")).rejects.toThrow("HTTP 500");
  });

  test("throws when the backend is unreachable", async () => {
    fetchMock.mockRejectedValueOnce(new TypeError("fetch failed"));
    await expect(searchPlayers("Anna")).rejects.toThrow("backend not reachable");
  });
});

describe.each([
  ["getPlayerStats", () => getPlayerStats("p1")],
  ["getRecentMatches", () => getRecentMatches("p1")],
  ["getMatchesPage", () => getMatchesPage("p1", 0, 50)],
  ["getHeadToHead", () => getHeadToHead("p1", "p2")],
  ["getPlayerSeasons", () => getPlayerSeasons("p1")],
])("%s", (_, call) => {
  test("returns undefined on 404", async () => {
    respond(404, { title: "Not Found" });
    await expect(call()).resolves.toBeUndefined();
  });

  test.each([400, 500, 503])("throws on HTTP %s", async (status) => {
    respond(status);
    await expect(call()).rejects.toThrow(`HTTP ${status}`);
  });

  test("throws when the backend is unreachable", async () => {
    fetchMock.mockRejectedValueOnce(new TypeError("fetch failed"));
    await expect(call()).rejects.toThrow("backend not reachable");
  });
});

test("getPlayerStats returns the body on 200", async () => {
  const stats: PlayerStats = {
    playerId: "p1",
    player: anna,
    matchesPlayed: 0,
    hasEnoughData: false,
    record: null,
    tieBreak: null,
    streak: null,
    decidingSet: null,
    firstSet: null,
  };
  respond(200, stats);
  await expect(getPlayerStats("p1")).resolves.toEqual(stats);
});

test("getPlayerSeasons returns the seasons on 200", async () => {
  const seasons: PlayerSeason[] = [
    {
      season: "Winter 2025/26",
      type: "WINTER",
      startsOn: "2025-10-01",
      endsOn: "2026-03-31",
      matchesPlayed: 2,
      record: { wins: 1, losses: 1, winPercentage: 50 },
      tieBreak: { tieBreaksPlayed: 0, tieBreaksWon: 0, tieBreakWinPercentage: 0 },
    },
  ];
  respond(200, seasons);
  await expect(getPlayerSeasons("p1")).resolves.toEqual(seasons);
  expect(new URL((fetchMock.mock.calls[0][0] as Request).url).pathname).toMatch(/\/players\/p1\/seasons$/);
});

describe("getMatchesPage", () => {
  test("returns the matches and the total from X-Total-Count", async () => {
    respond(200, [], { "X-Total-Count": "73" });
    await expect(getMatchesPage("p1", 50, 50)).resolves.toEqual({ matches: [], total: 73 });
  });

  test("sends offset and limit as query parameters", async () => {
    respond(200, [], { "X-Total-Count": "0" });
    await getMatchesPage("p1", 50, 25);
    const url = new URL((fetchMock.mock.calls[0][0] as Request).url);
    expect(url.pathname).toMatch(/\/players\/p1\/matches$/);
    expect(url.searchParams.get("offset")).toBe("50");
    expect(url.searchParams.get("limit")).toBe("25");
  });

  test("throws when the total header is missing", async () => {
    respond(200, []);
    await expect(getMatchesPage("p1", 0, 50)).rejects.toThrow("X-Total-Count");
  });
});

describe("getOverview", () => {
  test("returns the body on 200", async () => {
    const overview: Overview = {
      hasData: false,
      totals: {
        players: 0,
        clubs: 0,
        teams: 0,
        meetings: 0,
        matches: { total: 0, singles: 0, doubles: 0 },
        sets: 0,
        firstPlayedOn: null,
        lastPlayedOn: null,
      },
      funStats: {
        tieBreaks: 0,
        matchTieBreaks: 0,
        completedMatches: 0,
        decidingSetMatches: 0,
        decidingSetPercentage: null,
        mostCommonScoreLine: null,
        longestWinStreak: 0,
      },
    };
    respond(200, overview);
    await expect(getOverview()).resolves.toEqual(overview);
  });

  test("throws on non-2xx responses", async () => {
    respond(500);
    await expect(getOverview()).rejects.toThrow("HTTP 500");
  });

  test("throws when the backend is unreachable", async () => {
    fetchMock.mockRejectedValueOnce(new TypeError("fetch failed"));
    await expect(getOverview()).rejects.toThrow("backend not reachable");
  });
});
