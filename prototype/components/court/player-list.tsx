import Link from "next/link";
import { ChevronRight } from "lucide-react";
import type { PlayerSummary } from "@/lib/mock/api";
import { initials } from "./format";

export function PlayerList({
  players,
  hrefFor,
  tone = "clay",
}: {
  players: PlayerSummary[];
  hrefFor: (player: PlayerSummary) => string;
  tone?: "clay" | "grass";
}) {
  const avatar =
    tone === "clay"
      ? "bg-(--court-clay) text-white"
      : "bg-(--court-grass) text-white";
  return (
    <ul className="divide-y divide-black/5 overflow-hidden rounded-2xl bg-(--court-paper) shadow-sm ring-1 ring-black/5">
      {players.map((player) => (
        <li key={player.id}>
          <Link
            href={hrefFor(player)}
            className="group flex items-center gap-4 px-4 py-3.5 transition-colors hover:bg-(--court-clay-soft)/60"
          >
            <span
              className={`court-num flex size-11 shrink-0 -skew-x-6 items-center justify-center rounded-lg text-lg font-bold ${avatar}`}
            >
              {initials(player.name)}
            </span>
            <span className="min-w-0 flex-1">
              <span className="block truncate text-base font-semibold">{player.name}</span>
              <span className="block truncate text-sm text-(--court-muted)">
                {player.club ?? "Verein unbekannt"}
              </span>
            </span>
            <ChevronRight className="size-5 shrink-0 text-(--court-muted) transition-transform group-hover:translate-x-1" />
          </Link>
        </li>
      ))}
    </ul>
  );
}
