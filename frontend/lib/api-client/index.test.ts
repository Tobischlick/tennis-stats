import { beforeEach, describe, expect, test, vi } from "vitest";
import type { PlayerStats, PlayerSummary } from "./types";

// openapi-fetch captures globalThis.fetch when the client is created at import time.
const fetchMock = vi.hoisted(() => {
  const mock = vi.fn<typeof fetch>();
  vi.stubGlobal("fetch", mock);
  return mock;
});

import { getHeadToHead, getPlayerStats, getRecentMatches, searchPlayers } from "./index";

function respond(status: number, body?: unknown) {
  fetchMock.mockResolvedValueOnce(
    new Response(body === undefined ? null : JSON.stringify(body), {
      status,
      headers: { "Content-Type": "application/json" },
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
  ["getHeadToHead", () => getHeadToHead("p1", "p2")],
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
  };
  respond(200, stats);
  await expect(getPlayerStats("p1")).resolves.toEqual(stats);
});
