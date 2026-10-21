import type { PlayerStats } from "@/lib/mock/api";
import { formatPercent } from "./format";

type TieBreak = NonNullable<PlayerStats["tieBreak"]>;

function verdict(tb: TieBreak) {
  if (tb.tieBreaksPlayed < 3)
    return { text: "Noch kaum Tie-Breaks gespielt", tone: "text-(--court-muted) bg-black/5" };
  if (tb.tieBreakWinPercentage < 40)
    return { text: "Wackelt im Tie-Break", tone: "text-white bg-(--court-clay)" };
  if (tb.tieBreakWinPercentage <= 60)
    return { text: "Solide im Tie-Break", tone: "text-(--court-ink) bg-(--court-ball)" };
  return { text: "Nervenstark im Tie-Break", tone: "text-white bg-(--court-grass)" };
}

export function TieBreakCard({ tieBreak }: { tieBreak: TieBreak }) {
  const v = verdict(tieBreak);
  const pct = tieBreak.tieBreakWinPercentage;
  return (
    <article className="rounded-3xl bg-(--court-paper) p-6 shadow-sm ring-1 ring-black/5">
      <header className="flex items-start justify-between gap-3">
        <h2 className="court-display court-baseline min-w-0 text-2xl font-bold">Tie-Break-Quote</h2>
        <span className={`shrink-0 whitespace-nowrap rounded-full px-3 py-1 text-xs font-bold ${v.tone}`}>{v.text}</span>
      </header>
      <p className="court-num mt-5 text-7xl font-extrabold italic md:text-8xl">
        {formatPercent(pct)}
      </p>
      <p className="mt-2 text-base text-(--court-muted)">
        <strong className="text-(--court-ink)">{tieBreak.tieBreaksWon}</strong> von{" "}
        <strong className="text-(--court-ink)">{tieBreak.tieBreaksPlayed}</strong> Tie-Breaks gewonnen
      </p>
      <div className="relative mt-5">
        <div className="flex h-4 overflow-hidden rounded-full bg-(--court-clay-soft)">
          <div className="rounded-full bg-(--court-grass)" style={{ width: `${pct}%` }} />
        </div>
        <div className="absolute inset-y-[-4px] left-1/2 w-0.5 bg-(--court-ink)/60" />
        <div className="mt-1.5 flex justify-between text-xs text-(--court-muted)">
          <span>0 %</span>
          <span>50 %</span>
          <span>100 %</span>
        </div>
      </div>
    </article>
  );
}
