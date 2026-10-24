import { afterEach, describe, expect, test } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import type { DoublesPartner, DoublesPartners } from "@/lib/api-client/types";
import { formatPercent } from "@/lib/format";
import { bestPairing, DoublesPartnersCard } from "./doubles-partners";

afterEach(cleanup);

function partner(id: string, name: string, wins: number, losses: number): DoublesPartner {
  const matchesPlayed = wins + losses;
  return {
    partner: { id, name, club: "TC Blau-Weiss Karlsruhe" },
    matchesPlayed,
    record: { wins, losses, winPercentage: Math.round((1000 * wins) / matchesPlayed) / 10 },
  };
}

function doublesPartners(partners: DoublesPartner[]): DoublesPartners {
  const matchesPlayed = partners.reduce((sum, p) => sum + p.matchesPlayed, 0);
  return {
    playerId: "p1",
    player: { id: "p1", name: "Anna Schmidt", club: "TC Blau-Weiss Karlsruhe" },
    matchesPlayed,
    hasEnoughData: matchesPlayed > 0,
    partners,
  };
}

const clara = partner("p3", "Clara Wolf", 4, 3);
const mia = partner("p4", "Mia Keller", 5, 1);
const lena = partner("p5", "Lena Vogt", 3, 0);

describe("bestPairing", () => {
  test("picks the best win percentage among partners beyond the small sample", () => {
    expect(bestPairing([clara, mia, lena])).toBe(mia);
  });

  test("prefers more matches on a tied win percentage", () => {
    const more = partner("p6", "Eva Engel", 6, 0);
    const fewer = partner("p7", "Ida Iske", 5, 0);
    expect(bestPairing([fewer, more])).toBe(more);
  });

  test("has none when every partner is a small sample or never won", () => {
    expect(bestPairing([lena])).toBeUndefined();
    expect(bestPairing([partner("p8", "Uta Ulm", 0, 6)])).toBeUndefined();
  });
});

describe("DoublesPartnersCard", () => {
  test("lists each partner with their record, linking to their page", () => {
    render(<DoublesPartnersCard doublesPartners={doublesPartners([clara, mia, lena])} />);
    expect(screen.getByRole("heading", { name: "Doppelpartner" })).toBeDefined();
    const links = screen.getAllByRole("link");
    expect(links.map((l) => l.getAttribute("href"))).toEqual(["/player/p3", "/player/p4", "/player/p5"]);
    expect(links[0].textContent).toContain("Clara Wolf");
    expect(links[0].textContent).toContain("7 Matches zusammen");
    expect(links[0].textContent).toContain("Bilanz 4:3");
    expect(links[0].textContent).toContain(formatPercent(57.1));
  });

  test("highlights only the best pairing", () => {
    render(<DoublesPartnersCard doublesPartners={doublesPartners([clara, mia, lena])} />);
    const highlighted = screen.getByText("Beste Paarung");
    expect(highlighted.closest("a")?.getAttribute("href")).toBe("/player/p4");
    expect(screen.getAllByText("Beste Paarung")).toHaveLength(1);
    expect(screen.queryByText(/sobald ein Duo genug Matches/)).toBeNull();
  });

  test("explains the missing highlight when every pairing is a small sample", () => {
    render(<DoublesPartnersCard doublesPartners={doublesPartners([lena])} />);
    expect(screen.queryByText("Beste Paarung")).toBeNull();
    expect(screen.getByText(/sobald ein Duo genug Matches zusammen gespielt hat/)).toBeDefined();
  });

  test("shows an empty state for a player without doubles", () => {
    render(<DoublesPartnersCard doublesPartners={doublesPartners([])} />);
    expect(screen.getByText("Noch kein Doppel erfasst")).toBeDefined();
    expect(screen.queryByRole("link")).toBeNull();
  });
});
