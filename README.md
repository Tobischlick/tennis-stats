![GitHub release (latest by date)](https://img.shields.io/github/v/release/Tobischlick/tennis-stats)

# Tennis Stats

Stats lookup tool for Badischer Tennisverband (BTV) players — crawls BTV
match results into a database and surfaces stats (win/loss, tie-breaks,
head-to-head, and more) that BTV itself doesn't show.

**Built by AI agents:** this repo is almost entirely written by Claude Code.
Work is scoped into epics and stories as GitHub issues; each story is
implemented end-to-end by a Claude Code subagent in its own branch, then
reviewed and merged by the human owner, who also drives the product vision
and architecture docs in conversation with Claude Code.

**Status:** not in active development right now — no further features are
planned as of now.

## Docs

- [Product vision](docs/arc42/01_introduction_and_goals/vision.md)
- [User journeys](docs/arc42/01_introduction_and_goals/user-journeys.md)
- [Architecture (arc42)](docs/arc42) — introduction & goals, constraints,
  system context, solution strategy, building block view,
  [UI concept](docs/arc42/08_crosscutting_concepts/ui_concept.md), and
  [architecture decisions](docs/arc42/09_architecture_decisions/architecture_decisions.md)

## Local use only

Tennis Stats runs locally for personal use and is never deployed — see
[ADR-0003](docs/arc42/09_architecture_decisions/adr-0003-local-only.md) for
the legal reasons and what to revisit first if going live is ever
reconsidered. The source code is public so anyone can run their own local
instance ([ADR-0004](docs/arc42/09_architecture_decisions/adr-0004-public-source.md)).
In short:

- crawl politely: sequential requests with a delay, never work around rate
  limits, blocks or logins;
- don't redistribute crawled data: no dumps, no database files and no real
  player names in git;
- no BTV logos or anything that looks official.

## Stack

Java/Spring Boot backend (with a JSoup crawler run as a separate mode),
Postgres (local Docker), Next.js frontend — see [ADR-0001](docs/arc42/09_architecture_decisions/adr-0001-technology-stack.md)
for why.

## Contributing

See [CLAUDE.md](CLAUDE.md) for branching and commit conventions.

## Dependency updates

[Renovate](https://docs.renovatebot.com) opens the update PRs; its config is
[renovate.json5](renovate.json5). It runs daily (before 6am,
Europe/Berlin, at most 3 open PRs; security alerts skip the schedule): minor and
patch updates share one PR, each major gets its own,
and Spring Boot majors only appear once approved on the Dependency Dashboard
issue. Java only moves between LTS releases. Nothing is automerged — the CI
gate runs like on any PR and the PR is merged by hand.
