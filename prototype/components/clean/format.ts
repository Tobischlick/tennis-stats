const percent = new Intl.NumberFormat("de-DE", { maximumFractionDigits: 0 });

export function formatPercent(value: number): string {
  return `${percent.format(value)} %`;
}

export function firstName(name: string): string {
  return name.split(" ")[0];
}

export function tieBreakVerdict(rate: number): string {
  if (rate < 40) return "Wackelt im Tie-Break";
  if (rate <= 60) return "Im Tie-Break ausgeglichen";
  return "Stark im Tie-Break";
}
