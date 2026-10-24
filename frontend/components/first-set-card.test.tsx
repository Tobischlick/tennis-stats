import { afterEach, describe, expect, test } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import type { FirstSetStats } from "@/lib/api-client/types";
import { FirstSetCard } from "./first-set-card";

const none: FirstSetStats = {
  lostFirstSet: 0,
  comebacks: 0,
  comebackPercentage: 0,
  wonFirstSet: 0,
  blownLeads: 0,
  blownLeadPercentage: 0,
};

afterEach(cleanup);

describe("FirstSetCard", () => {
  test("shows both rates with their counts", () => {
    render(
      <FirstSetCard
        firstSet={{
          lostFirstSet: 14,
          comebacks: 3,
          comebackPercentage: 21.4,
          wonFirstSet: 27,
          blownLeads: 2,
          blownLeadPercentage: 7.4,
        }}
      />,
    );
    expect(screen.getByText("Comeback-Quote")).toBeDefined();
    expect(screen.getByText("21 %")).toBeDefined();
    expect(screen.getByText("3 von 14 nach verlorenem ersten Satz noch gewonnen")).toBeDefined();
    expect(screen.getByText("Vorsprung verspielt")).toBeDefined();
    expect(screen.getByText("7 %")).toBeDefined();
    expect(screen.getByText("2 von 27 nach gewonnenem ersten Satz noch verloren")).toBeDefined();
    expect(screen.queryByText(/gewertet/)).toBeNull();
  });

  test("flags a small sample of qualifying rubbers", () => {
    render(<FirstSetCard firstSet={{ ...none, lostFirstSet: 2, comebacks: 1, comebackPercentage: 50, wonFirstSet: 1 }} />);
    expect(screen.getByText("Erst 3 Matches gewertet – die Quoten können sich noch deutlich ändern.")).toBeDefined();
  });

  test("explains a rate without any rubber behind it", () => {
    render(<FirstSetCard firstSet={{ ...none, wonFirstSet: 6 }} />);
    expect(screen.getByText("Noch keinen ersten Satz verloren")).toBeDefined();
    expect(screen.getByText("0 von 6 nach gewonnenem ersten Satz noch verloren")).toBeDefined();
  });

  test("shows an empty state when no rubber counts", () => {
    render(<FirstSetCard firstSet={none} />);
    expect(screen.getByText("Noch kein Match mit beendetem ersten Satz")).toBeDefined();
    expect(screen.queryByText("Comeback-Quote")).toBeNull();
  });
});
