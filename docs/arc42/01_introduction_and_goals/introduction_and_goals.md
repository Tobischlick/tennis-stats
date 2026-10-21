# 1. Introduction and Goals

Full narrative: [vision.md](vision.md) and [user-journeys.md](user-journeys.md).

## Requirements Overview
Tennis Stats lets BTV (Badischer Tennisverband) players search any player and
see stats BTV itself doesn't provide — win/loss record, tie-break
performance, head-to-head history — kept fresh by crawling BTV's results
regularly. Read-only, no accounts yet, BTV only.

## Quality Goals

| Priority | Quality Goal | Motivation |
|---|---|---|
| 1 | Usability | Modern, clean, self-explanatory UI — no manual needed (see vision's "Experience" section). |
| 2 | Data freshness | Stats must reflect recently played matches, not stale snapshots. |
| 3 | Reliability | Crawler must run unattended without breaking on every BTV markup change. |
| 4 | Privacy | Runs locally for personal use only; crawled data is never published ([ADR-0003](../09_architecture_decisions/adr-0003-local-only.md)). |

## Stakeholders

| Role | Expectation |
|---|---|
| Author (also primary user) | A tool they'll actually use before matches; also the build/maintain effort. |
| BTV / nuLiga operator | Polite crawling, no redistribution of its data, nothing that looks official. |
