import type { PlayerStats, PlayerSummary } from "@/lib/mock/api";
import { CourtLines } from "./court-lines";
import { formatPercent } from "./format";

export function PlayerHero({ player, stats }: { player: PlayerSummary; stats: PlayerStats }) {
  const record = stats.hasEnoughData ? stats.record : null;
  return (
    <section className="court-clay-texture relative overflow-hidden text-white">
      <CourtLines />
      <div className="relative mx-auto w-full max-w-5xl px-4 pt-8 pb-10 md:pt-12 md:pb-14">
        <p className="inline-block -skew-x-6 bg-(--court-ink) px-2.5 py-1 text-xs font-semibold tracking-wider uppercase">
          <span className="inline-block skew-x-6">{player.club ?? "Verein unbekannt"}</span>
        </p>
        <h1 className="court-display mt-3 text-6xl font-extrabold break-words md:text-8xl">
          {player.name}
        </h1>

        {record ? (
          <div className="mt-8 grid gap-6 md:grid-cols-[auto_1fr] md:items-end md:gap-12">
            <div>
              <p className="text-sm font-semibold tracking-widest text-(--court-ball) uppercase">
                Siegquote
              </p>
              <p className="court-num text-8xl font-extrabold italic md:text-9xl">
                {formatPercent(record.winPercentage)}
              </p>
            </div>
            <div className="max-w-md">
              <div className="flex items-end justify-between gap-4">
                <div>
                  <p className="court-num text-5xl font-bold">{record.wins}</p>
                  <p className="mt-1 text-sm font-medium text-white/85">Siege</p>
                </div>
                <p className="pb-6 text-sm text-white/70">{stats.matchesPlayed} Matches</p>
                <div className="text-right">
                  <p className="court-num text-5xl font-bold">{record.losses}</p>
                  <p className="mt-1 text-sm font-medium text-white/85">Niederlagen</p>
                </div>
              </div>
              <div
                className="mt-3 flex h-3 overflow-hidden rounded-full bg-(--court-clay-dark) ring-2 ring-white/60"
                role="img"
                aria-label={`${record.wins} Siege, ${record.losses} Niederlagen`}
              >
                <div className="bg-(--court-grass)" style={{ width: `${record.winPercentage}%` }} />
              </div>
            </div>
          </div>
        ) : null}
      </div>
    </section>
  );
}
