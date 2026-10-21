import type { LucideIcon } from "lucide-react";

export function FriendlyState({
  icon: Icon,
  title,
  children,
}: {
  icon: LucideIcon;
  title: string;
  children: React.ReactNode;
}) {
  return (
    <div className="flex items-start gap-4 rounded-md border border-dashed border-(--sb-line) bg-(--sb-panel) p-5">
      <span className="flex size-10 shrink-0 items-center justify-center rounded-full bg-(--sb-panel-2) text-(--sb-amber)">
        <Icon className="size-5" aria-hidden />
      </span>
      <div className="min-w-0">
        <h2 className="sb-display text-xl font-bold">{title}</h2>
        <div className="mt-1 text-sm leading-relaxed text-(--sb-muted)">{children}</div>
      </div>
    </div>
  );
}
