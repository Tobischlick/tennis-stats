import type { components } from "@/lib/api/schema";
import type { MatchResult } from "./stats";

export type HeadToHead = components["schemas"]["HeadToHead"];

// Meetings keyed "playerId:opponentId", newest first, from playerId's perspective.
// The reverse direction is derived by flipping each result.
const meetings: Record<string, MatchResult[]> = {
  "a1b2c3:d4e5f6": ["WIN", "LOSS", "WIN"],
  "a1b2c3:p7q8r9": ["WIN"],
  "j1k2l3:m4n5o6": ["WIN", "WIN", "WIN", "WIN", "LOSS"],
  "j1k2l3:s1t2u3": ["LOSS", "WIN"],
  "s1t2u3:m4n5o6": ["WIN", "LOSS", "LOSS", "WIN"],
  "d4e5f6:v4w5x6": ["LOSS", "LOSS"],
};

const flip = (r: MatchResult): MatchResult => (r === "WIN" ? "LOSS" : "WIN");

export function headToHeadFor(playerId: string, opponentId: string): HeadToHead {
  const recentResults =
    meetings[`${playerId}:${opponentId}`] ??
    meetings[`${opponentId}:${playerId}`]?.map(flip) ??
    [];
  if (recentResults.length === 0) {
    return {
      playerId,
      opponentId,
      matchesPlayed: 0,
      hasEnoughData: false,
      record: null,
      recentResults: [],
    };
  }
  const wins = recentResults.filter((r) => r === "WIN").length;
  const losses = recentResults.length - wins;
  return {
    playerId,
    opponentId,
    matchesPlayed: recentResults.length,
    hasEnoughData: true,
    record: {
      wins,
      losses,
      winPercentage: Math.round((wins / recentResults.length) * 1000) / 10,
    },
    recentResults,
  };
}
