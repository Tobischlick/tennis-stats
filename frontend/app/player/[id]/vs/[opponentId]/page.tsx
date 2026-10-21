import Link from "next/link";
import { notFound } from "next/navigation";
import { ArrowLeft, ArrowLeftRight, Handshake } from "lucide-react";
import { Initials } from "@/components/player-list";
import { getHeadToHead, type PlayerSummary } from "@/lib/api-client";

export default async function HeadToHeadPage({ params }: PageProps<"/player/[id]/vs/[opponentId]">) {
  const { id, opponentId } = await params;
  if (id === opponentId) notFound();
  const h2h = await getHeadToHead(id, opponentId);
  if (!h2h) notFound();

  const { player, opponent, record } = h2h;

  return (
    <div>
      <div className="flex items-center justify-between gap-3">
        <Link
          href={`/player/${player.id}`}
          className="inline-flex min-w-0 items-center gap-1.5 text-sm text-muted-foreground transition-colors hover:text-foreground"
        >
          <ArrowLeft aria-hidden className="size-4 shrink-0" />
          <span className="truncate">{player.name}</span>
        </Link>
        <Link
          href={`/player/${opponent.id}/vs/${player.id}`}
          className="inline-flex shrink-0 items-center gap-1.5 rounded-full border border-border bg-card px-3 py-1.5 text-sm transition-colors hover:border-primary/40 hover:text-primary"
        >
          <ArrowLeftRight aria-hidden className="size-3.5" />
          Seiten tauschen
        </Link>
      </div>

      <h1 className="mt-8 text-center text-sm font-medium text-muted-foreground md:mt-10">Direkter Vergleich</h1>

      <section className="mt-6 grid grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] items-start gap-3 md:gap-8">
        <PlayerSide player={player} />
        <div className="flex h-14 items-center md:h-20">
          {record ? (
            <p
              aria-label={`${record.wins} zu ${record.losses}`}
              className="text-4xl font-semibold tracking-tight whitespace-nowrap md:text-7xl"
            >
              <span className={record.wins >= record.losses ? "text-primary" : undefined}>{record.wins}</span>
              <span className="mx-1 text-muted-foreground/50 md:mx-3">:</span>
              <span className={record.losses >= record.wins ? "text-primary" : undefined}>{record.losses}</span>
            </p>
          ) : (
            <p className="text-xl font-medium text-muted-foreground md:text-3xl">vs</p>
          )}
        </div>
        <PlayerSide player={opponent} />
      </section>

      {record ? (
        <>
          <p className="mt-8 text-center text-lg text-balance md:mt-10 md:text-xl">
            {leaderLine(player.name, opponent.name, record.wins, record.losses, h2h.matchesPlayed)}
          </p>

          <section className="mx-auto mt-10 max-w-2xl md:mt-12">
            <div className="flex items-baseline justify-between gap-3">
              <h2 className="text-sm font-medium text-muted-foreground">Letzte Duelle</h2>
              <span className="text-xs text-muted-foreground">Neuestes oben</span>
            </div>
            <ol className="mt-3 divide-y divide-border overflow-hidden rounded-3xl border border-border bg-card">
              {h2h.recentResults.map((result, i) => (
                <li key={i} className="flex items-center gap-4 px-4 py-4 md:px-5">
                  <span
                    aria-hidden
                    className={`size-3 shrink-0 rounded-full ${
                      result === "WIN" ? "bg-primary" : "border-2 border-foreground/20"
                    }`}
                  />
                  <span className="min-w-0 flex-1 break-words">
                    <span className="font-medium">{result === "WIN" ? player.name : opponent.name}</span>
                    <span className="text-muted-foreground"> gewinnt</span>
                  </span>
                  {i === 0 && (
                    <span className="shrink-0 rounded-full bg-secondary px-2.5 py-0.5 text-xs text-muted-foreground">
                      Zuletzt
                    </span>
                  )}
                </li>
              ))}
            </ol>
          </section>
        </>
      ) : (
        <section className="mx-auto mt-10 max-w-xl rounded-3xl border border-border bg-card px-5 py-10 text-center md:px-8 md:py-12">
          <span className="mx-auto flex size-14 items-center justify-center rounded-full bg-primary-soft">
            <Handshake aria-hidden className="size-6 text-primary" />
          </span>
          <h2 className="mt-5 text-2xl font-semibold tracking-tight">Noch kein direktes Duell</h2>
          <p className="mx-auto mt-3 max-w-sm text-balance text-muted-foreground">
            {player.name} und {opponent.name} haben noch nicht gegeneinander gespielt. Die Einzelstatistiken helfen
            trotzdem bei der Einschätzung.
          </p>
          <div className="mt-6 flex flex-col justify-center gap-2 sm:flex-row">
            <StatsLink player={player} />
            <StatsLink player={opponent} />
          </div>
        </section>
      )}
    </div>
  );
}

function PlayerSide({ player }: { player: PlayerSummary }) {
  return (
    <Link href={`/player/${player.id}`} className="group flex min-w-0 flex-col items-center text-center">
      <Initials name={player.name} size="lg" />
      <span className="mt-3 max-w-full leading-tight font-semibold break-words transition-colors group-hover:text-primary md:text-xl">
        {player.name}
      </span>
      <span className="mt-1 max-w-full truncate text-xs text-muted-foreground md:text-sm">
        {player.club ?? "Kein Verein hinterlegt"}
      </span>
    </Link>
  );
}

function StatsLink({ player }: { player: PlayerSummary }) {
  return (
    <Link
      href={`/player/${player.id}`}
      className="inline-flex h-11 min-w-0 items-center justify-center rounded-xl border border-border px-5 text-sm font-medium transition-colors hover:border-primary/40 hover:text-primary"
    >
      <span className="truncate">Statistik {player.name}</span>
    </Link>
  );
}

function leaderLine(player: string, opponent: string, wins: number, losses: number, played: number): string {
  const duels = played === 1 ? "1 Duell" : `${played} Duellen`;
  if (wins === losses) return `Ausgeglichen nach ${duels}.`;
  return `${wins > losses ? player : opponent} führt nach ${duels}.`;
}
