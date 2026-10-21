import { ArrowLeft } from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";

export default function AllMatchesLoading() {
  return (
    <div>
      <p role="status" className="sr-only">
        Matches werden geladen …
      </p>
      <div className="flex items-center gap-1.5 text-sm text-muted-foreground">
        <ArrowLeft aria-hidden className="size-4" />
        <Skeleton className="h-4 w-32" />
      </div>

      <header className="mt-6 md:mt-8">
        <Skeleton className="h-8 w-56 max-w-full md:h-12 md:w-80" />
        <Skeleton className="mt-2 h-5 w-40 max-w-full md:w-56" />
      </header>

      <div className="mt-8 rounded-3xl border border-border bg-card p-6 md:mt-10 md:p-7">
        <Skeleton className="h-4 w-12" />
        <div className="mt-2 divide-y divide-border">
          {[0, 1, 2, 3, 4].map((i) => (
            <div key={i} className="flex items-center gap-3 py-3.5 md:gap-4">
              <Skeleton className="size-3 shrink-0 rounded-full" />
              <div className="min-w-0 flex-1">
                <Skeleton className="h-5 w-44 max-w-full" />
                <Skeleton className="mt-1.5 h-3 w-32 max-w-full" />
              </div>
              <Skeleton className="h-5 w-20 shrink-0" />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
