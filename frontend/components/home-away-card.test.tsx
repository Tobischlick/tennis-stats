import { afterEach, describe, expect, test } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import { HomeAwayCard } from "./home-away-card";

afterEach(cleanup);

describe("HomeAwayCard", () => {
  test("shows home and away side by side with their records", () => {
    render(
      <HomeAwayCard
        homeAway={{
          home: { wins: 16, losses: 6, winPercentage: 72.7 },
          away: { wins: 11, losses: 9, winPercentage: 55 },
        }}
      />,
    );
    expect(screen.getByText("Heim")).toBeDefined();
    expect(screen.getByText("73 %")).toBeDefined();
    expect(screen.getByText("16 Siege · 6 Niederlagen")).toBeDefined();
    expect(screen.getByText("Auswärts")).toBeDefined();
    expect(screen.getByText("55 %")).toBeDefined();
    expect(screen.getByText("11 Siege · 9 Niederlagen")).toBeDefined();
    expect(screen.queryByText(/können sich noch/)).toBeNull();
  });

  test("flags a small sample on either side", () => {
    render(
      <HomeAwayCard
        homeAway={{
          home: { wins: 8, losses: 2, winPercentage: 80 },
          away: { wins: 1, losses: 0, winPercentage: 100 },
        }}
      />,
    );
    expect(screen.getByText("1 Sieg · 0 Niederlagen")).toBeDefined();
    expect(
      screen.getByText("Erst 10 Matches zu Hause und 1 Match auswärts – die Quoten können sich noch deutlich ändern."),
    ).toBeDefined();
  });

  test("explains a side without any match", () => {
    render(
      <HomeAwayCard
        homeAway={{
          home: { wins: 0, losses: 0, winPercentage: 0 },
          away: { wins: 3, losses: 3, winPercentage: 50 },
        }}
      />,
    );
    expect(screen.getByText("Noch kein Heimspiel")).toBeDefined();
    expect(screen.queryByText("0 %")).toBeNull();
    expect(screen.getByText("3 Siege · 3 Niederlagen")).toBeDefined();
  });
});
