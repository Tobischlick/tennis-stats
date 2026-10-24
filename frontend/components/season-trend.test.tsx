import { afterEach, describe, expect, test } from "vitest";
import { cleanup, render, screen, within } from "@testing-library/react";
import type { PlayerSeason } from "@/lib/api-client/types";
import { SeasonTrend } from "./season-trend";

afterEach(cleanup);

function season(name: string, wins: number, losses: number, tieBreaks: [number, number] = [0, 0]): PlayerSeason {
  const played = wins + losses;
  const [tieBreaksWon, tieBreaksPlayed] = tieBreaks;
  return {
    season: name,
    type: name.startsWith("Sommer") ? "SUMMER" : "WINTER",
    startsOn: "2025-10-01",
    endsOn: "2026-03-31",
    matchesPlayed: played,
    record: { wins, losses, winPercentage: played ? Math.round((1000 * wins) / played) / 10 : 0 },
    tieBreak: {
      tieBreaksPlayed,
      tieBreaksWon,
      tieBreakWinPercentage: tieBreaksPlayed ? (100 * tieBreaksWon) / tieBreaksPlayed : 0,
    },
  };
}

function rows() {
  return within(screen.getByRole("list", { name: "Saisons, älteste zuerst" })).getAllByRole("listitem");
}

describe("SeasonTrend", () => {
  test("lists every season oldest first with its counts", () => {
    render(<SeasonTrend seasons={[season("Winter 2025/26", 4, 2, [1, 3]), season("Sommer 2026", 6, 1)]} />);
    const [winter, summer] = rows();
    expect(within(winter).getByText("Winter 2025/26")).toBeDefined();
    expect(within(winter).getByText("67 %")).toBeDefined();
    expect(within(winter).getByText(/6 Matches · 4:2/)).toBeDefined();
    expect(within(winter).getByText(/1 von 3 Tie-Breaks gewonnen/)).toBeDefined();
    expect(within(summer).getByText("Sommer 2026")).toBeDefined();
    expect(within(summer).getByText("86 %")).toBeDefined();
    expect(within(summer).queryByText(/Tie-Breaks/)).toBeNull();
  });

  test("draws a trend line across several seasons", () => {
    const { container } = render(
      <SeasonTrend seasons={[season("Sommer 2025", 5, 5), season("Winter 2025/26", 4, 2), season("Sommer 2026", 6, 1)]} />,
    );
    expect(container.querySelector("polyline")?.getAttribute("points")).toBe("0,50 50,33.3 100,14.3");
    expect(screen.queryByText(/der Verlauf erscheint/)).toBeNull();
  });

  test("marks seasons with few matches as small samples", () => {
    const { container } = render(<SeasonTrend seasons={[season("Winter 2025/26", 2, 1), season("Sommer 2026", 6, 1)]} />);
    const [winter, summer] = rows();
    expect(within(winter).getByText("kleine Stichprobe")).toBeDefined();
    expect(within(summer).queryByText("kleine Stichprobe")).toBeNull();
    expect(container.querySelectorAll("[data-small-sample]")).toHaveLength(1);
  });

  test("shows a single season without a trend line", () => {
    const { container } = render(<SeasonTrend seasons={[season("Sommer 2026", 6, 1)]} />);
    expect(container.querySelector("svg")).toBeNull();
    expect(screen.getByText("Erst eine Saison mit Matches erfasst – der Verlauf erscheint ab der zweiten Saison.")).toBeDefined();
    expect(rows()).toHaveLength(1);
  });

  test("leaves a season without a decided match out of the trend", () => {
    const { container } = render(
      <SeasonTrend seasons={[season("Sommer 2026", 6, 1), season("Winter 2026/27", 0, 0, [1, 1])]} />,
    );
    expect(container.querySelector("svg")).toBeNull();
    const [, winter] = rows();
    expect(within(winter).getByText("–")).toBeDefined();
    expect(within(winter).getByText(/0 Matches · 0:0 · 1 von 1 Tie-Breaks gewonnen/)).toBeDefined();
  });
});
