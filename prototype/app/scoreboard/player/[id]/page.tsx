import Link from "next/link";
import { notFound } from "next/navigation";
import { Flame, Hourglass, Info, Search, Snowflake } from "lucide-react";
import { getPlayer, getPlayerStats } from "@/lib/mock/api";
import { CompareSection } from "@/components/scoreboard/compare-section";
import { formatPercent } from "@/components/scoreboard/format";
import { FormStrip } from "@/components/scoreboard/form-strip";
import { FriendlyState } from "@/components/scoreboard/friendly-state";
import { streakText, tieBreakVerdict, toneClass } from "@/components/scoreboard/insights";
import { Panel } from "@/components/scoreboard/panel";
import { PlayerHeader } from "@/components/scoreboard/player-header";
import { SplitBar } from "@/components/scoreboard/split-bar";

const SMALL_SAMPLE = 5;

export default async function ScoreboardPlayerPage({
  params,
  searchParams,
}: {
  params: Promise<{ id: string }>;
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}) {
  const { id } = await params;
  const player = getPlayer(id);
  const stats = getPlayerStats(id);
  if (!player || !stats) notFound();

  const rawVs = (await searchParams).vs;
  const vsQuery = (Array.isArray(rawVs) ? rawVs[0] : rawVs)?.trim() ?? "";

  const { record, tieBreak, streak } = stats;
  const hasStats = stats.hasEnoughData && record && tieBreak && streak;

  return (
    <div className="space-y-4">
      <PlayerHeader player={player} matchesPlayed={stats.matchesPlayed} />

      {hasStats ? (
        <>
          {stats.matchesPlayed < SMALL_SAMPLE && (
            <p className="flex items-center gap-2 rounded-md border border-(--sb-amber)/40 bg-(--sb-amber)/10 px-3 py-2 text-sm text-(--sb-amber)">
              <Info className="size-4 shrink-0" aria-hidden />
              Erst {stats.matchesPlayed} Matches erfasst – die Werte sind noch wenig aussagekräftig.
            </p>
          )}

          <div className="grid gap-4 md:grid-cols-3">
            <Panel label="Bilanz">
              <div className="sb-num flex items-baseline gap-2 text-5xl leading-none font-extrabold">
                <span className="text-(--sb-win)">{record.wins}</span>
                <span className="text-3xl text-(--sb-muted)">:</span>
                <span className="text-(--sb-loss)">{record.losses}</span>
              </div>
              <div className="sb-label mt-1 flex gap-4 text-[10px] text-(--sb-muted)">
                <span>Siege</span>
                <span>Niederlagen</span>
              </div>
              <div className="mt-4">
                <SplitBar percent={record.winPercentage} label="Siegquote" />
              </div>
              <p className="mt-2 text-sm text-(--sb-muted)">
                <span className="sb-num font-bold text-(--sb-text)">{formatPercent(record.winPercentage)}</span>{" "}
                aller Matches gewonnen
              </p>
            </Panel>

            <Panel
              label="Tie-Break-Quote"
              aside={
                <span className={`rounded-sm border px-1.5 py-0.5 text-[11px] font-semibold ${toneClass[tieBreakVerdict(tieBreak).tone]}`}>
                  {tieBreakVerdict(tieBreak).text}
                </span>
              }
            >
              <div className="sb-num text-5xl leading-none font-extrabold">
                {tieBreak.tieBreaksPlayed > 0 ? formatPercent(tieBreak.tieBreakWinPercentage) : "–"}
              </div>
              <div className="sb-label mt-1 text-[10px] text-(--sb-muted)">gewonnen</div>
              <div className="mt-4">
                <SplitBar percent={tieBreak.tieBreakWinPercentage} label="Tie-Break-Quote" />
              </div>
              <p className="mt-2 text-sm text-(--sb-muted)">
                <span className="sb-num font-bold text-(--sb-text)">{tieBreak.tieBreaksWon}</span> von{" "}
                <span className="sb-num font-bold text-(--sb-text)">{tieBreak.tieBreaksPlayed}</span> Tie-Breaks
                gewonnen
              </p>
            </Panel>

            <Panel label="Aktuelle Form">
              <div
                className={`flex items-center gap-2 text-2xl leading-none font-bold ${
                  streak.current.result === "WIN" ? "text-(--sb-win)" : "text-(--sb-loss)"
                }`}
              >
                {streak.current.result === "WIN" ? (
                  <Flame className="size-6 shrink-0" aria-hidden />
                ) : (
                  <Snowflake className="size-6 shrink-0" aria-hidden />
                )}
                <span className="sb-display">{streakText(streak.current)}</span>
              </div>
              <div className="mt-4">
                <FormStrip results={streak.recentResults} />
              </div>
              <p className="mt-3 text-sm text-(--sb-muted)">
                <span className="sb-num font-bold text-(--sb-text)">
                  {streak.recentResults.filter((r) => r === "WIN").length}
                </span>{" "}
                von <span className="sb-num font-bold text-(--sb-text)">{streak.recentResults.length}</span> letzten
                Matches gewonnen
              </p>
            </Panel>
          </div>
        </>
      ) : (
        <FriendlyState icon={Hourglass} title="Noch nicht genug Matches">
          Für {player.name} sind noch keine Ergebnisse erfasst. Sobald die ersten Medenspiele eingetragen sind,
          stehen hier Bilanz, Tie-Break-Quote und aktuelle Form.
          <Link
            href="/scoreboard"
            className="sb-label mt-3 flex w-fit items-center gap-1.5 text-[11px] text-(--sb-amber) hover:underline"
          >
            <Search className="size-3.5" aria-hidden />
            Anderen Spieler suchen
          </Link>
        </FriendlyState>
      )}

      <CompareSection player={player} query={vsQuery} />
    </div>
  );
}
