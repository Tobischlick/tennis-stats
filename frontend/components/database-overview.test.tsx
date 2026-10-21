import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import type { Overview } from "@/lib/api-client/types";
import { DatabaseOverview, DatabaseOverviewSkeleton } from "./database-overview";

const getOverview = vi.hoisted(() => vi.fn<() => Promise<Overview>>());

vi.mock("@/lib/api-client", () => ({ getOverview }));

const crawled: Overview = {
  hasData: true,
  totals: {
    players: 1843,
    clubs: 97,
    teams: 212,
    meetings: 618,
    matches: { total: 5562, singles: 3708, doubles: 1854 },
    sets: 11873,
    firstPlayedOn: "2026-04-25",
    lastPlayedOn: "2026-07-19",
  },
  funStats: {
    tieBreaks: 702,
    matchTieBreaks: 611,
    completedMatches: 5431,
    decidingSetMatches: 774,
    decidingSetPercentage: 14.3,
    mostCommonScoreLine: { scoreLine: "6:4 6:3", count: 241 },
    longestWinStreak: 12,
  },
};

const empty: Overview = {
  hasData: false,
  totals: {
    players: 0,
    clubs: 0,
    teams: 0,
    meetings: 0,
    matches: { total: 0, singles: 0, doubles: 0 },
    sets: 0,
    firstPlayedOn: null,
    lastPlayedOn: null,
  },
  funStats: {
    tieBreaks: 0,
    matchTieBreaks: 0,
    completedMatches: 0,
    decidingSetMatches: 0,
    decidingSetPercentage: null,
    mostCommonScoreLine: null,
    longestWinStreak: 0,
  },
};

async function renderOverview() {
  render(await DatabaseOverview());
}

function figure(label: string): string | null | undefined {
  return screen.getByText(label).previousElementSibling?.textContent;
}

beforeEach(() => {
  getOverview.mockReset();
});

afterEach(cleanup);

describe("DatabaseOverview", () => {
  test("shows the totals with German labels and number format", async () => {
    getOverview.mockResolvedValueOnce(crawled);
    await renderOverview();
    expect(screen.getByRole("region", { name: "Datenbank im Überblick" })).toBeDefined();
    expect(figure("Spieler")).toBe("1.843");
    expect(figure("Vereine")).toBe("97");
    expect(figure("Matches")).toBe("5.562");
    expect(screen.getByText("Ergebnisse vom 25.04.2026 bis 19.07.2026")).toBeDefined();
  });

  test("shows the fun stats", async () => {
    getOverview.mockResolvedValueOnce(crawled);
    await renderOverview();
    expect(figure("Sätze mit 7:6")).toBe("702");
    expect(figure("Match-Tie-Breaks")).toBe("611");
    expect(screen.getByText("Entscheidungssatz").nextElementSibling?.textContent).toBe(
      "14\u00a0%der Matches gingen in den dritten Satz",
    );
    expect(screen.getByText("Häufigstes Ergebnis").nextElementSibling?.textContent).toBe(
      "6:4 6:3241 Matches endeten so",
    );
    expect(screen.getByText("Längste Siegesserie").nextElementSibling?.textContent).toBe(
      "12Siege in Folge von einem Spieler",
    );
  });

  test("leaves out fun stats the backend has no value for", async () => {
    getOverview.mockResolvedValueOnce({
      ...crawled,
      funStats: {
        ...crawled.funStats,
        completedMatches: 0,
        decidingSetMatches: 0,
        decidingSetPercentage: null,
        mostCommonScoreLine: null,
        longestWinStreak: 0,
      },
    });
    await renderOverview();
    expect(screen.getByText("Tie-Breaks")).toBeDefined();
    expect(screen.queryByText("Entscheidungssatz")).toBeNull();
    expect(screen.queryByText("Häufigstes Ergebnis")).toBeNull();
    expect(screen.queryByText("Längste Siegesserie")).toBeNull();
  });

  test("shows a friendly empty state instead of zeros", async () => {
    getOverview.mockResolvedValueOnce(empty);
    await renderOverview();
    expect(screen.getByRole("heading", { name: "Noch keine Daten" })).toBeDefined();
    expect(screen.queryByText("0")).toBeNull();
    expect(screen.queryByText("Spieler")).toBeNull();
  });

  test("shows a quiet note instead of failing when the backend is unreachable", async () => {
    const consoleError = vi.spyOn(console, "error").mockImplementation(() => {});
    getOverview.mockRejectedValueOnce(new Error("getOverview: backend not reachable"));
    await renderOverview();
    expect(screen.getByText("Die Übersicht ist gerade nicht erreichbar.")).toBeDefined();
    expect(screen.queryByRole("region")).toBeNull();
    expect(consoleError).toHaveBeenCalled();
    consoleError.mockRestore();
  });
});

test("DatabaseOverviewSkeleton announces loading to screen readers only", () => {
  render(<DatabaseOverviewSkeleton />);
  expect(screen.getByRole("status").textContent).toBe("Übersicht wird geladen …");
});
