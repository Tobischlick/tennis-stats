import Link from "next/link";
import { notFound } from "next/navigation";
import { ArrowLeftRight, ArrowRight, Swords } from "lucide-react";
import { getHeadToHead, getPlayer, type PlayerSummary } from "@/lib/mock/api";
import { CourtLines } from "@/components/court/court-lines";
import { FriendlyEmpty } from "@/components/court/friendly-empty";
import { PillLink } from "@/components/court/pill-link";

function Side({ player, align, texture }: { player: PlayerSummary; align: "left" | "right"; texture: string }) {
  return (
    <div className={`${texture} relative overflow-hidden px-4 pt-8 pb-14 md:px-10 md:pt-12 md:pb-20`}>
      <CourtLines className="opacity-60" />
      <div className={`relative ${align === "right" ? "text-right" : ""}`}>
        <p className="truncate text-xs font-semibold tracking-wider text-white/75 uppercase">
          {player.club ?? "Verein unbekannt"}
        </p>
        <h1 className="court-display mt-2 text-4xl font-extrabold break-words md:text-7xl">{player.name}</h1>
        <Link
          href={`/court/player/${player.id}`}
          className="mt-4 inline-flex items-center gap-1 text-sm font-semibold text-(--court-ball) hover:underline"
        >
          Statistik <ArrowRight className="size-4" />
        </Link>
      </div>
    </div>
  );
}

export default async function CourtHeadToHeadPage({
  params,
}: {
  params: Promise<{ id: string; opponentId: string }>;
}) {
  const { id, opponentId } = await params;
  const player = getPlayer(id);
  const opponent = getPlayer(opponentId);
  const h2h = getHeadToHead(id, opponentId);
  if (!player || !opponent || !h2h) notFound();

  const record = h2h.hasEnoughData ? h2h.record : null;
  const headline = !record
    ? null
    : record.wins > record.losses
      ? `${player.name} führt`
      : record.wins < record.losses
        ? `${opponent.name} führt`
        : "Ausgeglichen";

  return (
    <>
      <section className="relative grid grid-cols-2 text-white">
        <Side player={player} align="left" texture="court-grass-texture" />
        <Side player={opponent} align="right" texture="court-clay-texture" />
        <div className="absolute bottom-0 left-1/2 flex size-16 -translate-x-1/2 translate-y-1/2 items-center justify-center rounded-full bg-(--court-ball) text-(--court-ink) shadow-[0_6px_0_rgba(0,0,0,0.18)] ring-4 ring-(--court-chalk)">
          <span className="court-display text-2xl font-extrabold">vs</span>
        </div>
      </section>

      <div className="mx-auto w-full max-w-3xl px-4 pt-14 pb-10">
        {record ? (
          <>
            <div className="text-center">
              <p className="text-sm font-semibold tracking-widest text-(--court-muted) uppercase">
                Direkte Bilanz · {h2h.matchesPlayed} {h2h.matchesPlayed === 1 ? "Duell" : "Duelle"}
              </p>
              <p className="court-num mt-3 flex items-center justify-center gap-4 text-8xl font-extrabold italic md:text-9xl">
                <span className="text-(--court-grass)">{record.wins}</span>
                <span className="text-(--court-ink)/30">:</span>
                <span className="text-(--court-clay)">{record.losses}</span>
              </p>
              <p className="court-display mt-3 text-3xl font-bold">{headline}</p>
              <p className="mt-1 text-sm text-(--court-muted)">aus Sicht von {player.name}</p>
              <div className="mt-5">
                <PillLink href={`/court/player/${opponent.id}/vs/${player.id}`} tone="outline">
                  <ArrowLeftRight className="size-4" /> Perspektive tauschen
                </PillLink>
              </div>
            </div>

            <h2 className="court-display court-baseline mt-12 text-2xl font-bold">Letzte Duelle</h2>
            <p className="mt-3 text-sm text-(--court-muted)">Neuestes zuerst</p>
            <ol className="mt-4 space-y-2">
              {h2h.recentResults.map((result, i) => {
                const winner = result === "WIN" ? player : opponent;
                const win = result === "WIN";
                return (
                  <li
                    key={i}
                    className="flex items-center gap-4 rounded-2xl bg-(--court-paper) px-4 py-3 shadow-sm ring-1 ring-black/5"
                  >
                    <span className="court-num w-8 shrink-0 text-2xl font-bold text-(--court-muted)">
                      {i + 1}
                    </span>
                    <span className="min-w-0 flex-1 text-sm text-(--court-muted)">
                      {i === 0 ? "Letztes Duell" : `${i + 1}. letztes Duell`}
                    </span>
                    <span
                      className={`truncate rounded-full px-3 py-1 text-sm font-bold ${
                        win ? "bg-(--court-grass) text-white" : "bg-(--court-clay) text-white"
                      }`}
                    >
                      Sieg {winner.name}
                    </span>
                  </li>
                );
              })}
            </ol>
          </>
        ) : (
          <FriendlyEmpty
            title="Noch kein direktes Duell"
            action={
              <>
                <PillLink href={`/court/player/${player.id}#vergleich`} tone="outline">
                  <Swords className="size-4" /> Anderen Gegner wählen
                </PillLink>
                <PillLink href={`/court/player/${opponent.id}`}>
                  Statistik {opponent.name} <ArrowRight className="size-4" />
                </PillLink>
              </>
            }
          >
            {player.name} und {opponent.name} sind in den erfassten BTV-Ergebnissen noch nicht
            gegeneinander angetreten. Die Einzelstatistiken verraten dir trotzdem, wer gerade in Form ist.
          </FriendlyEmpty>
        )}
      </div>
    </>
  );
}
