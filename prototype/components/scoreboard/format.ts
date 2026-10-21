const percentFormat = new Intl.NumberFormat("de-DE", { maximumFractionDigits: 1 });

export function formatPercent(value: number): string {
  return `${percentFormat.format(value)} %`;
}
