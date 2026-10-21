import type { MatchResult } from "@/lib/api-client/types";

export function FormDots({ results }: { results: MatchResult[] }) {
  const wins = results.filter((r) => r === "WIN").length;
  return (
    <div>
      <ol className="flex flex-wrap items-center gap-2" aria-label="Letzte Ergebnisse, neuestes zuerst">
        {results.map((r, i) => (
          <li
            key={i}
            title={r === "WIN" ? "Sieg" : "Niederlage"}
            className={`size-3.5 rounded-full ${
              r === "WIN" ? "bg-primary" : "border-2 border-foreground/15"
            } ${i === 0 ? "ring-2 ring-primary/25 ring-offset-2 ring-offset-card" : ""}`}
          >
            <span className="sr-only">{r === "WIN" ? "Sieg" : "Niederlage"}</span>
          </li>
        ))}
      </ol>
      <div className="mt-3 flex items-center justify-between gap-4 text-xs text-muted-foreground">
        <span>← Neuestes</span>
        <span>
          {wins} von {results.length} gewonnen
        </span>
      </div>
    </div>
  );
}
