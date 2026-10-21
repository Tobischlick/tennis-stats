# ADR-0003: Local-only operation

**Status:** Accepted — supersedes [ADR-0002](adr-0002-crawler-execution.md)
and the hosting parts of [ADR-0001](adr-0001-technology-stack.md). The
"repository stays private" part is amended by
[ADR-0004](adr-0004-public-source.md).

**Context:** Tennis Stats was planned as a public web app on free-tier cloud
hosting (Cloud Run, Neon, Vercel). A legal review before going live found
(not formal legal advice):

- **GDPR:** player names and their results/stats are personal data, even
  though BTV publishes them. Showing only IDs doesn't help: nuLiga IDs are
  pseudonymous and trivially re-identifiable through BTV. Purely private,
  local use falls under the household exemption. A public app would need a
  legal basis, a privacy policy, data-subject rights handling and special
  care for minors.
- **Database right (§87a ff. UrhG):** BTV or the portal operator likely
  holds a database right in nuLiga. Systematically extracting large parts
  of it is the risky part; for private local use the risk is low as long
  as crawling stays polite.
- **Scraping/competition law:** fetching public pages is fine;
  circumventing technical protections (login, captchas, rate limits) is
  not.

**Options considered:**

| Option | Notes |
|---|---|
| A | Go public as planned — needs privacy policy, Impressum, opt-out/deletion handling, minors excluded, ideally BTV's permission. Far beyond a hobby project. |
| B | Public with aggregated or opt-in data only — reduces but doesn't remove the GDPR and database-right questions, and loses the "look up my opponent" use case. |
| **C (chosen)** | Run locally for personal use only. |

**Decision:** Go with Option C. Tennis Stats runs on the maintainer's machine
only: local Postgres, backend and frontend, and the crawler started locally
in crawler mode. Nothing is deployed and the repository stays private.

**Consequences:**
- No cloud hosting, no Cloud Run Job, no Cloud Scheduler, no deploy CI/CD.
  The crawler mode from
  ADR-0002 stays: one run, then exit.
- No privacy policy, Impressum, cookie handling, accounts, opt-out flows or
  name hiding — the household exemption covers local use.
- Still required:
  - Polite crawling: sequential requests with a delay, incremental
    skipping of completed meetings, and stopping on rate-limit or block
    responses instead of retrying.
  - No circumvention of any access restriction.
  - No redistribution: no public repo, no published data dumps; database
    files and dumps stay out of git. The test fixtures contained real
    players' names at the time, which was one more reason the repo stayed
    private.
  - No BTV logos or anything that looks official.
- **If going live is ever reconsidered**, revisit this first: talk to BTV
  (permission or an API), respect player opt-outs, exclude minors, and
  consider aggregated-only stats or opt-in profiles.
