import { Swords } from "lucide-react";
import { allPlayers, searchPlayers, type PlayerSummary } from "@/lib/mock/api";
import { firstName } from "./format";
import { PlayerList } from "./player-list";
import { SearchField } from "./search-field";

export function CompareSection({ player, query }: { player: PlayerSummary; query: string }) {
  const others = (list: PlayerSummary[]) => list.filter((p) => p.id !== player.id);
  const results = query ? others(searchPlayers(query)) : others(allPlayers()).slice(0, 3);
  const hrefFor = (p: PlayerSummary) => `/court/player/${player.id}/vs/${p.id}`;

  return (
    <section id="vergleich" className="court-grass-texture scroll-mt-4 text-white">
      <div className="mx-auto w-full max-w-5xl px-4 py-10 md:grid md:grid-cols-[1fr_1.2fr] md:gap-10">
        <div>
          <p className="flex items-center gap-2 text-sm font-semibold tracking-widest text-(--court-ball) uppercase">
            <Swords className="size-4" /> Head-to-Head
          </p>
          <h2 className="court-display mt-2 text-4xl font-extrabold md:text-5xl">
            {firstName(player.name)} vergleichen mit…
          </h2>
          <p className="mt-2 text-base text-white/80">
            Wähle eine Gegnerin oder einen Gegner und sieh die direkte Bilanz.
          </p>
          <div className="mt-5">
            <SearchField
              action={`/court/player/${player.id}`}
              name="vs"
              defaultValue={query}
              placeholder="Gegner suchen"
              label="Gegner suchen"
              scroll={false}
            />
          </div>
        </div>
        <div className="mt-6 text-(--court-ink) md:mt-0">
          <p className="mb-3 text-sm font-medium text-white/80">
            {query ? `Treffer für „${query}“` : "Vorschläge"}
          </p>
          {results.length > 0 ? (
            <PlayerList players={results} hrefFor={hrefFor} tone="grass" />
          ) : (
            <p className="rounded-2xl bg-white/10 px-4 py-5 text-sm text-white ring-1 ring-white/20">
              Niemanden für „{query}“ gefunden. Versuch es mit einem anderen Namen.
            </p>
          )}
        </div>
      </div>
    </section>
  );
}
