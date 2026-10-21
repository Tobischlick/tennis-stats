import { allPlayers, searchPlayers, type PlayerSummary } from "@/lib/mock/api";
import { Panel } from "./panel";
import { PlayerList } from "./player-list";
import { SearchForm } from "./search-form";

export function CompareSection({ player, query }: { player: PlayerSummary; query: string }) {
  const others = (list: PlayerSummary[]) => list.filter((p) => p.id !== player.id);
  const results = query ? others(searchPlayers(query)) : others(allPlayers()).slice(0, 4);
  const hrefFor = (p: PlayerSummary) => `/scoreboard/player/${player.id}/vs/${p.id}`;

  return (
    <Panel label="Vergleichen mit …">
      <p className="mb-3 text-sm text-(--sb-muted)">
        Direkter Vergleich: Wer hat gewonnen, wenn {player.name} auf jemand anderen getroffen ist?
      </p>
      <SearchForm
        action={`/scoreboard/player/${player.id}`}
        name="vs"
        defaultValue={query}
        placeholder="Gegner suchen"
        buttonLabel="Finden"
      />
      <div className="mt-3">
        {results.length > 0 ? (
          <>
            {!query && <h3 className="sb-label mb-2 text-[10px] text-(--sb-muted)">Vorschläge</h3>}
            <PlayerList players={results} hrefFor={hrefFor} />
          </>
        ) : (
          <p className="text-sm text-(--sb-muted)">Kein anderer Spieler passt zu „{query}“.</p>
        )}
      </div>
    </Panel>
  );
}
