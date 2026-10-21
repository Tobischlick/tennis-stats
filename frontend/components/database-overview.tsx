import { CloudOff, Sprout } from "lucide-react";
import { BigNumber, StatCard } from "@/components/stat-card";
import { Skeleton } from "@/components/ui/skeleton";
import { getOverview, type Overview } from "@/lib/api-client";
import { formatCount, formatDate, formatPercent } from "@/lib/format";

export async function DatabaseOverview() {
  let overview: Overview;
  try {
    overview = await getOverview();
  } catch (error) {
    // The overview is a bonus on the search page; a failing backend must not take the search down with it.
    console.error(error);
    return (
      <p className="mt-12 flex items-center justify-center gap-2 text-sm text-muted-foreground md:mt-16">
        <CloudOff aria-hidden className="size-4" />
        Die Übersicht ist gerade nicht erreichbar.
      </p>
    );
  }
  return overview.hasData ? <OverviewStats overview={overview} /> : <NoOverviewData />;
}

function OverviewStats({ overview: { totals, funStats } }: { overview: Overview }) {
  const scoreLine = funStats.mostCommonScoreLine;

  return (
    <section aria-label="Datenbank im Überblick" className="mt-12 md:mt-16">
      <StatCard label="In der Datenbank">
        <div className="grid grid-cols-3 gap-3 sm:gap-4 md:gap-8">
          <Figure value={totals.players} label="Spieler" />
          <Figure value={totals.clubs} label={totals.clubs === 1 ? "Verein" : "Vereine"} />
          <Figure value={totals.matches.total} label={totals.matches.total === 1 ? "Match" : "Matches"} />
        </div>
        {totals.firstPlayedOn && totals.lastPlayedOn && (
          <p className="mt-6 text-sm text-muted-foreground">
            {totals.firstPlayedOn === totals.lastPlayedOn
              ? `Ergebnisse vom ${formatDate(totals.firstPlayedOn)}`
              : `Ergebnisse vom ${formatDate(totals.firstPlayedOn)} bis ${formatDate(totals.lastPlayedOn)}`}
          </p>
        )}
      </StatCard>

      <div className="mt-4 grid gap-4 sm:grid-cols-2 md:mt-5 md:gap-5">
        <StatCard label="Tie-Breaks" className="sm:odd:last:col-span-2">
          <div className="grid grid-cols-2 gap-4">
            <Figure value={funStats.tieBreaks} label="Sätze mit 7:6" />
            <Figure value={funStats.matchTieBreaks} label="Match-Tie-Breaks" />
          </div>
        </StatCard>

        {funStats.decidingSetPercentage !== null && (
          <StatCard label="Entscheidungssatz" className="sm:odd:last:col-span-2">
            <BigNumber>{formatPercent(funStats.decidingSetPercentage)}</BigNumber>
            <p className="mt-2 text-sm text-muted-foreground">der Matches gingen in den dritten Satz</p>
          </StatCard>
        )}

        {scoreLine && (
          <StatCard label="Häufigstes Ergebnis" className="sm:odd:last:col-span-2">
            <p className="text-3xl font-semibold tracking-tight [word-spacing:0.3em] md:text-4xl">
              {scoreLine.scoreLine}
            </p>
            <p className="mt-2 text-sm text-muted-foreground">
              {scoreLine.count === 1 ? "1 Match endete so" : `${formatCount(scoreLine.count)} Matches endeten so`}
            </p>
          </StatCard>
        )}

        {funStats.longestWinStreak > 0 && (
          <StatCard label="Längste Siegesserie" className="sm:odd:last:col-span-2">
            <BigNumber>
              <span className="text-primary">{formatCount(funStats.longestWinStreak)}</span>
            </BigNumber>
            <p className="mt-2 text-sm text-muted-foreground">
              {funStats.longestWinStreak === 1 ? "Sieg" : "Siege"} in Folge von einem Spieler
            </p>
          </StatCard>
        )}
      </div>
    </section>
  );
}

function Figure({ value, label }: { value: number; label: string }) {
  return (
    <div className="min-w-0">
      <p className="text-2xl font-semibold tracking-tight sm:text-3xl md:text-4xl">{formatCount(value)}</p>
      <p className="mt-1 text-sm text-muted-foreground">{label}</p>
    </div>
  );
}

function NoOverviewData() {
  return (
    <section className="mt-12 rounded-3xl border border-border bg-card px-6 py-10 text-center md:mt-16 md:py-12">
      <span className="mx-auto flex size-12 items-center justify-center rounded-full bg-primary-soft">
        <Sprout aria-hidden className="size-5 text-primary" />
      </span>
      <h2 className="mt-4 text-xl font-semibold tracking-tight">Noch keine Daten</h2>
      <p className="mx-auto mt-2 max-w-md text-balance text-muted-foreground">
        Sobald die ersten Ergebnisse aus dem BTV eingelesen sind, siehst du hier, wie viele Spieler, Vereine und Matches
        erfasst sind.
      </p>
    </section>
  );
}

export function DatabaseOverviewSkeleton() {
  return (
    <section className="mt-12 md:mt-16">
      <p role="status" className="sr-only">
        Übersicht wird geladen …
      </p>
      <div className="rounded-3xl border border-border bg-card p-6 md:p-7">
        <Skeleton className="h-5 w-32" />
        <div className="mt-4 grid grid-cols-3 gap-3 sm:gap-4 md:gap-8">
          {[0, 1, 2].map((i) => (
            <div key={i}>
              <Skeleton className="h-8 w-16 sm:h-9 max-w-full md:h-10 md:w-20" />
              <Skeleton className="mt-1.5 h-4 w-14 max-w-full" />
            </div>
          ))}
        </div>
        <Skeleton className="mt-6 h-4 w-64 max-w-full" />
      </div>
      <div className="mt-4 grid gap-4 sm:grid-cols-2 md:mt-5 md:gap-5">
        {[0, 1, 2, 3].map((i) => (
          <div key={i} className="rounded-3xl border border-border bg-card p-6 md:p-7">
            <Skeleton className="h-5 w-28" />
            <Skeleton className="mt-4 h-12 w-24 md:h-14" />
            <Skeleton className="mt-2 h-4 w-40 max-w-full" />
          </div>
        ))}
      </div>
    </section>
  );
}
