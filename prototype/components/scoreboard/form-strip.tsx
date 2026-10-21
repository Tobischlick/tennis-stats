import type { MatchResult } from "@/lib/mock/api";
import { ResultChip } from "./result-chip";

export function FormStrip({ results }: { results: MatchResult[] }) {
  return (
    <div>
      <ol className="grid max-w-80 grid-cols-10 gap-1" aria-label="Letzte Ergebnisse, neuestes zuerst">
        {results.map((r, i) => (
          <li key={i} className={i === 0 ? "rounded-[5px] ring-2 ring-(--sb-amber) ring-offset-1 ring-offset-(--sb-panel)" : ""}>
            <ResultChip result={r} size="fluid" />
          </li>
        ))}
      </ol>
      <div className="sb-label mt-2 flex items-center justify-between text-[10px] text-(--sb-muted)">
        <span className="text-(--sb-amber)">▲ Letztes Match</span>
        <span>Älter →</span>
      </div>
    </div>
  );
}
