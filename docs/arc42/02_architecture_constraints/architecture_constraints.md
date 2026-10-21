# 2. Architecture Constraints

| Constraint | Explanation |
|---|---|
| No official BTV API | Data must come from crawling BTV's results system (likely nuLiga/mybigpoint) — fragile to markup changes. Legal review done: see the local-only row. |
| Polite crawling only | Regular polling must stay low-frequency and respectful of the source — sequential requests with a delay, and no circumvention of rate limits, blocks or logins. |
| Local-only, personal use | No hosting and no redistribution of crawled data (GDPR household exemption, database right of the portal operator) — see [ADR-0003](../09_architecture_decisions/adr-0003-local-only.md). DB files and dumps stay out of git; no BTV logos or official look. |
| BTV scope only | No generalized multi-association data model in v1. |
