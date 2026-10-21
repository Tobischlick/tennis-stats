"use client";

import { useEffect } from "react";
import Link from "next/link";
import { CloudOff } from "lucide-react";
import { Button } from "@/components/ui/button";

export default function LoadError({ error, retry }: { error: Error & { digest?: string }; retry: () => void }) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <section className="mt-16 flex flex-col items-center text-center">
      <span className="flex size-14 items-center justify-center rounded-full bg-secondary">
        <CloudOff aria-hidden className="size-6 text-muted-foreground" />
      </span>
      <h1 className="mt-5 text-xl font-semibold">Daten gerade nicht erreichbar</h1>
      <p className="mt-2 max-w-sm text-balance text-muted-foreground">
        Wir kommen im Moment nicht an die Spielerdaten. Das liegt nicht an dir – versuch es in ein paar Sekunden noch
        einmal.
      </p>
      <div className="mt-6 flex flex-col gap-2 sm:flex-row">
        <Button onClick={() => retry()} className="h-11 rounded-xl px-5 hover:bg-primary/90">
          Erneut versuchen
        </Button>
        <Link
          href="/"
          className="inline-flex h-11 items-center justify-center rounded-xl border border-border px-5 text-sm font-medium transition-colors hover:bg-secondary"
        >
          Zur Suche
        </Link>
      </div>
    </section>
  );
}
