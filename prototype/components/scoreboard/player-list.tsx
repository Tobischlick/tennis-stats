import Link from "next/link";
import { ChevronRight } from "lucide-react";
import { getPlayerStats, type PlayerSummary } from "@/lib/mock/api";
import { ResultChip } from "./result-chip";

export function PlayerList({
  players,
  hrefFor,
}: {
  players: PlayerSummary[];
  hrefFor: (player: PlayerSummary) => string;
}) {
  return (
    <ul className="divide-y divide-(--sb-line) overflow-hidden rounded-md border border-(--sb-line) bg-(--sb-panel)">
      {players.map((p) => {
        const stats = getPlayerStats(p.id);
        return (
          <li key={p.id}>
            <Link
              href={hrefFor(p)}
              className="flex items-center gap-3 px-4 py-3 transition-colors hover:bg-(--sb-panel-2)"
            >
              <div className="min-w-0 flex-1">
                <div className="truncate font-semibold">{p.name}</div>
                <div className="truncate text-sm text-(--sb-muted)">{p.club ?? "Kein Verein angegeben"}</div>
              </div>
              {stats?.record ? (
                <div className="flex shrink-0 flex-col items-end gap-1">
                  <span className="sb-num text-sm font-bold">
                    <span className="text-(--sb-win)">{stats.record.wins}</span>
                    <span className="text-(--sb-muted)">–</span>
                    <span className="text-(--sb-loss)">{stats.record.losses}</span>
                  </span>
                  <span className="hidden gap-0.5 sm:flex" aria-label="Letzte 5 Ergebnisse">
                    {stats.streak?.recentResults.slice(0, 5).map((r, i) => (
                      <ResultChip key={i} result={r} size="sm" />
                    ))}
                  </span>
                </div>
              ) : (
                <span className="sb-label shrink-0 text-[10px] text-(--sb-muted)">Noch keine Matches</span>
              )}
              <ChevronRight className="size-4 shrink-0 text-(--sb-muted)" aria-hidden />
            </Link>
          </li>
        );
      })}
    </ul>
  );
}
