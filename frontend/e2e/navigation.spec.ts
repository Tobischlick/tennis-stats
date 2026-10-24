import { expect, test } from "@playwright/test";
import { anna, berta } from "./fixtures";

test("search finds a player and opens their stats", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("searchbox", { name: "Spieler suchen" }).fill("schmidt");
  await page.getByRole("button", { name: "Suchen" }).click();

  await expect(page).toHaveURL("/?q=schmidt");
  await expect(page.getByRole("heading", { name: "1 Spieler für „schmidt“" })).toBeVisible();
  await page.getByRole("link", { name: new RegExp(anna.name) }).click();

  await expect(page).toHaveURL(`/player/${anna.id}`);
  await expect(page.getByRole("heading", { level: 1 })).toHaveText(anna.name);
});

test("player page shows the stats", async ({ page }) => {
  await page.goto(`/player/${anna.id}`);

  await expect(page.getByRole("heading", { level: 1 })).toHaveText(anna.name);
  await expect(page.getByText("3 von 4 Tie-Breaks gewonnen")).toBeVisible();
  await expect(page.getByText("1 von 1 Entscheidungssätzen gewonnen")).toBeVisible();
  await expect(page.getByText(`Sieg gegen ${berta.name}`)).toHaveCount(2);
});

test("all-matches page lists every match", async ({ page }) => {
  await page.goto(`/player/${anna.id}`);
  await page.getByRole("link", { name: "Alle Matches" }).click();

  await expect(page).toHaveURL(`/player/${anna.id}/matches`);
  await expect(page.getByRole("heading", { level: 1 })).toHaveText("Alle Matches");
  await expect(page.getByRole("main").getByRole("listitem")).toHaveCount(3);
});

test("compare picker opens the head-to-head page", async ({ page }) => {
  await page.goto(`/player/${anna.id}`);
  await page.getByRole("searchbox", { name: "Gegner suchen" }).fill("Berta");
  await page.getByRole("link", { name: new RegExp(berta.name) }).click();

  await expect(page).toHaveURL(`/player/${anna.id}/vs/${berta.id}`);
  await expect(page.getByRole("heading", { name: "Direkter Vergleich" })).toBeVisible();
  await expect(page.getByLabel("2 zu 1")).toBeVisible();
  await expect(page.getByText(`${anna.name} führt nach 3 Duellen.`)).toBeVisible();
});
