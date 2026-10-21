import { notFound } from "next/navigation";
import { Info, Search, Swords } from "lucide-react";
import { getPlayer, getPlayerStats } from "@/lib/mock/api";
import { CompareSection } from "@/components/court/compare-section";
import { FormCard } from "@/components/court/form-card";
import { firstName } from "@/components/court/format";
import { FriendlyEmpty } from "@/components/court/friendly-empty";
import { PillLink } from "@/components/court/pill-link";
import { PlayerHero } from "@/components/court/player-hero";
import { TieBreakCard } from "@/components/court/tie-break-card";

const SMALL_SAMPLE = 5;

export default async function CourtPlayerPage({
  params,
  searchParams,
}: {
  params: Promise<{ id: string }>;
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const { id } = await params;
  const { vs } = await searchParams;
  const player = getPlayer(id);
  const stats = getPlayerStats(id);
  if (!player || !stats) notFound();
  const compareQuery = typeof vs === "string" ? vs.trim() : "";

  return (
    <>
      <PlayerHero player={player} stats={stats} />

      <div className="mx-auto w-full max-w-5xl px-4 py-8 md:py-10">
        {stats.hasEnoughData && stats.tieBreak && stats.streak ? (
          <>
            {stats.matchesPlayed < SMALL_SAMPLE ? (
              <p className="mb-6 flex items-start gap-2 rounded-2xl bg-(--court-ball)/40 px-4 py-3 text-sm">
                <Info className="mt-0.5 size-4 shrink-0" />
                <span>
                  Erst {stats.matchesPlayed} Matches erfasst – die Zahlen können sich mit den nächsten
                  Spielen noch stark ändern.
                </span>
              </p>
            ) : null}
            <div className="grid gap-5 md:grid-cols-2">
              <TieBreakCard tieBreak={stats.tieBreak} />
              <FormCard streak={stats.streak} />
            </div>
          </>
        ) : (
          <FriendlyEmpty
            title="Noch nicht genug Matches"
            action={
              <>
                <PillLink href="/court" tone="outline">
                  <Search className="size-4" /> Andere Spieler suchen
                </PillLink>
                <PillLink href="#vergleich">
                  <Swords className="size-4" /> Trotzdem vergleichen
                </PillLink>
              </>
            }
          >
            Für {firstName(player.name)} sind noch keine Ergebnisse erfasst. Sobald die ersten Matches
            im BTV gespielt sind, siehst du hier Bilanz, Tie-Breaks und aktuelle Form.
          </FriendlyEmpty>
        )}
      </div>

      <CompareSection player={player} query={compareQuery} />
    </>
  );
}
