import { describe, expect, test } from "vitest";
import {
  firstName,
  formatPercent,
  formatShortDate,
  isSmallSample,
  matchCount,
  resultLabel,
  tieBreakVerdict,
} from "./format";

describe("formatPercent", () => {
  test("rounds to whole numbers with a non-breaking space before the percent sign", () => {
    expect(formatPercent(64)).toBe("64\u00a0%");
    expect(formatPercent(36.4)).toBe("36\u00a0%");
    expect(formatPercent(66.7)).toBe("67\u00a0%");
    expect(formatPercent(0)).toBe("0\u00a0%");
    expect(formatPercent(100)).toBe("100\u00a0%");
  });

  test("uses German digit grouping", () => {
    expect(formatPercent(1234)).toBe("1.234\u00a0%");
  });
});

describe("formatShortDate", () => {
  test("formats as day and month with German separators", () => {
    expect(formatShortDate("2026-09-20")).toBe("20.09.");
    expect(formatShortDate("2026-01-05")).toBe("05.01.");
  });

  test("does not shift the day across time zones", () => {
    expect(formatShortDate("2026-12-31")).toBe("31.12.");
    expect(formatShortDate("2026-01-01")).toBe("01.01.");
  });
});

describe("tieBreakVerdict", () => {
  test.each([
    [0, "Wackelt im Tie-Break"],
    [39.9, "Wackelt im Tie-Break"],
    [40, "Im Tie-Break ausgeglichen"],
    [50, "Im Tie-Break ausgeglichen"],
    [60, "Im Tie-Break ausgeglichen"],
    [60.1, "Stark im Tie-Break"],
    [100, "Stark im Tie-Break"],
  ])("rate %s → %s", (rate, verdict) => {
    expect(tieBreakVerdict(rate)).toBe(verdict);
  });
});

describe("isSmallSample", () => {
  test.each([
    [0, true],
    [1, true],
    [4, true],
    [5, false],
    [20, false],
  ])("%s matches → %s", (matchesPlayed, expected) => {
    expect(isSmallSample(matchesPlayed)).toBe(expected);
  });
});

describe("wording helpers", () => {
  test("matchCount uses the singular only for one match", () => {
    expect(matchCount(1)).toBe("1 Match");
    expect(matchCount(0)).toBe("0 Matches");
    expect(matchCount(3)).toBe("3 Matches");
  });

  test("resultLabel picks the German word by result and count", () => {
    expect(resultLabel("WIN", 1)).toBe("Sieg");
    expect(resultLabel("WIN", 3)).toBe("Siege");
    expect(resultLabel("LOSS", 1)).toBe("Niederlage");
    expect(resultLabel("LOSS", 2)).toBe("Niederlagen");
  });

  test("firstName takes the first word of the name", () => {
    expect(firstName("Anna Schmidt")).toBe("Anna");
  });
});
