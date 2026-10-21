const percent = new Intl.NumberFormat("de-DE", { maximumFractionDigits: 0 });

export function formatPercent(value: number): string {
  return `${percent.format(value)} %`;
}

export function initials(name: string): string {
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("");
}

export function firstName(name: string): string {
  return name.split(/\s+/)[0] ?? name;
}
