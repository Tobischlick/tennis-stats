import { afterEach, describe, expect, test } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import type { DoublesMeeting } from "@/lib/api-client/types";
import { DoublesMeetings } from "./doubles-meetings";

afterEach(cleanup);

function meeting(overrides: Partial<DoublesMeeting> = {}): DoublesMeeting {
  return {
    playedOn: "2026-09-20",
    result: "LOSS",
    partner: { id: "p3", name: "Clara Wolf", club: "TC Blau-Weiss Karlsruhe" },
    opponentPartner: { id: "p4", name: "Lena Vogt", club: "TC Rot-Gelb Heidelberg" },
    ownTeam: "TC Blau-Weiss Karlsruhe 1",
    opponentTeam: "TC Rot-Gelb Heidelberg 1",
    sets: [
      { own: 4, opponent: 6, ownTieBreak: null, opponentTieBreak: null, matchTieBreak: false },
      { own: 6, opponent: 7, ownTieBreak: 5, opponentTieBreak: 7, matchTieBreak: false },
    ],
    ...overrides,
  };
}

describe("DoublesMeetings", () => {
  test("lists both partners, the result and the set scores", () => {
    render(<DoublesMeetings meetings={[meeting()]} opponentName="Anna Schuster" />);
    const row = screen.getByRole("listitem");
    expect(row.textContent).toContain("Niederlage gegen Anna Schuster / Lena Vogt");
    expect(row.textContent).toContain("20.09. · mit Clara Wolf");
    expect(row.textContent).toContain("4:6");
    expect(row.querySelector("sup")?.textContent).toBe("5");
  });

  test("leaves out partners missing from the data", () => {
    render(
      <DoublesMeetings meetings={[meeting({ result: "WIN", partner: null, opponentPartner: null })]} opponentName="Anna Schuster" />,
    );
    const row = screen.getByRole("listitem");
    expect(row.textContent).toContain("Sieg gegen Anna Schuster");
    expect(row.textContent).not.toContain("/");
    expect(row.textContent).not.toContain("mit");
  });
});
