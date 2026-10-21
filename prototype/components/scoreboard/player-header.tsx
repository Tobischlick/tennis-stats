import type { PlayerSummary } from "@/lib/mock/api";

export function PlayerHeader({ player, matchesPlayed }: { player: PlayerSummary; matchesPlayed: number }) {
  return (
    <div className="sb-scanlines flex items-end justify-between gap-4 rounded-md border border-(--sb-line) bg-(--sb-panel) px-4 py-4">
      <div className="min-w-0">
        <p className="sb-label truncate text-[11px] text-(--sb-amber)">{player.club ?? "Kein Verein angegeben"}</p>
        <h1 className="sb-display mt-0.5 text-3xl leading-none font-extrabold break-words md:text-5xl">
          {player.name}
        </h1>
      </div>
      <div className="shrink-0 text-right">
        <div className="sb-num text-2xl leading-none font-bold md:text-3xl">{matchesPlayed}</div>
        <div className="sb-label mt-1 text-[10px] text-(--sb-muted)">Matches</div>
      </div>
    </div>
  );
}
