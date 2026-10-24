import { expect, test } from "@playwright/test";
import { UNREACHABLE_PLAYER_ID } from "./fixtures";

test("unknown player id shows the not-found page", async ({ page }) => {
  await page.goto("/player/does-not-exist");

  await expect(page.getByRole("heading", { name: "Spieler nicht gefunden" })).toBeVisible();
});

test("unreachable backend shows the error boundary", async ({ page }) => {
  await page.goto(`/player/${UNREACHABLE_PLAYER_ID}`);

  await expect(page.getByRole("heading", { name: "Daten gerade nicht erreichbar" })).toBeVisible();
  await expect(page.getByRole("button", { name: "Erneut versuchen" })).toBeVisible();
});
