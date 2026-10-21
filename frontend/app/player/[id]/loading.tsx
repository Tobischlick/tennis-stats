import { ArrowLeft } from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";

export default function PlayerLoading() {
  return (
    <div>
      <p role="status" className="sr-only">
        Statistik wird geladen …
      </p>
      <span className="inline-flex items-center gap-1.5 text-sm text-muted-foreground">
        <ArrowLeft aria-hidden className="size-4" /> Suche
      </span>

      <header className="mt-6 flex items-center gap-4 md:mt-8 md:gap-6">
        <Skeleton className="size-14 shrink-0 rounded-full md:size-20" />
        <div className="min-w-0 flex-1">
          <Skeleton className="h-8 w-56 max-w-full md:h-12 md:w-80" />
          <Skeleton className="mt-2 h-5 w-40 max-w-full md:w-56" />
        </div>
      </header>

      <div className="mt-10 max-w-2xl space-y-3 md:mt-12">
        <Skeleton className="h-6 w-full md:h-7" />
        <Skeleton className="h-6 w-2/3 md:h-7" />
      </div>

      <div className="mt-8 grid gap-4 md:grid-cols-2 md:gap-5">
        <CardSkeleton />
        <CardSkeleton />
        <CardSkeleton className="md:col-span-2" />
      </div>
    </div>
  );
}

function CardSkeleton({ className = "" }: { className?: string }) {
  return (
    <div className={`rounded-3xl border border-border bg-card p-6 md:p-7 ${className}`}>
      <Skeleton className="h-4 w-28" />
      <Skeleton className="mt-5 h-12 w-24 md:h-14" />
      <Skeleton className="mt-5 h-1.5 w-full rounded-full" />
      <Skeleton className="mt-4 h-4 w-40 max-w-full" />
    </div>
  );
}
