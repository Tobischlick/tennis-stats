import { Flame, Snowflake, TrendingUp } from "lucide-react";
import type { PlayerStats } from "@/lib/mock/api";
import { FormStrip } from "./form-strip";

type Streak = NonNullable<PlayerStats["streak"]>;

function streakLabel({ result, count }: Streak["current"]) {
  const win = result === "WIN";
  if (count === 1) return win ? "Letztes Match gewonnen" : "Letztes Match verloren";
  return win ? `${count} Siege in Folge` : `${count} Niederlagen in Folge`;
}

export function FormCard({ streak }: { streak: Streak }) {
  const { result, count } = streak.current;
  const hot = result === "WIN" && count >= 3;
  const cold = result === "LOSS" && count >= 3;
  const Icon = hot ? Flame : cold ? Snowflake : TrendingUp;
  const badge = hot
    ? { text: "Läuft gerade", tone: "bg-(--court-grass) text-white" }
    : cold
      ? { text: "Formtief", tone: "bg-(--court-clay) text-white" }
      : { text: "Wechselhaft", tone: "bg-black/5 text-(--court-muted)" };
  const wins = streak.recentResults.filter((r) => r === "WIN").length;

  return (
    <article className="rounded-3xl bg-(--court-paper) p-6 shadow-sm ring-1 ring-black/5">
      <header className="flex items-start justify-between gap-3">
        <h2 className="court-display court-baseline text-2xl font-bold">Aktuelle Form</h2>
        <span className={`rounded-full px-3 py-1 text-xs font-bold ${badge.tone}`}>{badge.text}</span>
      </header>
      <div className="mt-5 flex items-center gap-3">
        <span
          className={`flex size-14 shrink-0 items-center justify-center rounded-2xl ${
            result === "WIN" ? "bg-(--court-grass-soft) text-(--court-grass)" : "bg-(--court-clay-soft) text-(--court-clay)"
          }`}
        >
          <Icon className="size-7" />
        </span>
        <p className="court-display text-4xl font-extrabold md:text-5xl">{streakLabel(streak.current)}</p>
      </div>
      <p className="mt-5 mb-3 text-sm text-(--court-muted)">
        Letzte {streak.recentResults.length} Matches: <strong className="text-(--court-ink)">{wins} S</strong>{" "}
        · <strong className="text-(--court-ink)">{streak.recentResults.length - wins} N</strong>
      </p>
      <FormStrip results={streak.recentResults} />
    </article>
  );
}
