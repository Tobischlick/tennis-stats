import { afterEach, describe, expect, test } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import type { components } from "@/lib/api/schema";
import { FormDots } from "./form-dots";

type MatchResult = components["schemas"]["MatchResult"];

afterEach(cleanup);

describe("FormDots", () => {
  test("captions wins out of the number of results shown", () => {
    const results: MatchResult[] = ["WIN", "WIN", "LOSS", "WIN", "LOSS", "WIN", "WIN", "LOSS", "WIN", "WIN"];
    render(<FormDots results={results} />);
    expect(screen.getByText("7 von 10 gewonnen")).toBeDefined();
  });

  test("derives n from the actual array length", () => {
    render(<FormDots results={["LOSS", "WIN", "LOSS"]} />);
    expect(screen.getByText("1 von 3 gewonnen")).toBeDefined();
  });

  test("renders one labelled dot per result, newest first", () => {
    render(<FormDots results={["WIN", "LOSS", "WIN", "WIN"]} />);
    const dots = screen.getAllByRole("listitem");
    expect(dots.map((d) => d.textContent)).toEqual(["Sieg", "Niederlage", "Sieg", "Sieg"]);
    expect(screen.getByText("← Neuestes")).toBeDefined();
  });
});
