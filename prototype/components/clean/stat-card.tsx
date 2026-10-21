export function StatCard({
  label,
  children,
  className = "",
}: {
  label: string;
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <section className={`rounded-3xl border border-border bg-card p-6 md:p-7 ${className}`}>
      <h2 className="text-sm font-medium text-muted-foreground">{label}</h2>
      <div className="mt-4">{children}</div>
    </section>
  );
}

export function BigNumber({ children, compact }: { children: React.ReactNode; compact?: boolean }) {
  return (
    <p className={`font-semibold tracking-tight ${compact ? "text-3xl sm:text-5xl md:text-6xl" : "text-5xl md:text-6xl"}`}>
      {children}
    </p>
  );
}

export function Meter({ value }: { value: number }) {
  return (
    <div className="h-1.5 w-full overflow-hidden rounded-full bg-secondary" aria-hidden>
      <div className="h-full rounded-full bg-primary" style={{ width: `${value}%` }} />
    </div>
  );
}
