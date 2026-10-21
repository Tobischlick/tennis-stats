import type { RecentMatch, SetScore } from "@/lib/api-client/types";
import { formatShortDate } from "@/lib/format";

const VISIBLE = 5;

export function RecentMatches({ matches }: { matches: RecentMatch[] }) {
  const more = matches.slice(VISIBLE);
  return (
    <div>
      <MatchList matches={matches.slice(0, VISIBLE)} />
      {more.length > 0 && (
        <details className="group">
          <summary className="mt-2 inline-block cursor-pointer list-none rounded-md text-sm font-medium text-primary group-open:hidden hover:underline focus-visible:ring-2 focus-visible:ring-ring/50 focus-visible:outline-none [&::-webkit-details-marker]:hidden">
            {more.length === 1 ? "1 weiteres Match anzeigen" : `${more.length} weitere Matches anzeigen`}
          </summary>
          <MatchList matches={more} />
        </details>
      )}
    </div>
  );
}

function MatchList({ matches }: { matches: RecentMatch[] }) {
  return (
    <ol className="divide-y divide-border">
      {matches.map((m, i) => (
        <li key={i} className="flex items-center gap-3 py-3.5 md:gap-4">
          <span
            aria-hidden
            className={`size-3 shrink-0 rounded-full ${m.result === "WIN" ? "bg-primary" : "border-2 border-foreground/15"}`}
          />
          <div className="min-w-0 flex-1">
            <p className="truncate font-medium">
              <span className="sr-only">{m.result === "WIN" ? "Sieg gegen " : "Niederlage gegen "}</span>
              {m.opponents.map((o) => o.name).join(" / ")}
            </p>
            <p className="truncate text-xs text-muted-foreground">
              {formatShortDate(m.playedOn)} ·{" "}
              {m.type === "DOUBLES" ? `Doppel${m.partner ? ` mit ${m.partner.name}` : ""}` : "Einzel"}
              <span className="hidden sm:inline"> · gegen {m.opponentTeam}</span>
            </p>
          </div>
          <p className="shrink-0 text-right text-sm font-medium tabular-nums">
            {m.sets.map((s, j) => (
              <SetResult key={j} set={s} />
            ))}
          </p>
        </li>
      ))}
    </ol>
  );
}

function SetResult({ set }: { set: SetScore }) {
  const won = set.own > set.opponent;
  const tieBreakLoserPoints =
    set.ownTieBreak !== null && set.opponentTieBreak !== null
      ? Math.min(set.ownTieBreak, set.opponentTieBreak)
      : null;
  return (
    <span
      className={`ml-2.5 first:ml-0 ${won ? "" : "text-muted-foreground"}`}
      title={set.matchTieBreak ? "Match-Tie-Break" : undefined}
    >
      {set.own}:{set.opponent}
      {tieBreakLoserPoints !== null && <sup className="ml-px text-[0.65em]">{tieBreakLoserPoints}</sup>}
    </span>
  );
}
