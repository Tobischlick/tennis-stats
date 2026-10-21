import type { MatchResult } from "@/lib/mock/api";

export function FormStrip({ results }: { results: MatchResult[] }) {
  return (
    <div className="max-w-md">
      <ol
        className="grid gap-1 sm:gap-1.5"
        style={{ gridTemplateColumns: `repeat(${results.length}, minmax(0, 2.5rem))` }}
        aria-label="Letzte Ergebnisse, neuestes zuerst"
      >
        {results.map((result, i) => {
          const win = result === "WIN";
          return (
            <li
              key={i}
              title={`${i === 0 ? "Letztes Match" : `${i + 1}. letztes Match`}: ${win ? "Sieg" : "Niederlage"}`}
              className={`court-num flex aspect-square items-center justify-center rounded-full text-sm font-bold sm:text-base ${
                win
                  ? "bg-(--court-grass) text-white"
                  : "bg-(--court-clay-soft) text-(--court-clay-dark) ring-2 ring-(--court-clay)/40 ring-inset"
              } ${i === 0 ? "outline-3 outline-offset-2 outline-(--court-ball)" : ""}`}
            >
              <span aria-hidden="true">{win ? "S" : "N"}</span>
              <span className="sr-only">{win ? "Sieg" : "Niederlage"}</span>
            </li>
          );
        })}
      </ol>
      <div className="mt-2 flex items-center gap-2 text-xs font-medium text-(--court-muted)">
        <span>Neuestes</span>
        <span className="h-px flex-1 bg-current opacity-40" />
        <span aria-hidden="true">→</span>
        <span>Älter</span>
      </div>
    </div>
  );
}
