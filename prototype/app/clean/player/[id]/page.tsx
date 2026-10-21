import Link from "next/link";
import { notFound } from "next/navigation";
import { ArrowLeft, Sprout } from "lucide-react";
import { allPlayers, getPlayer, getPlayerStats, getRecentMatches } from "@/lib/mock/api";
import { ComparePicker } from "@/components/clean/compare-picker";
import { FormDots } from "@/components/clean/form-dots";
import { Initials } from "@/components/clean/player-list";
import { RecentMatches } from "@/components/clean/recent-matches";
import { BigNumber, Meter, StatCard } from "@/components/clean/stat-card";
import { firstName, formatPercent, tieBreakVerdict } from "@/components/clean/format";

const SMALL_SAMPLE = 5;

export default async function CleanPlayerPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const player = getPlayer(id);
  const stats = getPlayerStats(id);
  if (!player || !stats) notFound();

  const candidates = allPlayers().filter((p) => p.id !== id);
  const { record, tieBreak, streak } = stats;
  const hasStats = stats.hasEnoughData && record && tieBreak && streak;

  return (
    <div>
      <Link
        href="/clean"
        className="inline-flex items-center gap-1.5 text-sm text-muted-foreground transition-colors hover:text-foreground"
      >
        <ArrowLeft aria-hidden className="size-4" /> Suche
      </Link>

      <header className="mt-6 flex items-center gap-4 md:mt-8 md:gap-6">
        <Initials name={player.name} size="lg" />
        <div className="min-w-0">
          <h1 className="text-3xl font-semibold tracking-tight md:text-5xl">{player.name}</h1>
          <p className="mt-1 text-muted-foreground md:text-lg">
            {player.club ?? "Kein Verein hinterlegt"}
            {hasStats && <span> · {stats.matchesPlayed} Matches</span>}
          </p>
        </div>
      </header>

      {hasStats ? (
        <>
          <p className="mt-10 max-w-2xl text-xl leading-relaxed md:mt-12 md:text-2xl">
            {firstName(player.name)} gewinnt{" "}
            <strong className="font-semibold">{formatPercent(record.winPercentage)}</strong> der Matches
            und <strong className="font-semibold">{formatPercent(tieBreak.tieBreakWinPercentage)}</strong> der
            Tie-Breaks – aktuell{" "}
            <strong className="font-semibold text-primary">
              {streak.current.count} {streakLabel(streak.current.result, streak.current.count)}
            </strong>{" "}
            in Folge.
          </p>

          {stats.matchesPlayed < SMALL_SAMPLE && (
            <p className="mt-4 inline-block rounded-full bg-secondary px-3 py-1 text-sm text-muted-foreground">
              Erst {stats.matchesPlayed} Matches erfasst – die Zahlen können noch schwanken.
            </p>
          )}

          <div className="mt-8 grid gap-4 md:grid-cols-2 md:gap-5">
            <StatCard label="Tie-Break-Quote">
              <BigNumber>{formatPercent(tieBreak.tieBreakWinPercentage)}</BigNumber>
              <p className="mt-2 font-medium">{tieBreakVerdict(tieBreak.tieBreakWinPercentage)}</p>
              <div className="mt-5">
                <Meter value={tieBreak.tieBreakWinPercentage} />
              </div>
              <p className="mt-3 text-sm text-muted-foreground">
                {tieBreak.tieBreaksWon} von {tieBreak.tieBreaksPlayed} Tie-Breaks gewonnen
              </p>
            </StatCard>

            <StatCard label="Aktuelle Form">
              <BigNumber>
                {streak.current.count}
                <span className="ml-2 text-2xl font-medium tracking-normal text-muted-foreground md:text-3xl">
                  {streakLabel(streak.current.result, streak.current.count)}
                </span>
              </BigNumber>
              <p className="mt-2 font-medium">in Folge</p>
              <div className="mt-5">
                <FormDots results={streak.recentResults} />
              </div>
            </StatCard>

            <StatCard label="Bilanz" className="md:col-span-2">
              <div className="grid grid-cols-3 gap-4 md:gap-8">
                <div>
                  <BigNumber compact>{record.wins}</BigNumber>
                  <p className="mt-1 text-sm text-muted-foreground">Siege</p>
                </div>
                <div>
                  <BigNumber compact>{record.losses}</BigNumber>
                  <p className="mt-1 text-sm text-muted-foreground">Niederlagen</p>
                </div>
                <div>
                  <BigNumber compact>
                    <span className="text-primary">{formatPercent(record.winPercentage)}</span>
                  </BigNumber>
                  <p className="mt-1 text-sm text-muted-foreground">Siegquote</p>
                </div>
              </div>
              <div className="mt-6">
                <Meter value={record.winPercentage} />
              </div>
            </StatCard>

            <StatCard label="Letzte Matches" className="min-w-0 md:col-span-2">
              <RecentMatches matches={getRecentMatches(id)} />
            </StatCard>
          </div>
        </>
      ) : (
        <section className="mt-10 rounded-3xl border border-border bg-card px-6 py-12 text-center md:mt-12 md:py-16">
          <span className="mx-auto flex size-14 items-center justify-center rounded-full bg-primary/10">
            <Sprout aria-hidden className="size-6 text-primary" />
          </span>
          <h2 className="mt-5 text-2xl font-semibold tracking-tight">Noch nicht genug Matches</h2>
          <p className="mx-auto mt-3 max-w-md text-muted-foreground">
            Für {player.name} haben wir noch keine Spiele erfasst. Sobald Ergebnisse vorliegen, erscheinen hier
            Bilanz, Tie-Break-Quote und aktuelle Form.
          </p>
          <Link
            href="/clean"
            className="mt-6 inline-flex h-11 items-center rounded-xl border border-border px-5 text-sm font-medium transition-colors hover:bg-secondary"
          >
            Anderen Spieler suchen
          </Link>
        </section>
      )}

      <div className="mt-4 md:mt-5">
        <ComparePicker playerId={id} playerName={firstName(player.name)} candidates={candidates} />
      </div>
    </div>
  );
}

function streakLabel(result: "WIN" | "LOSS", count: number): string {
  if (result === "WIN") return count === 1 ? "Sieg" : "Siege";
  return count === 1 ? "Niederlage" : "Niederlagen";
}
