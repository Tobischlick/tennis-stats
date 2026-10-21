import Link from "next/link";

const tones = {
  ink: "bg-(--court-ink) text-white hover:bg-(--court-clay-dark)",
  ball: "bg-(--court-ball) text-(--court-ink) hover:brightness-95",
  outline: "bg-transparent text-(--court-ink) ring-2 ring-(--court-ink)/15 hover:ring-(--court-ink)/40",
  onDark: "bg-white/10 text-white ring-1 ring-white/30 hover:bg-white/20",
};

export function PillLink({
  href,
  tone = "ink",
  children,
}: {
  href: string;
  tone?: keyof typeof tones;
  children: React.ReactNode;
}) {
  return (
    <Link
      href={href}
      className={`inline-flex h-11 items-center gap-2 rounded-full px-5 text-sm font-semibold transition ${tones[tone]}`}
    >
      {children}
    </Link>
  );
}
