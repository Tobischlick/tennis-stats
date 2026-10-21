"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { ArrowLeftRight, ChevronRight } from "lucide-react";
import { Input } from "@/components/ui/input";
import { searchOpponents } from "@/app/player/[id]/actions";
import type { PlayerSummary } from "@/lib/api-client/types";

const DEBOUNCE_MS = 250;

export function ComparePicker({ playerId, playerName }: { playerId: string; playerName: string }) {
  const [query, setQuery] = useState("");
  const [matches, setMatches] = useState<PlayerSummary[] | null>();
  const q = query.trim();

  useEffect(() => {
    if (!q) return;
    let stale = false;
    const timer = setTimeout(() => {
      searchOpponents(playerId, q).then((players) => {
        if (!stale) setMatches(players);
      });
    }, DEBOUNCE_MS);
    return () => {
      stale = true;
      clearTimeout(timer);
    };
  }, [playerId, q]);

  return (
    <section className="rounded-3xl bg-secondary/70 p-5 md:p-7">
      <div className="flex items-center gap-2">
        <ArrowLeftRight aria-hidden className="size-4 shrink-0 text-primary" />
        <h2 className="font-medium">Vergleichen mit …</h2>
      </div>
      <p className="mt-1 text-sm text-muted-foreground">
        Sieh dir die direkte Bilanz von {playerName} gegen einen anderen Spieler an.
      </p>
      <Input
        type="search"
        value={query}
        onChange={(e) => {
          setQuery(e.target.value);
          if (!e.target.value.trim()) setMatches(undefined);
        }}
        autoComplete="off"
        placeholder="Gegner suchen"
        aria-label="Gegner suchen"
        className="mt-4 h-12 rounded-xl bg-card px-4 text-base focus-visible:border-primary focus-visible:ring-4 focus-visible:ring-primary/15 md:text-base"
      />
      {q && matches !== undefined && (
        <ul className="mt-2 divide-y divide-border overflow-hidden rounded-xl border border-border bg-card">
          {matches === null ? (
            <li className="px-4 py-3 text-sm text-muted-foreground">
              Die Suche ist gerade nicht erreichbar. Versuch es gleich noch einmal.
            </li>
          ) : matches.length === 0 ? (
            <li className="px-4 py-3 text-sm text-muted-foreground">Kein Spieler mit diesem Namen gefunden.</li>
          ) : (
            matches.map((p) => (
              <li key={p.id}>
                <Link
                  href={`/player/${playerId}/vs/${p.id}`}
                  className="group flex items-center gap-3 px-4 py-3 transition-colors hover:bg-secondary focus-visible:bg-secondary focus-visible:outline-none"
                >
                  <span className="min-w-0 flex-1">
                    <span className="block truncate font-medium">{p.name}</span>
                    <span className="block truncate text-sm text-muted-foreground">
                      {p.club ?? "Kein Verein hinterlegt"}
                    </span>
                  </span>
                  <ChevronRight
                    aria-hidden
                    className="size-4 shrink-0 text-muted-foreground/60 group-hover:text-primary"
                  />
                </Link>
              </li>
            ))
          )}
        </ul>
      )}
    </section>
  );
}
