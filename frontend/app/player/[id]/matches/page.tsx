import type { Metadata } from "next";
import Link from "next/link";
import { notFound, redirect } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import { AllMatches, matchesPageHref } from "@/components/all-matches";
import { matchCount } from "@/lib/format";
import { getMatchesPage, getPlayerStats } from "@/lib/api-client";

const PAGE_SIZE = 50;

export async function generateMetadata({ params }: PageProps<"/player/[id]/matches">): Promise<Metadata> {
  const stats = await getPlayerStats((await params).id).catch(() => undefined);
  return { title: stats ? `Alle Matches von ${stats.player.name} – Tennis Stats` : "Tennis Stats" };
}

export default async function AllMatchesPage({ params, searchParams }: PageProps<"/player/[id]/matches">) {
  const { id } = await params;
  const page = parsePage((await searchParams).seite);
  const [stats, result] = await Promise.all([
    getPlayerStats(id),
    getMatchesPage(id, (page - 1) * PAGE_SIZE, PAGE_SIZE),
  ]);
  if (!stats || !result) notFound();
  const pageCount = Math.max(1, Math.ceil(result.total / PAGE_SIZE));
  if (page > pageCount) redirect(matchesPageHref(id, pageCount));
  const { player } = stats;

  return (
    <div>
      <Link
        href={`/player/${id}`}
        className="inline-flex max-w-full items-center gap-1.5 text-sm text-muted-foreground transition-colors hover:text-foreground"
      >
        <ArrowLeft aria-hidden className="size-4 shrink-0" />
        <span className="truncate">{player.name}</span>
      </Link>

      <header className="mt-6 md:mt-8">
        <h1 className="text-3xl font-semibold tracking-tight md:text-5xl">Alle Matches</h1>
        <p className="mt-1 break-words text-muted-foreground md:text-lg">
          {player.name}
          {result.total > 0 && <span> · {matchCount(result.total)}</span>}
        </p>
      </header>

      <AllMatches
        playerId={id}
        playerName={player.name}
        matches={result.matches}
        total={result.total}
        page={page}
        pageCount={pageCount}
      />
    </div>
  );
}

function parsePage(raw: string | string[] | undefined): number {
  const page = Number(Array.isArray(raw) ? raw[0] : raw);
  return Number.isInteger(page) && page >= 1 ? page : 1;
}
