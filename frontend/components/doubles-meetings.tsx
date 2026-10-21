import type { DoublesMeeting } from "@/lib/api-client/types";
import { formatShortDate } from "@/lib/format";
import { SetResult } from "./recent-matches";

export function DoublesMeetings({ meetings, opponentName }: { meetings: DoublesMeeting[]; opponentName: string }) {
  return (
    <ol className="divide-y divide-border">
      {meetings.map((m, i) => (
        <li key={i} className="flex items-center gap-3 px-4 py-3.5 md:gap-4 md:px-5">
          <span
            aria-hidden
            className={`size-3 shrink-0 rounded-full ${m.result === "WIN" ? "bg-primary" : "border-2 border-foreground/15"}`}
          />
          <div className="min-w-0 flex-1">
            <p className="truncate font-medium">
              <span className="sr-only">{m.result === "WIN" ? "Sieg gegen " : "Niederlage gegen "}</span>
              {[opponentName, m.opponentPartner?.name].filter(Boolean).join(" / ")}
            </p>
            <p className="truncate text-xs text-muted-foreground">
              {formatShortDate(m.playedOn)}
              {m.partner && ` · mit ${m.partner.name}`}
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
