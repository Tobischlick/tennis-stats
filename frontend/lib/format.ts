import type { MatchResult } from "@/lib/api-client/types";

const percent = new Intl.NumberFormat("de-DE", { maximumFractionDigits: 0 });
const shortDate = new Intl.DateTimeFormat("de-DE", { day: "2-digit", month: "2-digit", timeZone: "UTC" });
const fullDate = new Intl.DateTimeFormat("de-DE", { day: "2-digit", month: "2-digit", year: "numeric", timeZone: "UTC" });
const count = new Intl.NumberFormat("de-DE", { maximumFractionDigits: 0 });

export function formatPercent(value: number): string {
  return `${percent.format(value)} %`;
}

export function formatShortDate(isoDate: string): string {
  return shortDate.format(new Date(isoDate));
}

export function formatDate(isoDate: string): string {
  return fullDate.format(new Date(isoDate));
}

export function formatCount(value: number): string {
  return count.format(value);
}

export function firstName(name: string): string {
  return name.split(" ")[0];
}

export function tieBreakVerdict(rate: number): string {
  if (rate < 40) return "Wackelt im Tie-Break";
  if (rate <= 60) return "Im Tie-Break ausgeglichen";
  return "Stark im Tie-Break";
}

export function resultLabel(result: MatchResult, count: number): string {
  if (result === "WIN") return count === 1 ? "Sieg" : "Siege";
  return count === 1 ? "Niederlage" : "Niederlagen";
}

export function matchCount(count: number): string {
  return count === 1 ? "1 Match" : `${count} Matches`;
}

export function isSmallSample(matchesPlayed: number): boolean {
  return matchesPlayed < 5;
}
