import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { ArrowLeft, ChevronRight, Sprout } from "lucide-react";
import { ComparePicker } from "@/components/compare-picker";
import { DoublesPartnersCard } from "@/components/doubles-partners";
import { FirstSetCard } from "@/components/first-set-card";
import { FormDots } from "@/components/form-dots";
import { HomeAwayCard } from "@/components/home-away-card";
import { Initials } from "@/components/player-list";
import { RecentMatches } from "@/components/recent-matches";
import { BigNumber, Meter, StatCard } from "@/components/stat-card";
import { firstName, formatPercent, isSmallSample, matchCount, resultLabel, tieBreakVerdict } from "@/lib/format";
import {
  getDoublesPartners,
  getPlayerStats,
  getRecentMatches,
  type DoublesPartners,
  type PlayerStats,
  type RecentMatch,
} from "@/lib/api-client";

export async function generateMetadata({ params }: PageProps<"/player/[id]">): Promise<Metadata> {
  // A failed fetch is left to the page render, which shows the error boundary; the tab still gets a title.
  const stats = await getPlayerStats((await params).id).catch(() => undefined);
  return { title: stats ? `${stats.player.name} – Tennis Stats` : "Tennis Stats" };
}

export default async function PlayerPage({ params }: PageProps<"/player/[id]">) {
  const { id } = await params;
  const [stats, recentMatches, doublesPartners] = await Promise.all([
    getPlayerStats(id),
    getRecentMatches(id),
    getDoublesPartners(id),
  ]);
  if (!stats) notFound();
  const { player } = stats;

  return (
    <div>
      <Link
        href="/"
        className="inline-flex items-center gap-1.5 text-sm text-muted-foreground transition-colors hover:text-foreground"
      >
        <ArrowLeft aria-hidden className="size-4" /> Suche
      </Link>

      <header className="mt-6 flex items-center gap-4 md:mt-8 md:gap-6">
        <Initials name={player.name} size="lg" />
        <div className="min-w-0">
          <h1 className="text-3xl font-semibold tracking-tight break-words md:text-5xl">{player.name}</h1>
          <p className="mt-1 text-muted-foreground md:text-lg">
            {player.club ?? "Kein Verein hinterlegt"}
            {stats.hasEnoughData && <span> · {matchCount(stats.matchesPlayed)}</span>}
          </p>
        </div>
      </header>

      {stats.hasEnoughData ? (
        <Stats stats={stats} recentMatches={recentMatches ?? []} doublesPartners={doublesPartners} />
      ) : (
        <NoData name={player.name} />
      )}

      <div className="mt-4 md:mt-5">
        <ComparePicker playerId={id} playerName={player.name} />
      </div>
    </div>
  );
}

function Stats({
  stats,
  recentMatches,
  doublesPartners,
}: {
  stats: PlayerStats;
  recentMatches: RecentMatch[];
  doublesPartners: DoublesPartners | undefined;
}) {
  const { record, tieBreak, streak, decidingSet, firstSet, homeAway } = stats;
  if (!record || !tieBreak || !streak || !decidingSet || !firstSet || !homeAway) {
    return <NoData name={stats.player.name} />;
  }
  const streakText = `${streak.current.count} ${resultLabel(streak.current.result, streak.current.count)}`;

  return (
    <>
      <p className="mt-10 max-w-2xl text-xl leading-relaxed text-balance md:mt-12 md:text-2xl">
        {firstName(stats.player.name)} gewinnt <strong className="font-semibold">{formatPercent(record.winPercentage)}</strong>{" "}
        der Matches
        {tieBreak.tieBreaksPlayed > 0 && (
          <>
            {" "}
            und <strong className="font-semibold">{formatPercent(tieBreak.tieBreakWinPercentage)}</strong> der Tie-Breaks
          </>
        )}
        {" – "}
        {streak.current.count > 1 ? (
          <>
            aktuell <strong className="font-semibold text-primary">{streakText}</strong> in Folge.
          </>
        ) : (
          <>
            zuletzt{" "}
            <strong className="font-semibold text-primary">
              {streak.current.result === "WIN" ? "ein Sieg" : "eine Niederlage"}
            </strong>
            .
          </>
        )}
      </p>

      {isSmallSample(stats.matchesPlayed) && (
        <p className="mt-4 inline-block rounded-2xl bg-secondary px-3 py-1 text-sm text-muted-foreground">
          Erst {matchCount(stats.matchesPlayed)} erfasst – die Zahlen können sich noch deutlich ändern.
        </p>
      )}

      <div className="mt-8 grid gap-4 md:grid-cols-2 md:gap-5">
        <StatCard label="Tie-Break-Quote">
          {tieBreak.tieBreaksPlayed > 0 ? (
            <>
              <BigNumber>{formatPercent(tieBreak.tieBreakWinPercentage)}</BigNumber>
              <p className="mt-2 font-medium">{tieBreakVerdict(tieBreak.tieBreakWinPercentage)}</p>
              <div className="mt-5">
                <Meter value={tieBreak.tieBreakWinPercentage} />
              </div>
              <p className="mt-3 text-sm text-muted-foreground">
                {tieBreak.tieBreaksWon} von {tieBreak.tieBreaksPlayed} Tie-Breaks gewonnen
              </p>
            </>
          ) : (
            <p className="font-medium">Noch kein Tie-Break gespielt</p>
          )}
        </StatCard>

        <StatCard label="Aktuelle Form">
          <BigNumber>
            {streak.current.count}
            <span className="ml-2 text-2xl font-medium tracking-normal text-muted-foreground md:text-3xl">
              {resultLabel(streak.current.result, streak.current.count)}
            </span>
          </BigNumber>
          <p className="mt-2 font-medium">{streak.current.count > 1 ? "in Folge" : "im letzten Match"}</p>
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

        <StatCard label="Entscheidungssatz" className="md:col-span-2">
          {decidingSet.played > 0 ? (
            <div className="md:flex md:items-end md:justify-between md:gap-10">
              <div>
                <BigNumber>{formatPercent(decidingSet.winPercentage)}</BigNumber>
                <p className="mt-2 font-medium">
                  {decidingSet.won} von {decidingSet.played} Entscheidungssätzen gewonnen
                </p>
              </div>
              <div className="mt-5 md:mt-0 md:mb-1 md:w-1/2">
                <Meter value={decidingSet.winPercentage} />
                <p className="mt-3 text-sm text-muted-foreground">
                  Matches, die in den dritten Satz gingen – Match-Tie-Breaks zählen mit.
                </p>
              </div>
            </div>
          ) : (
            <>
              <p className="font-medium">Noch kein Match im Entscheidungssatz</p>
              <p className="mt-1 text-sm text-muted-foreground">
                Zählt Matches, die in den dritten Satz gingen – Match-Tie-Breaks inklusive.
              </p>
            </>
          )}
        </StatCard>

        <FirstSetCard firstSet={firstSet} />

        <HomeAwayCard homeAway={homeAway} />

        {doublesPartners && <DoublesPartnersCard doublesPartners={doublesPartners} />}

        {recentMatches.length > 0 && (
          <StatCard label="Letzte Matches" className="md:col-span-2">
            <RecentMatches matches={recentMatches} />
            <Link
              href={`/player/${stats.playerId}/matches`}
              className="mt-4 inline-flex items-center gap-1 rounded-md text-sm font-medium text-primary hover:underline focus-visible:ring-2 focus-visible:ring-ring/50 focus-visible:outline-none"
            >
              Alle Matches <ChevronRight aria-hidden className="size-4" />
            </Link>
          </StatCard>
        )}
      </div>
    </>
  );
}

function NoData({ name }: { name: string }) {
  return (
    <section className="mt-10 rounded-3xl border border-border bg-card px-6 py-12 text-center md:mt-12 md:py-16">
      <span className="mx-auto flex size-14 items-center justify-center rounded-full bg-primary-soft">
        <Sprout aria-hidden className="size-6 text-primary" />
      </span>
      <h2 className="mt-5 text-2xl font-semibold tracking-tight">Noch nicht genug Matches</h2>
      <p className="mx-auto mt-3 max-w-md text-balance text-muted-foreground">
        Für {name} haben wir noch keine Spiele erfasst. Sobald die ersten Ergebnisse im BTV stehen, erscheinen hier
        Bilanz, Tie-Break-Quote und aktuelle Form.
      </p>
      <Link
        href="/"
        className="mt-6 inline-flex h-11 items-center rounded-xl border border-border px-5 text-sm font-medium transition-colors hover:bg-secondary"
      >
        Anderen Spieler suchen
      </Link>
    </section>
  );
}
