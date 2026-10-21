export function TennisBall({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 32 32" className={className} aria-hidden="true">
      <circle cx="16" cy="16" r="15" fill="var(--court-ball)" />
      <path
        d="M5.5 5.8c5.2 4.6 5.2 15.8 0 20.4M26.5 5.8c-5.2 4.6-5.2 15.8 0 20.4"
        fill="none"
        stroke="#fff"
        strokeWidth="2.2"
        strokeLinecap="round"
      />
    </svg>
  );
}
