import { players } from "./players";
import { stats, type MatchResult } from "./stats";

// Not in the API: the shape follows what the crawler stores (Meeting, Rubber, RubberSet).
export type SetScore = { own: number; opponent: number; ownTieBreak?: number; opponentTieBreak?: number; matchTieBreak?: boolean };

export type RecentMatch = {
  playedOn: string;
  type: "SINGLES" | "DOUBLES";
  result: MatchResult;
  opponents: string[];
  partner?: string;
  ownTeam: string;
  opponentTeam: string;
  sets: SetScore[];
};

const opponentNames = [
  "Tim Keller", "Laura Braun", "Max Schulz", "Julia Koch", "Paul Richter", "Clara Wolf",
  "Nico Schäfer", "Hannah Neumann", "David Zimmermann", "Emma Krüger", "Ben Hartmann", "Mia Lange",
];
const partnerNames = ["Lisa Vogel", "Tom Berger", "Sarah Frank", "Jan Roth"];
const teams = [
  "TC Rot-Weiß Pforzheim", "TC Ettlingen", "TSG Weinheim", "TC Offenburg", "TC Konstanz",
  "TC Rastatt", "TC Schwetzingen", "TC Lahr",
];

const winSets: SetScore[][] = [
  [{ own: 6, opponent: 3 }, { own: 6, opponent: 4 }],
  [{ own: 7, opponent: 6, ownTieBreak: 7, opponentTieBreak: 4 }, { own: 6, opponent: 2 }],
  [{ own: 4, opponent: 6 }, { own: 6, opponent: 3 }, { own: 10, opponent: 7, matchTieBreak: true }],
  [{ own: 6, opponent: 1 }, { own: 6, opponent: 2 }],
  [{ own: 6, opponent: 4 }, { own: 3, opponent: 6 }, { own: 10, opponent: 5, matchTieBreak: true }],
];
const lossSets: SetScore[][] = [
  [{ own: 4, opponent: 6 }, { own: 3, opponent: 6 }],
  [{ own: 6, opponent: 7, ownTieBreak: 5, opponentTieBreak: 7 }, { own: 4, opponent: 6 }],
  [{ own: 6, opponent: 4 }, { own: 2, opponent: 6 }, { own: 8, opponent: 10, matchTieBreak: true }],
  [{ own: 2, opponent: 6 }, { own: 1, opponent: 6 }],
];

// Derived from each player's recentResults so the list always agrees with the form dots.
export function recentMatchesFor(playerId: string): RecentMatch[] {
  const player = players.find((p) => p.id === playerId);
  const results = stats[playerId]?.streak?.recentResults ?? [];
  const seed = [...playerId].reduce((n, c) => n + c.charCodeAt(0), 0);
  const latest = new Date(Date.UTC(2026, 8, 20));
  return results.map((result, i) => {
    const k = seed + i * 7;
    const doubles = k % 3 === 0;
    const playedOn = new Date(latest);
    playedOn.setUTCDate(latest.getUTCDate() - i * 7 - (k % 3));
    const pool = result === "WIN" ? winSets : lossSets;
    return {
      playedOn: playedOn.toISOString().slice(0, 10),
      type: doubles ? "DOUBLES" : "SINGLES",
      result,
      opponents: doubles
        ? [opponentNames[k % 12], opponentNames[(k + 5) % 12]]
        : [opponentNames[k % 12]],
      partner: doubles ? partnerNames[k % 4] : undefined,
      ownTeam: player?.club ?? "TC Baden",
      opponentTeam: teams[k % teams.length],
      sets: pool[k % pool.length],
    };
  });
}

