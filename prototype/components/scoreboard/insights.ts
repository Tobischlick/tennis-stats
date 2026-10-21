import type { PlayerStats } from "@/lib/mock/api";

export type Tone = "win" | "loss" | "neutral";

export function tieBreakVerdict(tieBreak: NonNullable<PlayerStats["tieBreak"]>): { text: string; tone: Tone } {
  if (tieBreak.tieBreaksPlayed < 3) return { text: "Zu wenige Tie-Breaks für ein Urteil", tone: "neutral" };
  if (tieBreak.tieBreakWinPercentage < 45) return { text: "Wackelt im Tie-Break", tone: "loss" };
  if (tieBreak.tieBreakWinPercentage >= 60) return { text: "Nervenstark im Tie-Break", tone: "win" };
  return { text: "Im Tie-Break ausgeglichen", tone: "neutral" };
}

export function streakText(streak: NonNullable<PlayerStats["streak"]>["current"]): string {
  const win = streak.result === "WIN";
  if (streak.count === 1) return win ? "Letztes Match gewonnen" : "Letztes Match verloren";
  return win ? `${streak.count} Siege in Folge` : `${streak.count} Niederlagen in Folge`;
}

export const toneClass: Record<Tone, string> = {
  win: "border-(--sb-win)/40 bg-(--sb-win-soft) text-(--sb-win)",
  loss: "border-(--sb-loss)/50 bg-(--sb-loss-soft) text-(--sb-loss)",
  neutral: "border-(--sb-line) bg-(--sb-panel-2) text-(--sb-muted)",
};
