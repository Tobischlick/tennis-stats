import Link from "next/link";
import { SearchX } from "lucide-react";
import { searchPlayers } from "@/lib/mock/api";
import { PlayerList } from "@/components/clean/player-list";
import { SearchForm } from "@/components/clean/search-form";

const examples = ["Anna", "Weber", "Hoffmann"];

export default async function CleanSearchPage({
  searchParams,
}: {
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}) {
  const raw = (await searchParams).q;
  const query = (Array.isArray(raw) ? raw[0] : raw)?.trim() ?? "";
  const results = searchPlayers(query);

  return (
    <div className="mx-auto max-w-2xl">
      <div className={query ? "pt-4 md:pt-8" : "pt-10 md:pt-24"}>
        {!query && (
          <div className="mb-8 md:mb-10">
            <h1 className="text-4xl font-semibold tracking-tight md:text-5xl">Spieler suchen</h1>
            <p className="mt-3 text-lg text-muted-foreground">
              Bilanz, Tie-Breaks und aktuelle Form von jedem Spieler im BTV.
            </p>
          </div>
        )}
        <SearchForm defaultValue={query} autoFocus={!query} />
      </div>

      {!query && (
        <div className="mt-6 flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
          <span>Zum Ausprobieren:</span>
          {examples.map((e) => (
            <Link
              key={e}
              href={`/clean?q=${encodeURIComponent(e)}`}
              className="rounded-full border border-border bg-card px-3 py-1 text-foreground transition-colors hover:border-primary/40 hover:text-primary"
            >
              {e}
            </Link>
          ))}
        </div>
      )}

      {query && results.length > 0 && (
        <div className="mt-10">
          <p className="mb-3 text-sm text-muted-foreground">
            {results.length === 1 ? "1 Spieler" : `${results.length} Spieler`} für „{query}“
          </p>
          <PlayerList players={results} hrefFor={(p) => `/clean/player/${p.id}`} />
        </div>
      )}

      {query && results.length === 0 && (
        <div className="mt-16 flex flex-col items-center text-center">
          <span className="flex size-14 items-center justify-center rounded-full bg-secondary">
            <SearchX aria-hidden className="size-6 text-muted-foreground" />
          </span>
          <h2 className="mt-5 text-xl font-semibold">Kein Spieler gefunden</h2>
          <p className="mt-2 max-w-sm text-muted-foreground">
            Für „{query}“ gibt es keinen Treffer. Prüfe die Schreibweise oder suche nur nach dem Nachnamen.
          </p>
        </div>
      )}
    </div>
  );
}
