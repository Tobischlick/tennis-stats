import { BigNumber, Meter, StatCard } from "@/components/stat-card";
import { formatPercent, isSmallSample, matchCount } from "@/lib/format";
import type { FirstSetStats } from "@/lib/api-client/types";

export function FirstSetCard({ firstSet }: { firstSet: FirstSetStats }) {
  const counted = firstSet.lostFirstSet + firstSet.wonFirstSet;

  return (
    <StatCard label="Nach dem ersten Satz" className="md:col-span-2">
      {counted > 0 ? (
        <>
          <div className="grid gap-6 md:grid-cols-2 md:gap-10">
            <Rate
              title="Comeback-Quote"
              played={firstSet.lostFirstSet}
              hits={firstSet.comebacks}
              percentage={firstSet.comebackPercentage}
              caption="nach verlorenem ersten Satz noch gewonnen"
              empty="Noch keinen ersten Satz verloren"
            />
            <Rate
              title="Vorsprung verspielt"
              played={firstSet.wonFirstSet}
              hits={firstSet.blownLeads}
              percentage={firstSet.blownLeadPercentage}
              caption="nach gewonnenem ersten Satz noch verloren"
              empty="Noch keinen ersten Satz gewonnen"
            />
          </div>
          {isSmallSample(counted) && (
            <p className="mt-5 inline-block rounded-2xl bg-secondary px-3 py-1 text-sm text-muted-foreground">
              Erst {matchCount(counted)} gewertet – die Quoten können sich noch deutlich ändern.
            </p>
          )}
        </>
      ) : (
        <>
          <p className="font-medium">Noch kein Match mit beendetem ersten Satz</p>
          <p className="mt-1 text-sm text-muted-foreground">Aufgaben zählen nicht mit.</p>
        </>
      )}
    </StatCard>
  );
}

function Rate({
  title,
  played,
  hits,
  percentage,
  caption,
  empty,
}: {
  title: string;
  played: number;
  hits: number;
  percentage: number;
  caption: string;
  empty: string;
}) {
  return (
    <div>
      <h3 className="font-medium">{title}</h3>
      {played > 0 ? (
        <>
          <div className="mt-2">
            <BigNumber>{formatPercent(percentage)}</BigNumber>
          </div>
          <div className="mt-4">
            <Meter value={percentage} />
          </div>
          <p className="mt-3 text-sm text-muted-foreground">
            {hits} von {played} {caption}
          </p>
        </>
      ) : (
        <p className="mt-2 text-sm text-muted-foreground">{empty}</p>
      )}
    </div>
  );
}
