import type { MatchResult } from "@/lib/mock/api";

export function FormDots({ results, size = "md" }: { results: MatchResult[]; size?: "md" | "lg" }) {
  const dot = size === "lg" ? "size-4" : "size-3.5";
  return (
    <div>
      <ol className="flex flex-wrap items-center gap-1.5" aria-label="Letzte Ergebnisse, neuestes zuerst">
        {results.map((r, i) => (
          <li
            key={i}
            title={r === "WIN" ? "Sieg" : "Niederlage"}
            className={`${dot} rounded-full ${
              r === "WIN" ? "bg-primary" : "border-2 border-foreground/15 bg-transparent"
            } ${i === 0 ? "ring-2 ring-primary/20 ring-offset-2 ring-offset-card" : ""}`}
          >
            <span className="sr-only">{r === "WIN" ? "Sieg" : "Niederlage"}</span>
          </li>
        ))}
      </ol>
      <div className="mt-3 flex items-center justify-between gap-4 text-xs text-muted-foreground">
        <span>← Neuestes</span>
        <span>
          {results.filter((r) => r === "WIN").length} von {results.length} gewonnen
        </span>
      </div>
    </div>
  );
}
