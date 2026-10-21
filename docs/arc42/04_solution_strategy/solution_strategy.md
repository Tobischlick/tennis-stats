# 4. Solution Strategy

See [ADR-0001](../09_architecture_decisions/adr-0001-technology-stack.md)
for the alternatives considered and why this stack was chosen.

## Technology decisions

| Concern | Choice | Why |
|---|---|---|
| Backend language | Java + Spring Boot | One codebase and one container image serve the API and run the crawl job, so there is a single build for both. |
| Persistence | Spring Data JPA/Hibernate | Standard, mature ORM for the Spring ecosystem; maps cleanly onto the relational player/match/set domain. |
| Crawler | JSoup, run locally in crawler mode | nuLiga is confirmed static server-rendered HTML — JSoup is the standard Java library for exactly this, with no headless browser. Lives in the backend codebase but runs outside the API process; see [ADR-0002](../09_architecture_decisions/adr-0002-crawler-execution.md) and [ADR-0003](../09_architecture_decisions/adr-0003-local-only.md). |
| Database | Postgres, local Docker container | Relational fits match/set/player data well; data stays on the local machine. |
| Frontend | Next.js (App Router) + Tailwind + shadcn/ui | Modern, clean UI out of the box (matches the vision's Experience bar); calls the Spring REST API. Kept separate from the backend rather than server-rendered Thymeleaf, since Spring's templating doesn't hit that bar as easily. |
| Hosting | None — local only | Public hosting dropped after a legal review; see [ADR-0003](../09_architecture_decisions/adr-0003-local-only.md). |
| Repo layout | Single monorepo | `backend/` (Gradle/Maven Spring Boot project) and `frontend/` (Next.js) in one repo — not a shared build tool across languages, but one repo, which keeps agent-driven per-story worktrees simple. |

## Key strategic decisions

- **Crawler shares the backend's codebase but runs as its own process**
  (crawler mode: one crawl, then exit), not inside the API process, so a
  crawl failure stays away from the API and no scheduler lives in the app.
  See [ADR-0002](../09_architecture_decisions/adr-0002-crawler-execution.md)
  and the [deployment view](../07_deployment_view/deployment_view.md).
- **No headless browser.** The site being static HTML — confirmed by
  inspecting baden.liga.nu directly — is what makes the "polite crawling"
  constraint from
  [02_architecture_constraints](../02_architecture_constraints/architecture_constraints.md)
  achievable: JSoup fetching and parsing plain HTML is cheap, sequential
  and easy to throttle.
- **Frontend and backend are separate apps** (not a Spring monolith
  serving server-rendered pages), so the UI can hit the "modern, clean"
  bar with a proper frontend framework while the backend stays a
  standard Spring REST API.
