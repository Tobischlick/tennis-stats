import type { MatchResult } from "@/lib/mock/api";

const sizes = {
  sm: "size-5 text-[11px] rounded-[3px]",
  md: "size-7 text-sm rounded-[4px]",
  fluid: "aspect-square w-full text-sm rounded-[4px]",
};

export function ResultChip({ result, size = "md" }: { result: MatchResult; size?: keyof typeof sizes }) {
  const win = result === "WIN";
  return (
    <span
      title={win ? "Sieg" : "Niederlage"}
      className={`sb-num inline-flex shrink-0 items-center justify-center font-bold ${sizes[size]} ${
        win
          ? "bg-(--sb-win) text-(--sb-bg)"
          : "border border-(--sb-loss)/60 bg-(--sb-loss-soft) text-(--sb-loss)"
      }`}
    >
      <span aria-hidden>{win ? "S" : "N"}</span>
      <span className="sr-only">{win ? "Sieg" : "Niederlage"}</span>
    </span>
  );
}
