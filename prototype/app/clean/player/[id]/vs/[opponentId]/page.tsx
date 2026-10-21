import Link from "next/link";
import { notFound } from "next/navigation";
import { ArrowLeft, ArrowLeftRight, Handshake } from "lucide-react";
import { getHeadToHead, getPlayer } from "@/lib/mock/api";
import { Initials } from "@/components/clean/player-list";
import { Meter } from "@/components/clean/stat-card";
import { firstName } from "@/components/clean/format";

export default async function CleanHeadToHeadPage({
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

  return (
    <div>
      <div className="flex items-center justify-between gap-4">
        <Link
          href={`/clean/player/${id}`}
          className="inline-flex min-w-0 items-center gap-1.5 text-sm text-muted-foreground transition-colors hover:text-foreground"
        >
          <ArrowLeft aria-hidden className="size-4 shrink-0" />
          <span className="truncate">{player.name}</span>
        </Link>
        <Link
          href={`/clean/player/${opponentId}/vs/${id}`}
          className="inline-flex shrink-0 items-center gap-1.5 rounded-full border border-border px-3 py-1.5 text-sm transition-colors hover:bg-secondary"
        >
          <ArrowLeftRight aria-hidden className="size-3.5" /> Seiten tauschen
        </Link>
      </div>

      <p className="mt-8 text-center text-sm font-medium text-muted-foreground md:mt-10">Direkter Vergleich</p>

      <section className="mt-6 grid grid-cols-[1fr_auto_1fr] items-center gap-3 md:gap-8">
        <PlayerSide name={player.name} club={player.club} href={`/clean/player/${id}`} />
        <div className="text-center">
          {record ? (
            <p className="text-5xl font-semibold tracking-tight md:text-7xl">
              <span className={record.wins >= record.losses ? "text-primary" : ""}>{record.wins}</span>
              <span className="mx-1.5 text-muted-foreground/50 md:mx-3">–</span>
              <span className={record.losses >= record.wins ? "text-primary" : ""}>{record.losses}</span>
            </p>
          ) : (
            <p className="text-2xl font-medium text-muted-foreground md:text-3xl">vs</p>
          )}
        </div>
        <PlayerSide name={opponent.name} club={opponent.club} href={`/clean/player/${opponentId}`} />
      </section>

      {record ? (
        <>
          <p className="mt-8 text-center text-lg md:text-xl">{leaderLine(player.name, opponent.name, record.wins, record.losses, h2h.matchesPlayed)}</p>
          <div className="mx-auto mt-5 max-w-md">
            <Meter value={record.winPercentage} />
          </div>

          <section className="mx-auto mt-12 max-w-2xl">
            <div className="flex items-baseline justify-between">
              <h2 className="text-sm font-medium text-muted-foreground">Letzte Duelle</h2>
              <span className="text-xs text-muted-foreground">Neuestes oben</span>
            </div>
            <ol className="mt-3 divide-y divide-border overflow-hidden rounded-2xl border border-border bg-card">
              {h2h.recentResults.map((r, i) => {
                const winner = r === "WIN" ? player : opponent;
                return (
                  <li key={i} className="flex items-center gap-4 px-5 py-4">
                    <span
                      aria-hidden
                      className={`size-3 shrink-0 rounded-full ${
                        r === "WIN" ? "bg-primary" : "border-2 border-foreground/15"
                      }`}
                    />
                    <span className="flex-1">
                      <span className="font-medium">{winner.name}</span>
                      <span className="text-muted-foreground"> gewinnt</span>
                    </span>
                    {i === 0 && (
                      <span className="rounded-full bg-secondary px-2.5 py-0.5 text-xs text-muted-foreground">
                        Zuletzt
                      </span>
                    )}
                  </li>
                );
              })}
            </ol>
          </section>
        </>
      ) : (
        <section className="mx-auto mt-10 max-w-xl rounded-3xl border border-border bg-card px-6 py-12 text-center">
          <span className="mx-auto flex size-14 items-center justify-center rounded-full bg-primary/10">
            <Handshake aria-hidden className="size-6 text-primary" />
          </span>
          <h2 className="mt-5 text-2xl font-semibold tracking-tight">Noch kein direktes Duell</h2>
          <p className="mx-auto mt-3 max-w-sm text-muted-foreground">
            {firstName(player.name)} und {firstName(opponent.name)} haben noch nicht gegeneinander gespielt. Die
            Einzelstatistiken helfen trotzdem bei der Einschätzung.
          </p>
          <div className="mt-6 flex flex-col justify-center gap-2 sm:flex-row">
            <StatsLink href={`/clean/player/${id}`} name={player.name} />
            <StatsLink href={`/clean/player/${opponentId}`} name={opponent.name} />
          </div>
        </section>
      )}
    </div>
  );
}

function PlayerSide({ name, club, href }: { name: string; club?: string | null; href: string }) {
  return (
    <Link href={href} className="group flex min-w-0 flex-col items-center text-center">
      <Initials name={name} size="lg" />
      <span className="mt-3 text-base leading-tight font-semibold group-hover:text-primary md:text-xl">{name}</span>
      <span className="mt-1 hidden text-sm text-muted-foreground md:block">{club ?? "Kein Verein hinterlegt"}</span>
    </Link>
  );
}

function StatsLink({ href, name }: { href: string; name: string }) {
  return (
    <Link
      href={href}
      className="inline-flex h-11 items-center justify-center rounded-xl border border-border px-5 text-sm font-medium transition-colors hover:bg-secondary"
    >
      Statistik {name}
    </Link>
  );
}

function leaderLine(player: string, opponent: string, wins: number, losses: number, played: number): string {
  const duels = played === 1 ? "1 Duell" : `${played} Duellen`;
  if (wins === losses) return `Ausgeglichen nach ${duels}.`;
  const leader = wins > losses ? player : opponent;
  return `${leader} führt nach ${duels}.`;
}
