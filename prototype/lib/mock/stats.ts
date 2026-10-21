import type { components } from "@/lib/api/schema";

export type PlayerStats = components["schemas"]["PlayerStats"];
export type MatchResult = components["schemas"]["MatchResult"];

function withData(
  playerId: string,
  wins: number,
  losses: number,
  tieBreaksPlayed: number,
  tieBreaksWon: number,
  recentResults: MatchResult[],
): PlayerStats {
  const current = recentResults[0];
  let count = 0;
  while (recentResults[count] === current) count++;
  return {
    playerId,
    matchesPlayed: wins + losses,
    hasEnoughData: true,
    record: { wins, losses, winPercentage: percentage(wins, wins + losses) },
    tieBreak: {
      tieBreaksPlayed,
      tieBreaksWon,
      tieBreakWinPercentage: percentage(tieBreaksWon, tieBreaksPlayed),
    },
    streak: { current: { result: current, count }, recentResults },
  };
}

function percentage(part: number, total: number): number {
  return total === 0 ? 0 : Math.round((part / total) * 1000) / 10;
}

const W = "WIN" as const;
const L = "LOSS" as const;

export const stats: Record<string, PlayerStats> = {
  // Spec example: solid record, shaky in tie-breaks, on a win streak.
  a1b2c3: withData("a1b2c3", 27, 15, 11, 4, [W, W, W, L, W, L, W, W, L, W]),
  d4e5f6: withData("d4e5f6", 19, 21, 14, 10, [L, L, W, W, L, W, L, L, W, L]),
  // Spec example for a player without crawled matches.
  g7h8i9: {
    playerId: "g7h8i9",
    matchesPlayed: 0,
    hasEnoughData: false,
    record: null,
    tieBreak: null,
    streak: null,
  },
  j1k2l3: withData("j1k2l3", 34, 6, 9, 8, [W, W, W, W, W, W, L, W, W, W]),
  m4n5o6: withData("m4n5o6", 8, 17, 7, 1, [L, L, L, L, W, L, L, W, L, L]),
  // Very few matches: enough data, but a small sample.
  p7q8r9: withData("p7q8r9", 2, 1, 1, 1, [W, L, W]),
  s1t2u3: withData("s1t2u3", 22, 20, 18, 9, [L, W, L, W, W, L, W, L, L, W]),
  v4w5x6: withData("v4w5x6", 12, 9, 5, 3, [W, W, L, W, L, L, W, W, W, L]),
};
