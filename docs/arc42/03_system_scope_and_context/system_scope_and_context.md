# 3. System Scope and Context

## Business Context

```
BTV Player (browser) ──search/view stats──▶ Tennis Stats
Tennis Stats (crawler)  ──reads public results──▶ nuLiga (baden.liga.nu)
```

- **BTV player**: uses Tennis Stats read-only, browser only, no account.
- **nuLiga (baden.liga.nu)**: BTV's official results system (Apple
  WebObjects app). Tennis Stats only reads from it — never writes, never
  authenticates. Publicly accessible: league tables, schedules
  ("Spielplan"), and per-match reports ("Spielbericht") with individual
  set scores.

## Technical Context

- **nuLiga interface**: plain HTTP GET, server-rendered HTML, no JS
  rendering required. Stable, parameterized URLs
  (`.../wa/groupPage?championship=B1+S+2026&group=1`,
  `.../wa/meetingReport?meeting=<id>&federation=BAD&championship=...`).
  Confirmed live (Sept 2026) — league standings, schedules, and match
  reports (incl. per-set scores and deciding-set match tie-breaks, e.g.
  a 3rd set `10:8`) all render as static HTML tables. No player-search
  endpoint exists — data is organized by championship → group → meeting,
  not by player, which is exactly the gap Tennis Stats fills.
- **Tennis Stats ↔ end user**: HTTPS, browser to the hosted web app.
- **Tennis Stats ↔ database**: the web app and the crawler both talk
  directly to Postgres over its standard connection protocol.
