import { defineConfig, devices } from "@playwright/test";
import { MOCK_API_PORT } from "./e2e/mock-api";

const PORT = 3100;

export default defineConfig({
  testDir: "e2e",
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: [["list"], ["html", { open: "never" }]],
  globalSetup: "./e2e/mock-api.ts",
  use: {
    baseURL: `http://127.0.0.1:${PORT}`,
    trace: "retain-on-failure",
  },
  projects: [{ name: "chromium", use: { ...devices["Desktop Chrome"] } }],
  webServer: {
    command: `npm run build && npm run start -- --port ${PORT}`,
    // A route without backend calls: the readiness check runs before globalSetup starts the mock API.
    url: `http://127.0.0.1:${PORT}/icon.svg`,
    env: { TENNIS_STATS_API_BASE_URL: `http://127.0.0.1:${MOCK_API_PORT}/api/v1` },
    timeout: 300_000,
  },
});
