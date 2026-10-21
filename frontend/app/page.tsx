import { Suspense } from "react";
import { SearchX } from "lucide-react";
import { DatabaseOverview, DatabaseOverviewSkeleton } from "@/components/database-overview";
import { PlayerList } from "@/components/player-list";
import { SearchForm } from "@/components/search-form";
import { Skeleton } from "@/components/ui/skeleton";
import { searchPlayers } from "@/lib/api-client";
import { MIN_QUERY_LENGTH, normalizeQuery } from "@/lib/search";

export default async function SearchPage({ searchParams }: PageProps<"/">) {
  const query = normalizeQuery((await searchParams).q);

  return (
    <div className="group mx-auto max-w-2xl">
      <div className={query ? "pt-2 md:pt-6" : "pt-10 md:pt-24"}>
        {!query && (
          <div className="mb-8 text-center md:mb-10">
            <h1 className="text-4xl font-semibold tracking-tight text-balance md:text-5xl">Spieler suchen</h1>
            <p className="mt-3 text-lg text-balance text-muted-foreground">
              Bilanz, Tie-Breaks und aktuelle Form von jedem Spieler im BTV.
            </p>
          </div>
        )}
        <SearchForm query={query} autoFocus={!query} />
      </div>

      {!query && (
        <div className="transition-opacity group-has-data-pending:opacity-60">
          <Suspense fallback={<DatabaseOverviewSkeleton />}>
            <DatabaseOverview />
          </Suspense>
        </div>
      )}

      {query && (
        <div className="transition-opacity group-has-data-pending:opacity-60">
          {query.length < MIN_QUERY_LENGTH ? (
            <p className="mt-8 text-center text-muted-foreground md:mt-10">
              Gib mindestens {MIN_QUERY_LENGTH} Zeichen ein.
            </p>
          ) : (
            <Suspense fallback={<ResultsSkeleton />}>
              <Results query={query} />
            </Suspense>
          )}
        </div>
      )}
    </div>
  );
}

async function Results({ query }: { query: string }) {
  const results = await searchPlayers(query);
  if (results.length === 0) {
    return (
      <section className="mt-16 flex flex-col items-center text-center">
        <span className="flex size-14 items-center justify-center rounded-full bg-secondary">
          <SearchX aria-hidden className="size-6 text-muted-foreground" />
        </span>
        <h1 className="mt-5 text-xl font-semibold">Kein Spieler gefunden</h1>
        <p className="mt-2 max-w-sm text-balance text-muted-foreground">
          Für „{query}“ gibt es keinen Treffer. Prüfe die Schreibweise oder suche nur nach dem Nachnamen.
        </p>
      </section>
    );
  }

  return (
    <section className="mt-8 md:mt-10">
      <h1 className="mb-3 text-sm font-normal text-muted-foreground">
        {results.length === 1 ? "1 Spieler" : `${results.length} Spieler`} für „{query}“
      </h1>
      <PlayerList players={results} />
    </section>
  );
}

function ResultsSkeleton() {
  return (
    <section className="mt-8 md:mt-10">
      <p role="status" className="sr-only">
        Suche läuft …
      </p>
      <Skeleton className="mb-3 h-5 w-40" />
      <div className="divide-y divide-border overflow-hidden rounded-2xl border border-border bg-card">
        {[0, 1, 2].map((i) => (
          <div key={i} className="flex items-center gap-4 px-4 py-4 md:px-5">
            <Skeleton className="size-10 shrink-0 rounded-full" />
            <div className="min-w-0 flex-1">
              <Skeleton className="h-5 w-44 max-w-full" />
              <Skeleton className="mt-1.5 h-4 w-32 max-w-full" />
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
