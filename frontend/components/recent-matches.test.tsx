import { afterEach, describe, expect, test } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import type { components } from "@/lib/api/schema";
import { RecentMatches } from "./recent-matches";

type RecentMatch = components["schemas"]["RecentMatch"];
type SetScore = components["schemas"]["SetScore"];

afterEach(cleanup);

function set(own: number, opponent: number, tieBreak?: [number, number]): SetScore {
  return {
    own,
    opponent,
    ownTieBreak: tieBreak?.[0] ?? null,
    opponentTieBreak: tieBreak?.[1] ?? null,
    matchTieBreak: false,
  };
}

function match(sets: SetScore[], overrides: Partial<RecentMatch> = {}): RecentMatch {
  return {
    playedOn: "2026-09-20",
    type: "SINGLES",
    result: "WIN",
    opponents: [{ id: "p2", name: "Lena Maier", club: "TC Rot-Gelb Heidelberg" }],
    partner: null,
    ownTeam: "TC Blau-Weiss Karlsruhe 1",
    opponentTeam: "TC Rot-Gelb Heidelberg 1",
    sets,
    ...overrides,
  };
}

function setScores(): HTMLElement[] {
  const row = screen.getAllByRole("listitem")[0];
  return Array.from(row.querySelectorAll<HTMLElement>(":scope > p > span"));
}

describe("RecentMatches set scores", () => {
  test("shows the tie-break loser's points as superscript", () => {
    render(<RecentMatches matches={[match([set(7, 6, [7, 4]), set(6, 2)])]} />);
    const [first, second] = setScores();
    expect(first.textContent).toBe("7:64");
    expect(first.querySelector("sup")?.textContent).toBe("4");
    expect(second.querySelector("sup")).toBeNull();
  });

  test("uses the lower tie-break score when the player lost the tie-break", () => {
    render(<RecentMatches matches={[match([set(6, 7, [5, 7]), set(3, 6)], { result: "LOSS" })]} />);
    expect(setScores()[0].querySelector("sup")?.textContent).toBe("5");
  });

  test("shows a match tie-break as the third score", () => {
    const sets = [set(4, 6), set(6, 3), { ...set(10, 7), matchTieBreak: true }];
    render(<RecentMatches matches={[match(sets)]} />);
    const scores = setScores();
    expect(scores.map((s) => s.textContent)).toEqual(["4:6", "6:3", "10:7"]);
    expect(scores[2].title).toBe("Match-Tie-Break");
    expect(scores[2].querySelector("sup")).toBeNull();
    expect(scores[0].title).toBe("");
  });

  test("mutes lost sets only", () => {
    render(<RecentMatches matches={[match([set(4, 6), set(6, 3), set(6, 7, [3, 7])])]} />);
    const muted = setScores().map((s) => s.classList.contains("text-muted-foreground"));
    expect(muted).toEqual([true, false, true]);
  });
});

describe("RecentMatches list", () => {
  test("shows the German date, opponent and match type", () => {
    render(
      <RecentMatches
        matches={[
          match([set(6, 3), set(6, 4)], {
            type: "DOUBLES",
            partner: { id: "p3", name: "Mia Wolf", club: null },
            opponents: [
              { id: "p2", name: "Lena Maier", club: null },
              { id: "p4", name: "Sara Kurz", club: null },
            ],
          }),
        ]}
      />,
    );
    expect(screen.getByText("Lena Maier / Sara Kurz")).toBeDefined();
    expect(screen.getByText(/20\.09\. · Doppel mit Mia Wolf/)).toBeDefined();
  });

  test("shows five matches and collapses the rest", () => {
    const matches = Array.from({ length: 7 }, () => match([set(6, 0), set(6, 0)]));
    const { container } = render(<RecentMatches matches={matches} />);
    expect(screen.getByText("2 weitere Matches anzeigen")).toBeDefined();
    expect(container.querySelectorAll(":scope > div > ol > li")).toHaveLength(5);
    expect(container.querySelectorAll("details li")).toHaveLength(2);
  });

  test("uses the singular for a single hidden match", () => {
    const matches = Array.from({ length: 6 }, () => match([set(6, 0), set(6, 0)]));
    render(<RecentMatches matches={matches} />);
    expect(screen.getByText("1 weiteres Match anzeigen")).toBeDefined();
  });

  test("has no expander for five or fewer matches", () => {
    const matches = Array.from({ length: 5 }, () => match([set(6, 0), set(6, 0)]));
    render(<RecentMatches matches={matches} />);
    expect(screen.queryByText(/weitere/)).toBeNull();
  });
});

describe("RecentMatches opponents", () => {
  test("shows 'Unbekannt' when the opponent wasn't named", () => {
    render(<RecentMatches matches={[match([set(6, 4), set(6, 3)], { opponents: [] })]} />);

    expect(screen.getAllByRole("listitem")[0].textContent).toContain("Unbekannt");
  });
});
