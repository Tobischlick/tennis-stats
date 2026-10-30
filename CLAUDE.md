# Tennis Stats

Stats lookup tool for Badischer Tennisverband (BTV) players — crawls BTV
match results into a database and surfaces stats (win/loss, tie-breaks, and
more) that BTV itself doesn't show. See
[docs/arc42/01_introduction_and_goals/vision.md](docs/arc42/01_introduction_and_goals/vision.md).

## Branching
- Always work on a feature branch, never commit directly to the default branch.
- Branch naming:
  - Tied to a GitHub issue: `feature/issue-<number>-<short-description>`
  - No issue: `feature/no-issue-<short-description>`

## Commits
- Short, single-line commit messages only — no multi-paragraph bodies.

## Issues
- Every issue is a standalone story following [docs/templates/story.md](docs/templates/story.md), bugs included. There are no epics anymore: #3 is the last one and is closed once its remaining stories are done.
- Every issue gets exactly one type label (`enhancement`, `bug` or `documentation`) and at least one area label (`backend`, `frontend`, `crawler`, `api`, `ci`).

## After a merge
Once a `feature/*` branch is merged into `main` (by the maintainer or by Claude), clean up without asking; this overrides the "ask before deleting" rule in the global instructions:
- Delete the branch locally (`git branch -d`, never `-D`; if it refuses, stop and ask) and on the remote.
- Remove the worktree the work was done in (`git worktree remove`, no `--force`, run from outside that worktree) and any scratch/temp directories created for the task.
- Close the story issue. Skip closing when the PR says the issue stays open (e.g. `Refs #N` with an open acceptance-criteria note). For #3's remaining stories (#4, #7, #8), also tick them in #3's checklist, and close #3 once all of them are closed and its Definition of done is verified.
- `gh issue close`/`gh issue edit`/`gh pr edit` fail in this repo with a Projects (classic) GraphQL error; use `gh api` (REST) instead, e.g. `PATCH repos/Tobischlick/tennis-stats/issues/<n>`.
- Leave `renovate/*` branches alone; Renovate manages them.

## CI
- `.github/workflows/backend-ci.yml` runs backend tests on every PR targeting `main` and on every push to `main` (i.e. after each merge).
- `.github/workflows/frontend-ci.yml` runs two jobs on every PR targeting `main` and on every push to `main`:
  - `check`: frontend lint, a stale API types check (`npm run gen:api` must not change `lib/api/schema.d.ts`), build and tests (once a `test` script exists).
  - `ui-tests`: the Playwright UI tests (`npm run test:e2e`) in Chromium against a production build and a mock backend serving the fixtures in `frontend/e2e/`; uploads the Playwright report as the `playwright-report` artifact when they fail.
- `.github/workflows/osv-scanner.yml` scans all lockfiles (`backend/gradle.lockfile`, `frontend/package-lock.json`, `prototype/package-lock.json`) for known vulnerabilities with Google's OSV-Scanner reusable workflows (pinned by commit SHA, digests updated by Renovate):
  - `scan-pr` on every PR targeting `main`: fails only on vulnerabilities the PR newly introduces compared to `main`.
  - `scan-scheduled` daily, on every push to `main` and on manual dispatch: scans `main` and uploads SARIF results to **Security → Code scanning**; fails (turns `main` red) whenever a known vulnerability is found.
  - Ignores go in `osv-scanner.toml` at the repo root (applied to every lockfile via `--config`), each with a `reason` and an `effectiveUntil` expiry date. Local run: `osv-scanner scan source --config=./osv-scanner.toml -r .`
- Backend dependencies are locked (`dependencyLocking` in `backend/build.gradle`). After changing a backend dependency, run `./gradlew dependencies --write-locks` in `backend/` and commit `backend/gradle.lockfile`; the build fails if it's out of date.
- `main` is protected by the "Protect main" ruleset: no direct pushes, PRs need a code-owner review (see `CODEOWNERS`) and all checks (`test`, `check`, `ui-tests`, `scan-pr / osv-scan`) passing on an up-to-date branch. Only Renovate may merge its own PRs without a review.
