"use server";

import { searchPlayers, type PlayerSummary } from "@/lib/api-client";

const MAX_RESULTS = 5;
const MAX_QUERY_LENGTH = 100;

/** Opponent candidates for the compare picker; `null` when the backend can't be reached. */
export async function searchOpponents(playerId: string, query: string): Promise<PlayerSummary[] | null> {
  const q = String(query).trim().slice(0, MAX_QUERY_LENGTH);
  if (!q) return [];
  try {
    const players = await searchPlayers(q);
    return players.filter((p) => p.id !== playerId).slice(0, MAX_RESULTS);
  } catch (error) {
    console.error(error);
    return null;
  }
}
