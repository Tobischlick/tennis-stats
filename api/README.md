# API contract

`openapi.yaml` is the OpenAPI 3.0 contract for the Tennis Stats player search and
player stats endpoints. It's the source of truth both the
backend and the frontend implement against — see
[docs/arc42/05_building_block_view/building_block_view.md](../docs/arc42/05_building_block_view/building_block_view.md).

- Backend: implements these endpoints under `backend`'s `api` package.
- Frontend: generates a typed client from this file (e.g. via
  `openapi-typescript` or `orval`) instead of hand-writing fetch calls.

Endpoints:
- `GET /players?query=` — name search, returns lightweight player summaries.
- `GET /players/{id}/stats` — win/loss, tie-break and streak stats.
- `GET /players/{id}/head-to-head/{opponentId}` — head-to-head record
  between two players: singles, plus doubles on opposite sides as a separate
  record with partners and set scores.
- `GET /players/{id}/matches?limit=` — the player's most recent matches
  (up to 10) with opponents, partner, teams and set scores.
- `GET /overview` — database totals (players, clubs, teams, meetings,
  matches, sets, date range) and a few fun stats for the landing page.

Every stats response includes a `hasEnoughData` flag. When a player has no
crawled matches yet, the stat fields are `null` instead of zeroed out, so
the frontend can render an explicit "not enough data yet" state instead of
a misleading empty/zero result.

The stats and head-to-head responses embed a `PlayerSummary` (name, club)
for each player involved, so a page header needs no extra lookup.

Validated with `npx @redocly/cli lint openapi.yaml` (0 errors).
