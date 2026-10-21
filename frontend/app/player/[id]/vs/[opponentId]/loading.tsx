import { Skeleton } from "@/components/ui/skeleton";

export default function HeadToHeadLoading() {
  return (
    <div>
      <p role="status" className="sr-only">
        Direkter Vergleich wird geladen …
      </p>
      <div className="flex items-center justify-between gap-3">
        <Skeleton className="h-5 w-32" />
        <Skeleton className="h-8 w-36 rounded-full" />
      </div>

      <h1 className="mt-8 text-center text-sm font-medium text-muted-foreground md:mt-10">Direkter Vergleich</h1>

      <section className="mt-6 grid grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] items-start gap-3 md:gap-8">
        <SideSkeleton />
        <div className="flex h-14 items-center md:h-20">
          <Skeleton className="h-10 w-20 md:h-16 md:w-36" />
        </div>
        <SideSkeleton />
      </section>

      <div className="mx-auto mt-10 max-w-2xl md:mt-12">
        <Skeleton className="h-4 w-28" />
        <div className="mt-3 divide-y divide-border overflow-hidden rounded-3xl border border-border bg-card">
          {[0, 1, 2].map((i) => (
            <div key={i} className="flex items-center gap-4 px-4 py-4 md:px-5">
              <Skeleton className="size-3 shrink-0 rounded-full" />
              <Skeleton className="h-5 w-48 max-w-full" />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function SideSkeleton() {
  return (
    <div className="flex min-w-0 flex-col items-center">
      <Skeleton className="size-14 rounded-full md:size-20" />
      <Skeleton className="mt-3 h-5 w-24 max-w-full md:h-6 md:w-36" />
      <Skeleton className="mt-2 h-3 w-20 max-w-full md:h-4 md:w-28" />
    </div>
  );
}
