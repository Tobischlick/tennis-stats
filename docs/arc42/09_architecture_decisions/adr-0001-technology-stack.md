# ADR-0001: Technology stack

**Status:** Accepted — crawler scheduling amended by
[ADR-0002](adr-0002-crawler-execution.md), hosting superseded by
[ADR-0003](adr-0003-local-only.md) (local-only); the technology stack itself stands.

**Context:** Tennis Stats needs a stack for crawler, backend, and frontend
that fits the constraints in
[02_architecture_constraints](../02_architecture_constraints/architecture_constraints.md)
(free-tier hosting only, polite crawling of static HTML, BTV-only scope)
and the monorepo requirement.

**Options considered:**

| Option | Stack | Notes |
|---|---|---|
| A | TypeScript everywhere — Next.js (frontend+API), Postgres/Neon, Drizzle, Node+cheerio crawler on GitHub Actions cron | Simplest, one language, fully serverless. |
| B | Python + TypeScript — FastAPI backend, Postgres, Python crawler (requests/BeautifulSoup), separate TS frontend | Strong scraping/data ecosystem, but two languages and a persistent server to host on a free tier. |
| C | Cloudflare edge — SvelteKit, Turso (SQLite), Cloudflare Workers with native Cron Triggers | Fewest vendors, native edge cron, but SQLite has less headroom than Postgres for future aggregation. |
| **D (chosen)** | Java + Spring Boot backend (JSoup crawler in the same codebase), Postgres/Neon, separate Next.js frontend | Matches the author's existing Java/Spring background; JSoup is a strong fit for static-HTML crawling; crawler shares the backend's codebase and image, so there is one build. How it is triggered is decided in [ADR-0002](adr-0002-crawler-execution.md). |

**Decision:** Go with Option D.

**Consequences:**
- Backend needs a genuinely-free host that can run a JVM container —
  Google Cloud Run (scale-to-zero) chosen over Fly.io/Railway, which
  dropped their free tiers.
- Two toolchains in the monorepo (Java/Gradle or Maven for `backend/`,
  Node for `frontend/`) rather than a single shared JS workspace — no
  shared type-checking between them, so the API contract needs to be
  kept in sync deliberately (e.g. via an OpenAPI spec) rather than
  shared TypeScript types.
- Crawler shares the backend's codebase and image but runs as its own
  Cloud Run Job (see [ADR-0002](adr-0002-crawler-execution.md)), not
  inside the API process, so a crawl failure cannot take the API down.

Full rationale for each row: see
[04_solution_strategy](../04_solution_strategy/solution_strategy.md).
