import Link from "next/link";
import { ChevronLeft, ChevronRight, ListX } from "lucide-react";
import { MatchList } from "@/components/recent-matches";
import type { RecentMatch } from "@/lib/api-client/types";

export function AllMatches({
  playerId,
  playerName,
  matches,
  total,
  page,
  pageCount,
}: {
  playerId: string;
  playerName: string;
  matches: RecentMatch[];
  total: number;
  page: number;
  pageCount: number;
}) {
  if (total === 0) return <NoMatches playerId={playerId} playerName={playerName} />;

  return (
    <div className="mt-8 md:mt-10">
      {byYear(matches).map(([year, yearMatches], i) => (
        <section key={year} className="mt-4 rounded-3xl border border-border bg-card p-6 first:mt-0 md:mt-5 md:p-7">
          <div className="flex items-baseline justify-between gap-3">
            <h2 className="text-sm font-medium text-muted-foreground">{year}</h2>
            {i === 0 && page === 1 && <span className="text-xs text-muted-foreground">Neuestes oben</span>}
          </div>
          <div className="mt-2">
            <MatchList matches={yearMatches} />
          </div>
        </section>
      ))}
      {pageCount > 1 && <Pager playerId={playerId} page={page} pageCount={pageCount} />}
    </div>
  );
}

function byYear(matches: RecentMatch[]): [string, RecentMatch[]][] {
  const groups = new Map<string, RecentMatch[]>();
  for (const match of matches) {
    const year = match.playedOn.slice(0, 4);
    groups.set(year, [...(groups.get(year) ?? []), match]);
  }
  return [...groups];
}

export function matchesPageHref(playerId: string, page: number): string {
  return page > 1 ? `/player/${playerId}/matches?seite=${page}` : `/player/${playerId}/matches`;
}

const pagerLink =
  "inline-flex h-11 items-center gap-1.5 rounded-xl border border-border px-4 text-sm font-medium transition-colors hover:border-primary/40 hover:text-primary";

function Pager({ playerId, page, pageCount }: { playerId: string; page: number; pageCount: number }) {
  return (
    <nav aria-label="Seiten" className="mt-6 flex items-center justify-between gap-3">
      {page > 1 ? (
        <Link href={matchesPageHref(playerId, page - 1)} className={pagerLink}>
          <ChevronLeft aria-hidden className="size-4" /> Neuere
        </Link>
      ) : (
        <span />
      )}
      <span className="text-sm text-muted-foreground tabular-nums">
        Seite {page} von {pageCount}
      </span>
      {page < pageCount ? (
        <Link href={matchesPageHref(playerId, page + 1)} className={pagerLink}>
          Ältere <ChevronRight aria-hidden className="size-4" />
        </Link>
      ) : (
        <span />
      )}
    </nav>
  );
}

function NoMatches({ playerId, playerName }: { playerId: string; playerName: string }) {
  return (
    <section className="mt-10 rounded-3xl border border-border bg-card px-6 py-12 text-center md:mt-12 md:py-16">
      <span className="mx-auto flex size-14 items-center justify-center rounded-full bg-secondary">
        <ListX aria-hidden className="size-6 text-muted-foreground" />
      </span>
      <h2 className="mt-5 text-2xl font-semibold tracking-tight">Noch keine Matches</h2>
      <p className="mx-auto mt-3 max-w-md text-balance text-muted-foreground">
        Für {playerName} haben wir noch keine Spiele erfasst. Sobald die ersten Ergebnisse im BTV stehen, erscheinen
        sie hier.
      </p>
      <Link
        href={`/player/${playerId}`}
        className="mt-6 inline-flex h-11 items-center rounded-xl border border-border px-5 text-sm font-medium transition-colors hover:bg-secondary"
      >
        Zur Statistik
      </Link>
    </section>
  );
}
