import { Meter, StatCard } from "@/components/stat-card";
import { formatPercent, isSmallSample, matchCount } from "@/lib/format";
import type { PlayerSeason } from "@/lib/api-client/types";

export function SeasonTrend({ seasons }: { seasons: PlayerSeason[] }) {
  // A season with only an undecided rubber has no win rate to plot.
  const rated = seasons.filter((s) => s.matchesPlayed > 0);

  return (
    <StatCard label="Saisonverlauf" className="md:col-span-2">
      {rated.length > 1 ? (
        <TrendLine seasons={rated} />
      ) : (
        <p className="text-sm text-muted-foreground">
          Erst eine Saison mit Matches erfasst – der Verlauf erscheint ab der zweiten Saison.
        </p>
      )}
      <ol className="mt-5 divide-y divide-border" aria-label="Saisons, älteste zuerst">
        {seasons.map((season) => (
          <SeasonRow key={season.season} season={season} />
        ))}
      </ol>
    </StatCard>
  );
}

function TrendLine({ seasons }: { seasons: PlayerSeason[] }) {
  const points = seasons.map((s, i) => ({
    x: round((i / (seasons.length - 1)) * 100),
    y: round(100 - s.record.winPercentage),
    small: isSmallSample(s.matchesPlayed),
    label: s.season,
  }));

  return (
    <div aria-hidden className="px-2">
      <div className="relative h-28">
        <svg viewBox="0 0 100 100" preserveAspectRatio="none" className="absolute inset-0 size-full overflow-visible">
          <line x1="0" y1="50" x2="100" y2="50" className="stroke-border" strokeDasharray="2 2" vectorEffect="non-scaling-stroke" />
          <polyline
            points={points.map((p) => `${p.x},${p.y}`).join(" ")}
            fill="none"
            className="stroke-primary"
            strokeWidth="2"
            strokeLinejoin="round"
            vectorEffect="non-scaling-stroke"
          />
        </svg>
        {points.map((p) => (
          <span
            key={p.label}
            data-small-sample={p.small || undefined}
            className={`absolute size-3 -translate-x-1/2 -translate-y-1/2 rounded-full border-2 border-primary ${
              p.small ? "bg-card" : "bg-primary"
            }`}
            style={{ left: `${p.x}%`, top: `${p.y}%` }}
          />
        ))}
      </div>
      <div className="mt-2 flex justify-between gap-2 text-xs text-muted-foreground">
        <span>{seasons[0].season}</span>
        <span>{seasons[seasons.length - 1].season}</span>
      </div>
    </div>
  );
}

// Keeps float noise like 33.299999999999997 out of the SVG coordinates.
function round(value: number): number {
  return Math.round(value * 100) / 100;
}

function SeasonRow({ season }: { season: PlayerSeason }) {
  const { record, tieBreak, matchesPlayed } = season;

  return (
    <li className="py-3 first:pt-0 last:pb-0">
      <div className="flex flex-wrap items-baseline justify-between gap-x-4 gap-y-1">
        <h3 className="font-medium">{season.season}</h3>
        <p className="font-semibold">{matchesPlayed > 0 ? formatPercent(record.winPercentage) : "–"}</p>
      </div>
      {matchesPlayed > 0 && (
        <div className="mt-2">
          <Meter value={record.winPercentage} />
        </div>
      )}
      <p className="mt-2 text-sm text-muted-foreground">
        {matchCount(matchesPlayed)} · {record.wins}:{record.losses}
        {tieBreak.tieBreaksPlayed > 0 && (
          <>
            {" "}
            · {tieBreak.tieBreaksWon} von {tieBreak.tieBreaksPlayed} Tie-Breaks gewonnen
          </>
        )}
        {isSmallSample(matchesPlayed) && (
          <span className="ml-2 inline-block rounded-2xl bg-secondary px-2 py-0.5 text-xs">kleine Stichprobe</span>
        )}
      </p>
    </li>
  );
}
