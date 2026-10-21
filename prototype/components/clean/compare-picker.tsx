"use client";

import { useState } from "react";
import Link from "next/link";
import { ArrowLeftRight } from "lucide-react";
import type { PlayerSummary } from "@/lib/mock/api";

export function ComparePicker({ playerId, playerName, candidates }: { playerId: string; playerName: string; candidates: PlayerSummary[] }) {
  const [query, setQuery] = useState("");
  const q = query.trim().toLowerCase();
  const matches = q ? candidates.filter((p) => p.name.toLowerCase().includes(q)).slice(0, 5) : [];

  return (
    <section className="rounded-3xl bg-secondary/70 p-6 md:p-7">
      <div className="flex items-center gap-2">
        <ArrowLeftRight aria-hidden className="size-4 text-primary" />
        <h2 className="font-medium">Vergleichen mit …</h2>
      </div>
      <p className="mt-1 text-sm text-muted-foreground">
        Sieh dir die direkte Bilanz von {playerName} gegen einen anderen Spieler an.
      </p>
      <input
        type="search"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        autoComplete="off"
        placeholder="Gegner suchen"
        aria-label="Gegner suchen"
        className="mt-4 h-12 w-full rounded-xl border border-input bg-card px-4 text-base transition outline-none placeholder:text-muted-foreground focus-visible:border-primary focus-visible:ring-4 focus-visible:ring-primary/15"
      />
      {q && (
        <ul className="mt-2 overflow-hidden rounded-xl border border-border bg-card">
          {matches.length === 0 ? (
            <li className="px-4 py-3 text-sm text-muted-foreground">Kein Spieler mit diesem Namen gefunden.</li>
          ) : (
            matches.map((p) => (
              <li key={p.id} className="border-b border-border last:border-b-0">
                <Link
                  href={`/clean/player/${playerId}/vs/${p.id}`}
                  className="flex items-baseline justify-between gap-3 px-4 py-3 transition-colors hover:bg-secondary"
                >
                  <span className="truncate font-medium">{p.name}</span>
                  <span className="truncate text-sm text-muted-foreground">{p.club ?? ""}</span>
                </Link>
              </li>
            ))
          )}
        </ul>
      )}
    </section>
  );
}
