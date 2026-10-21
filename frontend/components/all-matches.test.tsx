import { afterEach, describe, expect, test } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import type { RecentMatch } from "@/lib/api-client/types";
import { AllMatches, matchesPageHref } from "./all-matches";

afterEach(cleanup);

function match(playedOn: string, opponent = "Lena Maier"): RecentMatch {
  return {
    playedOn,
    type: "SINGLES",
    result: "WIN",
    opponents: [{ id: "p2", name: opponent, club: null }],
    partner: null,
    ownTeam: "TC Blau-Weiss Karlsruhe 1",
    opponentTeam: "TC Rot-Gelb Heidelberg 1",
    sets: [
      { own: 6, opponent: 3, ownTieBreak: null, opponentTieBreak: null, matchTieBreak: false },
      { own: 6, opponent: 4, ownTieBreak: null, opponentTieBreak: null, matchTieBreak: false },
    ],
  };
}

function renderAllMatches(props: Partial<Parameters<typeof AllMatches>[0]> = {}) {
  return render(
    <AllMatches
      playerId="p1"
      playerName="Anna Beispiel"
      matches={[match("2026-05-10")]}
      total={1}
      page={1}
      pageCount={1}
      {...props}
    />,
  );
}

describe("AllMatches", () => {
  test("shows a single match without a pager", () => {
    renderAllMatches();
    expect(screen.getByText("Lena Maier")).toBeDefined();
    expect(screen.getAllByRole("listitem")).toHaveLength(1);
    expect(screen.queryByRole("navigation")).toBeNull();
  });

  test("groups matches by year, newest first", () => {
    renderAllMatches({
      matches: [match("2026-05-10", "Lena Maier"), match("2025-09-20", "Sara Kurz")],
      total: 2,
    });
    const years = screen.getAllByRole("heading", { level: 2 }).map((h) => h.textContent);
    expect(years).toEqual(["2026", "2025"]);
    expect(screen.getByText("Neuestes oben")).toBeDefined();
  });

  test("shows an empty state for a player without matches", () => {
    renderAllMatches({ matches: [], total: 0 });
    expect(screen.getByText("Noch keine Matches")).toBeDefined();
    expect(screen.getByRole("link", { name: "Zur Statistik" }).getAttribute("href")).toBe("/player/p1");
    expect(screen.queryByRole("list")).toBeNull();
  });

  test("links to newer and older pages in the middle of the list", () => {
    renderAllMatches({ total: 120, page: 2, pageCount: 3 });
    expect(screen.getByText("Seite 2 von 3")).toBeDefined();
    expect(screen.getByRole("link", { name: "Neuere" }).getAttribute("href")).toBe("/player/p1/matches");
    expect(screen.getByRole("link", { name: "Ältere" }).getAttribute("href")).toBe("/player/p1/matches?seite=3");
    expect(screen.queryByText("Neuestes oben")).toBeNull();
  });

  test("has no older link on the last page", () => {
    renderAllMatches({ total: 120, page: 3, pageCount: 3 });
    expect(screen.queryByRole("link", { name: "Ältere" })).toBeNull();
    expect(screen.getByRole("link", { name: "Neuere" }).getAttribute("href")).toBe("/player/p1/matches?seite=2");
  });
});

test("matchesPageHref leaves the page out for the first page", () => {
  expect(matchesPageHref("p1", 1)).toBe("/player/p1/matches");
  expect(matchesPageHref("p1", 4)).toBe("/player/p1/matches?seite=4");
});
