import { headToHeadFor, type HeadToHead } from "./head-to-head";
import { recentMatchesFor, type RecentMatch, type SetScore } from "./matches";
import { players, type PlayerSummary } from "./players";
import { stats, type MatchResult, type PlayerStats } from "./stats";

export type { HeadToHead, MatchResult, PlayerStats, PlayerSummary, RecentMatch, SetScore };

/** Mirrors `searchPlayers` (GET /players?query=). */
export function searchPlayers(query: string): PlayerSummary[] {
  const q = query.trim().toLowerCase();
  if (!q) return [];
  return players.filter((p) => p.name.toLowerCase().includes(q));
}

/** Mirrors `getPlayerStats` (GET /players/{id}/stats); undefined stands in for a 404. */
export function getPlayerStats(id: string): PlayerStats | undefined {
  return stats[id];
}

/** Mirrors `getHeadToHead` (GET /players/{id}/head-to-head/{opponentId}); undefined stands in for a 404. */
export function getHeadToHead(id: string, opponentId: string): HeadToHead | undefined {
  if (!getPlayer(id) || !getPlayer(opponentId) || id === opponentId) return undefined;
  return headToHeadFor(id, opponentId);
}

/**
 * Not in the API: PlayerStats carries no name/club, so the stats page needs this
 * extra lookup. Contract gap to raise when the prototype is reviewed.
 */
export function getPlayer(id: string): PlayerSummary | undefined {
  return players.find((p) => p.id === id);
}

/**
 * Not in the API: a player's recent matches, newest first, with opponents and set scores.
 * Contract gap to raise when the prototype is reviewed.
 */
export function getRecentMatches(id: string): RecentMatch[] {
  return recentMatchesFor(id);
}

/** All players, e.g. for "compare with…" suggestions. */
export function allPlayers(): PlayerSummary[] {
  return players;
}
