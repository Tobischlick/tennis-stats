import { describe, expect, test } from "vitest";
import { normalizeQuery, searchHref } from "./search";

describe("normalizeQuery", () => {
  test("trims and takes the first value", () => {
    expect(normalizeQuery("  Muster ")).toBe("Muster");
    expect(normalizeQuery(["Erika", "Max"])).toBe("Erika");
    expect(normalizeQuery(undefined)).toBe("");
  });
});

describe("searchHref", () => {
  test("encodes the query and drops an empty one", () => {
    expect(searchHref(" Erika Muster ")).toBe("/?q=Erika+Muster");
    expect(searchHref("Müller & Co")).toBe("/?q=M%C3%BCller+%26+Co");
    expect(searchHref("  ")).toBe("/");
  });
});
