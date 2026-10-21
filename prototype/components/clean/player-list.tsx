import Link from "next/link";
import { ChevronRight } from "lucide-react";
import type { PlayerSummary } from "@/lib/mock/api";

export function PlayerList({ players, hrefFor }: { players: PlayerSummary[]; hrefFor: (p: PlayerSummary) => string }) {
  return (
    <ul className="divide-y divide-border overflow-hidden rounded-2xl border border-border bg-card">
      {players.map((p) => (
        <li key={p.id}>
          <Link
            href={hrefFor(p)}
            className="group flex items-center gap-4 px-5 py-4 transition-colors hover:bg-secondary"
          >
            <Initials name={p.name} />
            <span className="min-w-0 flex-1">
              <span className="block truncate font-medium">{p.name}</span>
              <span className="block truncate text-sm text-muted-foreground">{p.club ?? "Kein Verein hinterlegt"}</span>
            </span>
            <ChevronRight
              aria-hidden
              className="size-5 shrink-0 text-muted-foreground/60 transition-transform group-hover:translate-x-0.5 group-hover:text-primary"
            />
          </Link>
        </li>
      ))}
    </ul>
  );
}

export function Initials({ name, size = "md" }: { name: string; size?: "md" | "lg" }) {
  const initials = name
    .split(" ")
    .map((part) => part[0])
    .slice(0, 2)
    .join("");
  return (
    <span
      aria-hidden
      className={`flex shrink-0 items-center justify-center rounded-full bg-secondary font-semibold text-muted-foreground ${
        size === "lg" ? "size-16 text-lg md:size-20 md:text-xl" : "size-10 text-sm"
      }`}
    >
      {initials}
    </span>
  );
}
