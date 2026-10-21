export function CourtLines({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 400 200"
      preserveAspectRatio="xMidYMid slice"
      className={`pointer-events-none absolute inset-0 h-full w-full ${className ?? ""}`}
      aria-hidden="true"
    >
      <g fill="none" stroke="var(--court-line)" strokeWidth="2.5">
        <rect x="20" y="20" width="360" height="160" />
        <line x1="20" y1="40" x2="380" y2="40" />
        <line x1="20" y1="160" x2="380" y2="160" />
        <line x1="110" y1="40" x2="110" y2="160" />
        <line x1="290" y1="40" x2="290" y2="160" />
        <line x1="110" y1="100" x2="290" y2="100" />
        <line x1="200" y1="10" x2="200" y2="190" strokeWidth="4" strokeDasharray="2 5" />
      </g>
    </svg>
  );
}
