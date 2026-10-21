import { SearchX } from "lucide-react";
import { allPlayers, searchPlayers } from "@/lib/mock/api";
import { FriendlyState } from "@/components/scoreboard/friendly-state";
import { PlayerList } from "@/components/scoreboard/player-list";
import { SearchForm } from "@/components/scoreboard/search-form";

export default async function ScoreboardSearchPage({
  searchParams,
}: {
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}) {
  const raw = (await searchParams).q;
  const q = (Array.isArray(raw) ? raw[0] : raw)?.trim() ?? "";
  const results = searchPlayers(q);
  const hrefFor = (p: { id: string }) => `/scoreboard/player/${p.id}`;

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="sb-display text-3xl font-extrabold md:text-4xl">Spieler suchen</h1>
      <p className="mt-1 text-sm text-(--sb-muted)">
        Namen eingeben – Bilanz, Tie-Breaks und aktuelle Form auf einen Blick.
      </p>

      <div className="mt-5">
        <SearchForm action="/scoreboard" name="q" defaultValue={q} placeholder="Vor- oder Nachname" autoFocus={!q} />
      </div>

      <div className="mt-6">
        {!q ? (
          <>
            <h2 className="sb-label mb-2 text-[11px] text-(--sb-muted)">Zum Ausprobieren</h2>
            <PlayerList players={allPlayers()} hrefFor={hrefFor} />
          </>
        ) : results.length === 0 ? (
          <FriendlyState icon={SearchX} title="Kein Spieler gefunden">
            Für „{q}“ gibt es keinen Treffer. Prüfe die Schreibweise oder suche nur nach dem Vor- oder
            Nachnamen.
          </FriendlyState>
        ) : (
          <>
            <h2 className="sb-label mb-2 text-[11px] text-(--sb-muted)">
              <span className="sb-num text-(--sb-text)">{results.length}</span>{" "}
              Treffer für „{q}“
            </h2>
            <PlayerList players={results} hrefFor={hrefFor} />
          </>
        )}
      </div>
    </div>
  );
}
