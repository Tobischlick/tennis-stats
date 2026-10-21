# ADR-0002: Crawler runs as a Cloud Run Job

**Status:** Superseded by [ADR-0003](adr-0003-local-only.md) — nothing is
deployed, so there is no Cloud Run Job or Cloud Scheduler. The crawler mode
described here (run once, then exit) stays and is started locally.

**Context:** [ADR-0001](adr-0001-technology-stack.md) assumed the backend
is a persistent JVM, so the crawler could run in-process as a Spring
`@Scheduled` cron job with no extra infrastructure. That does not hold on
Google Cloud Run's free tier: the API scales to zero when idle, and with
request-based billing CPU is throttled between requests, so a cron inside
the running app would not fire. Keeping an instance always on
(`min-instances=1`) exceeds the free tier by a wide margin (one instance
is about 2.6M seconds a month against a free allowance of 180k
vCPU-seconds). The crawl must stay inside the free-tier and polite-crawling
constraints in
[02_architecture_constraints](../02_architecture_constraints/architecture_constraints.md).

**Options considered:**

| Option | Notes |
|---|---|
| A | `@Scheduled` in the API process with `min-instances=1` — not free; CPU throttled between requests anyway. |
| B | Cloud Scheduler calls an HTTP crawl endpoint on the API — free, but needs a public endpoint with app-level auth, and the request must stay open for the whole crawl (Scheduler's HTTP deadline is about 30 minutes; a first full crawl can take hours). |
| C | GitHub Actions cron calls the same HTTP endpoint — free, but the same endpoint, auth and timeout problems, and a second scheduler outside the cloud project. |
| **D (chosen)** | Same image started in crawler mode as a Cloud Run Job, triggered daily by Cloud Scheduler. |

**Decision:** Go with Option D. The crawler code stays in the backend
codebase (`crawler` package) and ships in the same container image as the
API. The image can start in a crawler mode: web server off, an
`ApplicationRunner` runs the crawl service once and exits, gated by
`tennisstats.crawler.enabled`. Cloud Scheduler starts the Cloud Run Job on a
daily cron; the cron expression lives there, not in code.

**Consequences:**
- No HTTP trigger endpoint and no app-level auth for the crawl — Cloud Run
  IAM controls who can start the job.
- The crawler is isolated from the API: a crawl failure or slow nuLiga
  response cannot affect API requests. This replaces the in-process risk
  accepted in ADR-0001.
- Job tasks can run far longer than a request (up to 24 hours) and have
  built-in retries, which fits the first full backfill.
- The first full backfill can also run locally against Neon using the
  same crawler mode, with no cloud timeout involved.
- The crawl logic itself is unchanged: sequential fetching with a delay and
  a descriptive `User-Agent`, incremental skipping of already-completed
  meetings, per-meeting error isolation, idempotent persistence.
- CI/CD needs one more deploy step (the job) next to the API service, and
  one Cloud Scheduler job (the free tier includes 3 per billing account).
- Cloud Run and Cloud Scheduler require a Google billing account with a
  payment method on file, even within the free tier.
- Local development runs the crawl through the same runner instead of a
  cron, which is simpler than triggering a `@Scheduled` job.
