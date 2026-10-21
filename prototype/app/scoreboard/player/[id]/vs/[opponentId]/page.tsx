import Link from "next/link";
import { notFound } from "next/navigation";
import { ArrowLeftRight, Swords } from "lucide-react";
import { getHeadToHead, getPlayer, type PlayerSummary } from "@/lib/mock/api";
import { FriendlyState } from "@/components/scoreboard/friendly-state";
import { ResultChip } from "@/components/scoreboard/result-chip";

function ScoreRow({ player, score, leading }: { player: PlayerSummary; score: number | null; leading: boolean }) {
  return (
    <Link
      href={`/scoreboard/player/${player.id}`}
      className="flex items-center gap-3 px-4 py-3 transition-colors hover:bg-(--sb-panel-2)"
    >
      <span
        className={`h-10 w-1 shrink-0 rounded-full ${leading ? "sb-led bg-(--sb-win) text-(--sb-win)" : "bg-(--sb-line)"}`}
        aria-hidden
      />
      <div className="min-w-0 flex-1">
        <div className="sb-display truncate text-2xl leading-tight font-bold md:text-3xl">{player.name}</div>
        <div className="truncate text-sm text-(--sb-muted)">{player.club ?? "Kein Verein angegeben"}</div>
      </div>
      <div
        className={`sb-num w-14 shrink-0 text-right text-5xl leading-none font-extrabold md:w-20 md:text-6xl ${
          score === null ? "text-(--sb-muted)" : leading ? "text-(--sb-win)" : "text-(--sb-text)"
        }`}
      >
        {score ?? "–"}
      </div>
    </Link>
  );
}

export default async function ScoreboardHeadToHeadPage({
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
  const wins = record?.wins ?? null;
  const losses = record?.losses ?? null;
  const summary = !record
    ? null
    : record.wins === record.losses
      ? `Ausgeglichen – ${record.wins}:${record.losses} nach ${h2h.matchesPlayed} Duellen`
      : `${record.wins > record.losses ? player.name : opponent.name} führt ${Math.max(record.wins, record.losses)}:${Math.min(record.wins, record.losses)} im direkten Vergleich`;

  return (
    <div className="mx-auto max-w-3xl space-y-4">
      <div className="flex items-center justify-between gap-2">
        <h1 className="sb-label flex items-center gap-2 text-xs text-(--sb-muted)">
          <Swords className="size-4 text-(--sb-amber)" aria-hidden />
          Head-to-Head
        </h1>
        <Link
          href={`/scoreboard/player/${opponent.id}/vs/${player.id}`}
          className="sb-label flex items-center gap-1.5 rounded-md border border-(--sb-line) px-2.5 py-1.5 text-[11px] text-(--sb-muted) hover:border-(--sb-amber) hover:text-(--sb-text)"
        >
          <ArrowLeftRight className="size-3.5" aria-hidden />
          Perspektive tauschen
        </Link>
      </div>

      <section
        aria-label="Direkter Vergleich"
        className="sb-scanlines divide-y divide-(--sb-line) overflow-hidden rounded-md border border-(--sb-line) bg-(--sb-panel)"
      >
        <ScoreRow player={player} score={wins} leading={wins !== null && losses !== null && wins > losses} />
        <ScoreRow player={opponent} score={losses} leading={wins !== null && losses !== null && losses > wins} />
      </section>
      {summary && <p className="text-center text-sm text-(--sb-muted)">{summary}</p>}

      {h2h.hasEnoughData ? (
        <section className="rounded-md border border-(--sb-line) bg-(--sb-panel)">
          <header className="flex items-center justify-between border-b border-(--sb-line) px-4 py-2">
            <h2 className="sb-label text-[11px] text-(--sb-muted)">Letzte Duelle</h2>
            <span className="sb-label text-[10px] text-(--sb-muted)">Neuestes zuerst</span>
          </header>
          <ol className="divide-y divide-(--sb-line)">
            {h2h.recentResults.map((r, i) => {
              const winner = r === "WIN" ? player : opponent;
              return (
                <li key={i} className="flex items-center gap-3 px-4 py-2.5">
                  <span className="sb-num w-6 shrink-0 text-xs text-(--sb-muted)">#{h2h.recentResults.length - i}</span>
                  <ResultChip result={r} />
                  <span className="min-w-0 flex-1 truncate text-sm">
                    Sieg für <span className="font-semibold">{winner.name}</span>
                  </span>
                  {i === 0 && (
                    <span className="sb-label shrink-0 text-[10px] text-(--sb-amber)">Zuletzt</span>
                  )}
                </li>
              );
            })}
          </ol>
          <p className="border-t border-(--sb-line) px-4 py-2 text-xs text-(--sb-muted)">
            S / N aus Sicht von {player.name}
          </p>
        </section>
      ) : (
        <FriendlyState icon={Swords} title="Noch kein direktes Duell">
          {player.name} und {opponent.name} sind in den erfassten Matches noch nicht aufeinandergetroffen. Das erste
          Duell steht also noch aus – ein Blick auf die Einzelstatistiken hilft trotzdem weiter.
        </FriendlyState>
      )}

      <div className="grid gap-2 sm:grid-cols-2">
        {[player, opponent].map((p) => (
          <Link
            key={p.id}
            href={`/scoreboard/player/${p.id}`}
            className="sb-label truncate rounded-md border border-(--sb-line) bg-(--sb-panel) px-4 py-3 text-center text-xs hover:border-(--sb-amber)"
          >
            Statistik {p.name} →
          </Link>
        ))}
      </div>
    </div>
  );
}
