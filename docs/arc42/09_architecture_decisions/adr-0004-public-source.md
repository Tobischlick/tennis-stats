# ADR-0004: Public source code

**Status:** Accepted — amends [ADR-0003](adr-0003-local-only.md): the
repository no longer stays private. Local-only operation stays.

**Context:** ADR-0003 kept the repository private, partly because the test
fixtures contained real players' names. The fixtures and tests now use fake
data only, so the code itself holds no personal data. Others could run
Tennis Stats on their own machine for their own use, which the household
exemption covers just like the maintainer's local instance. The git history
still contains the real names, though, and so may issues and pull requests.

**Options considered:**

| Option | Notes |
|---|---|
| A | Keep the repository private — nobody else can run Tennis Stats. |
| B | Make this repository public after rewriting its history — GitHub keeps old commits reachable through pull request refs, and issues/PRs would be published too. |
| **C (chosen)** | Publish the current source in a new public repository, starting from a single commit without history. |

**Decision:** Go with Option C under the MIT license. This repository stays
private as the archive of the history, issues and pull requests.

**Consequences:**
- Still local only: nothing is deployed, and every user runs their own
  instance and crawler.
- Still no redistribution: no crawled data, dumps, database files or real
  player names in git, issues or pull requests. Test fixtures use fake data
  only.
- The crawler's polite defaults (sequential requests with a delay, stopping
  on rate-limit or block responses) stay, since others will run it too.
- Docs don't link to this repository's issues or pull requests, since they
  aren't visible from the public one.
