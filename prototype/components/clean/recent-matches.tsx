import type { RecentMatch, SetScore } from "@/lib/mock/api";

const VISIBLE = 5;
const date = new Intl.DateTimeFormat("de-DE", { day: "2-digit", month: "2-digit", timeZone: "UTC" });

export function RecentMatches({ matches }: { matches: RecentMatch[] }) {
  const visible = matches.slice(0, VISIBLE);
  const more = matches.slice(VISIBLE);
  return (
    <div>
      <MatchList matches={visible} />
      {more.length > 0 && (
        <details className="group">
          <summary className="mt-2 cursor-pointer list-none text-sm font-medium text-primary group-open:hidden [&::-webkit-details-marker]:hidden">
            {more.length} weitere anzeigen
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
            className={`size-3 shrink-0 rounded-full ${
              m.result === "WIN" ? "bg-primary" : "border-2 border-foreground/15"
            }`}
          />
          <div className="min-w-0 flex-1">
            <p className="truncate font-medium">
              <span className="sr-only">{m.result === "WIN" ? "Sieg gegen " : "Niederlage gegen "}</span>
              {m.opponents.join(" / ")}
            </p>
            <p className="truncate text-xs text-muted-foreground">
              {date.format(new Date(m.playedOn))} · {m.type === "DOUBLES" ? `Doppel mit ${m.partner}` : "Einzel"}
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
    set.ownTieBreak !== undefined && set.opponentTieBreak !== undefined
      ? Math.min(set.ownTieBreak, set.opponentTieBreak)
      : undefined;
  return (
    <span
      className={`ml-2.5 first:ml-0 ${won ? "" : "text-muted-foreground"}`}
      title={set.matchTieBreak ? "Match-Tiebreak" : undefined}
    >
      {set.own}:{set.opponent}
      {tieBreakLoserPoints !== undefined && <sup className="ml-px text-[0.65em]">{tieBreakLoserPoints}</sup>}
    </span>
  );
}
