import type {
  DoublesPartners,
  HeadToHead,
  Overview,
  PlayerSeason,
  PlayerStats,
  PlayerSummary,
  RecentMatch,
} from "@/lib/api-client/types";

export const anna: PlayerSummary = { id: "anna", name: "Anna Schmidt", club: "TC Blau-Weiss Karlsruhe" };
export const berta: PlayerSummary = { id: "berta", name: "Berta Schuster", club: "TC Rot-Gelb Heidelberg" };
/** Anna's doubles partner; not in the search results, but her player page exists. */
export const clara: PlayerSummary = { id: "clara", name: "Clara Wolf", club: "TC Blau-Weiss Karlsruhe" };

/** Requests for this player's data get their connection dropped, as if the backend were down. */
export const UNREACHABLE_PLAYER_ID = "offline";

export const players: PlayerSummary[] = [anna, berta];

export const overview: Overview = {
  hasData: true,
  totals: {
    players: 2,
    clubs: 2,
    teams: 2,
    meetings: 3,
    matches: { total: 3, singles: 3, doubles: 0 },
    sets: 7,
    firstPlayedOn: "2026-05-03",
    lastPlayedOn: "2026-06-14",
  },
  funStats: {
    tieBreaks: 3,
    matchTieBreaks: 1,
    completedMatches: 3,
    decidingSetMatches: 1,
    decidingSetPercentage: 33.3,
    mostCommonScoreLine: { scoreLine: "6:4 6:3", count: 2 },
    longestWinStreak: 2,
  },
};

const annaVsBerta = { ownTeam: "TC Blau-Weiss Karlsruhe 1", opponentTeam: "TC Rot-Gelb Heidelberg 1", partner: null };

export const matches: Record<string, RecentMatch[]> = {
  anna: [
    {
      ...annaVsBerta,
      playedOn: "2026-06-14",
      type: "SINGLES",
      result: "WIN",
      opponents: [berta],
      sets: [
        { own: 7, opponent: 6, ownTieBreak: null, opponentTieBreak: null, matchTieBreak: false },
        { own: 6, opponent: 3, ownTieBreak: null, opponentTieBreak: null, matchTieBreak: false },
      ],
    },
    {
      ...annaVsBerta,
      playedOn: "2026-05-24",
      type: "SINGLES",
      result: "WIN",
      opponents: [berta],
      sets: [
        { own: 6, opponent: 7, ownTieBreak: null, opponentTieBreak: null, matchTieBreak: false },
        { own: 7, opponent: 6, ownTieBreak: null, opponentTieBreak: null, matchTieBreak: false },
        { own: 10, opponent: 8, ownTieBreak: null, opponentTieBreak: null, matchTieBreak: true },
      ],
    },
    {
      ...annaVsBerta,
      playedOn: "2026-05-03",
      type: "SINGLES",
      result: "LOSS",
      opponents: [berta],
      sets: [
        { own: 4, opponent: 6, ownTieBreak: null, opponentTieBreak: null, matchTieBreak: false },
        { own: 3, opponent: 6, ownTieBreak: null, opponentTieBreak: null, matchTieBreak: false },
      ],
    },
  ],
};

export const stats: Record<string, PlayerStats> = {
  anna: {
    playerId: anna.id,
    player: anna,
    matchesPlayed: 3,
    hasEnoughData: true,
    record: { wins: 2, losses: 1, winPercentage: 66.7 },
    tieBreak: { tieBreaksPlayed: 4, tieBreaksWon: 3, tieBreakWinPercentage: 75 },
    streak: { current: { result: "WIN", count: 2 }, recentResults: ["WIN", "WIN", "LOSS"] },
    decidingSet: { played: 1, won: 1, winPercentage: 100 },
    firstSet: {
      lostFirstSet: 2,
      comebacks: 1,
      comebackPercentage: 50,
      wonFirstSet: 1,
      blownLeads: 0,
      blownLeadPercentage: 0,
    },
    // The 05-03 loss and the 06-14 win were home meetings, the 05-24 win an away one.
    homeAway: {
      home: { wins: 1, losses: 1, winPercentage: 50 },
      away: { wins: 1, losses: 0, winPercentage: 100 },
    },
  },
  berta: noStats(berta),
  clara: noStats(clara),
};

function noStats(player: PlayerSummary): PlayerStats {
  return {
    playerId: player.id,
    player,
    matchesPlayed: 0,
    hasEnoughData: false,
    record: null,
    tieBreak: null,
    streak: null,
    decidingSet: null,
    firstSet: null,
    homeAway: null,
  };
}

/** Players without an entry here have no doubles. */
export const doublesPartners: Record<string, DoublesPartners> = {
  anna: {
    playerId: anna.id,
    player: anna,
    matchesPlayed: 8,
    hasEnoughData: true,
    partners: [
      { partner: clara, matchesPlayed: 6, record: { wins: 5, losses: 1, winPercentage: 83.3 } },
      {
        partner: { id: "mia", name: "Mia Keller", club: "TC Blau-Weiss Karlsruhe" },
        matchesPlayed: 2,
        record: { wins: 2, losses: 0, winPercentage: 100 },
      },
    ],
  },
};

// Adds up to stats.anna: all of Anna's matches are from one season.
export const seasons: Record<string, PlayerSeason[]> = {
  anna: [
    {
      season: "Sommer 2026",
      type: "SUMMER",
      startsOn: "2026-04-01",
      endsOn: "2026-09-30",
      matchesPlayed: 3,
      record: { wins: 2, losses: 1, winPercentage: 66.7 },
      tieBreak: { tieBreaksPlayed: 4, tieBreaksWon: 3, tieBreakWinPercentage: 75 },
    },
  ],
};

export const headToHead: Record<string, HeadToHead> = {
  "anna/berta": {
    playerId: anna.id,
    player: anna,
    opponentId: berta.id,
    opponent: berta,
    matchesPlayed: 3,
    hasEnoughData: true,
    record: { wins: 2, losses: 1, winPercentage: 66.7 },
    recentResults: ["WIN", "WIN", "LOSS"],
    doubles: { matchesPlayed: 0, hasEnoughData: false, record: null, meetings: [] },
  },
};
