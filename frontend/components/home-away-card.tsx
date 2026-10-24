import { BigNumber, Meter, StatCard } from "@/components/stat-card";
import { formatPercent, isSmallSample, matchCount, resultLabel } from "@/lib/format";
import type { HomeAwayStats, WinLossRecord } from "@/lib/api-client/types";

export function HomeAwayCard({ homeAway }: { homeAway: HomeAwayStats }) {
  const home = played(homeAway.home);
  const away = played(homeAway.away);

  return (
    <StatCard label="Heim und auswärts" className="md:col-span-2">
      <div className="grid gap-6 md:grid-cols-2 md:gap-10">
        <Side title="Heim" record={homeAway.home} empty="Noch kein Heimspiel" />
        <Side title="Auswärts" record={homeAway.away} empty="Noch kein Auswärtsspiel" />
      </div>
      {(isSmallSample(home) || isSmallSample(away)) && (
        <p className="mt-5 inline-block rounded-2xl bg-secondary px-3 py-1 text-sm text-muted-foreground">
          Erst {matchCount(home)} zu Hause und {matchCount(away)} auswärts – die Quoten können sich noch deutlich
          ändern.
        </p>
      )}
    </StatCard>
  );
}

function played(record: WinLossRecord): number {
  return record.wins + record.losses;
}

function Side({ title, record, empty }: { title: string; record: WinLossRecord; empty: string }) {
  return (
    <div>
      <h3 className="font-medium">{title}</h3>
      {played(record) > 0 ? (
        <>
          <div className="mt-2">
            <BigNumber>{formatPercent(record.winPercentage)}</BigNumber>
          </div>
          <div className="mt-4">
            <Meter value={record.winPercentage} />
          </div>
          <p className="mt-3 text-sm text-muted-foreground">
            {record.wins} {resultLabel("WIN", record.wins)} · {record.losses} {resultLabel("LOSS", record.losses)}
          </p>
        </>
      ) : (
        <p className="mt-2 text-sm text-muted-foreground">{empty}</p>
      )}
    </div>
  );
}
