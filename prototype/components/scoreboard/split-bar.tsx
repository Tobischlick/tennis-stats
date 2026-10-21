export function SplitBar({ percent, label }: { percent: number; label: string }) {
  return (
    <div
      role="meter"
      aria-label={label}
      aria-valuenow={percent}
      aria-valuemin={0}
      aria-valuemax={100}
      className="relative flex h-2 w-full overflow-hidden rounded-full bg-(--sb-loss)/45"
    >
      <div className="h-full rounded-l-full bg-(--sb-win)" style={{ width: `${percent}%` }} />
      <div className="absolute inset-y-0 left-1/2 w-px bg-(--sb-bg)" aria-hidden />
    </div>
  );
}
