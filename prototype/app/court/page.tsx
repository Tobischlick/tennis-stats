import { allPlayers, searchPlayers } from "@/lib/mock/api";
import { CourtLines } from "@/components/court/court-lines";
import { FriendlyEmpty } from "@/components/court/friendly-empty";
import { PillLink } from "@/components/court/pill-link";
import { PlayerList } from "@/components/court/player-list";
import { SearchField } from "@/components/court/search-field";

export default async function CourtSearchPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const { q } = await searchParams;
  const query = typeof q === "string" ? q.trim() : "";
  const results = query ? searchPlayers(query) : [];

  return (
    <>
      <section className="court-clay-texture relative overflow-hidden text-white">
        <CourtLines />
        <div className="relative mx-auto w-full max-w-3xl px-4 pt-10 pb-12 md:pt-16 md:pb-16">
          <p className="text-sm font-semibold tracking-widest text-(--court-ball) uppercase">
            Spieler suchen
          </p>
          <h1 className="court-display mt-2 text-5xl font-extrabold md:text-7xl">
            Wer steht dir
            <br />
            gegenüber?
          </h1>
          <p className="mt-3 max-w-md text-base text-white/85">
            Name eingeben und Bilanz, Tie-Breaks und aktuelle Form sehen – für jede Spielerin und
            jeden Spieler im BTV.
          </p>
          <div className="mt-7">
            <SearchField
              action="/court"
              name="q"
              defaultValue={query}
              placeholder="z. B. Anna Schmidt"
              label="Spielername"
            />
          </div>
        </div>
      </section>

      <section className="mx-auto w-full max-w-3xl px-4 py-8">
        {!query ? (
          <>
            <h2 className="court-display court-baseline text-2xl font-bold">Zum Reinschnuppern</h2>
            <p className="mt-3 mb-4 text-sm text-(--court-muted)">
              Noch keine Idee? Tipp auf einen Namen.
            </p>
            <PlayerList players={allPlayers().slice(0, 5)} hrefFor={(p) => `/court/player/${p.id}`} />
          </>
        ) : results.length > 0 ? (
          <>
            <h2 className="court-display court-baseline text-2xl font-bold">
              {results.length} Treffer für „{query}“
            </h2>
            <div className="mt-5">
              <PlayerList players={results} hrefFor={(p) => `/court/player/${p.id}`} />
            </div>
          </>
        ) : (
          <FriendlyEmpty
            title="Aus! Kein Treffer"
            action={<PillLink href="/court" tone="outline">Neue Suche</PillLink>}
          >
            Für „{query}“ haben wir niemanden gefunden. Prüf die Schreibweise oder versuch es nur mit
            dem Nachnamen.
          </FriendlyEmpty>
        )}
      </section>
    </>
  );
}
