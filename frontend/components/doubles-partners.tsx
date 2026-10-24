import Link from "next/link";
import { ChevronRight } from "lucide-react";
import { Initials } from "@/components/player-list";
import { StatCard } from "@/components/stat-card";
import { formatPercent, isSmallSample, matchCount } from "@/lib/format";
import type { DoublesPartner, DoublesPartners } from "@/lib/api-client/types";

/**
 * The partner with the best win percentage among those with more than a small sample of matches
 * together, the one with more matches on a tie; `undefined` if no partner qualifies or none was won with.
 */
export function bestPairing(partners: DoublesPartner[]): DoublesPartner | undefined {
  let best: DoublesPartner | undefined;
  for (const p of partners) {
    if (isSmallSample(p.matchesPlayed) || p.record.wins === 0) continue;
    if (
      !best ||
      p.record.winPercentage > best.record.winPercentage ||
      (p.record.winPercentage === best.record.winPercentage && p.matchesPlayed > best.matchesPlayed)
    ) {
      best = p;
    }
  }
  return best;
}

export function DoublesPartnersCard({ doublesPartners }: { doublesPartners: DoublesPartners }) {
  const { partners } = doublesPartners;
  const best = bestPairing(partners);

  return (
    <StatCard label="Doppelpartner" className="md:col-span-2">
      {doublesPartners.hasEnoughData && partners.length > 0 ? (
        <>
          <ul className="-mx-2 divide-y divide-border">
            {partners.map((p) => (
              <li key={p.partner.id}>
                <Link
                  href={`/player/${p.partner.id}`}
                  className={`group flex items-center gap-3 rounded-2xl px-2 py-3.5 transition-colors hover:bg-secondary focus-visible:bg-secondary focus-visible:outline-none md:gap-4 ${
                    p === best ? "bg-primary-soft" : ""
                  }`}
                >
                  <Initials name={p.partner.name} />
                  <span className="min-w-0 flex-1">
                    <span className="flex items-center gap-2">
                      <span className="truncate font-medium">{p.partner.name}</span>
                      {p === best && (
                        <span className="shrink-0 rounded-full bg-primary px-2 py-0.5 text-xs font-medium text-primary-foreground">
                          Beste Paarung
                        </span>
                      )}
                    </span>
                    <span className="block truncate text-xs text-muted-foreground">
                      {matchCount(p.matchesPlayed)} zusammen
                      {p.partner.club && <span className="hidden sm:inline"> · {p.partner.club}</span>}
                    </span>
                  </span>
                  <span className="shrink-0 text-right tabular-nums">
                    <span className="block font-medium">
                      <span className="sr-only">Bilanz </span>
                      {p.record.wins}:{p.record.losses}
                    </span>
                    <span className="block text-xs text-muted-foreground">
                      <span className="sr-only">, Siegquote </span>
                      {formatPercent(p.record.winPercentage)}
                    </span>
                  </span>
                  <ChevronRight
                    aria-hidden
                    className="size-5 shrink-0 text-muted-foreground/60 transition-transform group-hover:translate-x-0.5 group-hover:text-primary"
                  />
                </Link>
              </li>
            ))}
          </ul>
          {!best && (
            <p className="mt-4 text-sm text-muted-foreground">
              Die beste Paarung wird hervorgehoben, sobald ein Duo genug Matches zusammen gespielt hat.
            </p>
          )}
        </>
      ) : (
        <>
          <p className="font-medium">Noch kein Doppel erfasst</p>
          <p className="mt-1 text-sm text-muted-foreground">Hier erscheint die Bilanz mit jedem Doppelpartner.</p>
        </>
      )}
    </StatCard>
  );
}
