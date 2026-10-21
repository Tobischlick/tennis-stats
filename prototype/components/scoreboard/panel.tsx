export function Panel({
  label,
  aside,
  children,
  className = "",
}: {
  label: string;
  aside?: React.ReactNode;
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <section className={`rounded-md border border-(--sb-line) bg-(--sb-panel) ${className}`}>
      <header className="flex items-center justify-between gap-2 border-b border-(--sb-line) px-4 py-2">
        <h2 className="sb-label text-[11px] text-(--sb-muted)">{label}</h2>
        {aside}
      </header>
      <div className="p-4">{children}</div>
    </section>
  );
}
