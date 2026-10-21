import Link from "next/link";
import { UserX } from "lucide-react";

export default function PlayerNotFound() {
  return (
    <section className="mt-16 flex flex-col items-center text-center">
      <span className="flex size-14 items-center justify-center rounded-full bg-secondary">
        <UserX aria-hidden className="size-6 text-muted-foreground" />
      </span>
      <h1 className="mt-5 text-xl font-semibold">Spieler nicht gefunden</h1>
      <p className="mt-2 max-w-sm text-balance text-muted-foreground">
        Unter dieser Adresse gibt es keinen Spieler. Vielleicht hilft eine neue Suche.
      </p>
      <Link
        href="/"
        className="mt-6 inline-flex h-11 items-center rounded-xl border border-border px-5 text-sm font-medium transition-colors hover:bg-secondary"
      >
        Zur Suche
      </Link>
    </section>
  );
}
