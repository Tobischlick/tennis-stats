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
- Every epic issue must follow [docs/templates/epic.md](docs/templates/epic.md).
- Every story issue must follow [docs/templates/story.md](docs/templates/story.md).
- Bugs don't need an epic: file them with the `bug` label and the story template without the `**Epic:**` line.

## After a merge
Once a `feature/*` branch is merged into `main` (by the maintainer or by Claude), clean up without asking; this overrides the "ask before deleting" rule in the global instructions:
- Delete the branch locally (`git branch -d`, never `-D`; if it refuses, stop and ask) and on the remote.
- Remove the worktree the work was done in (`git worktree remove`, no `--force`, run from outside that worktree) and any scratch/temp directories created for the task.
- Close the story issue and tick it in its epic's checklist. If every story of that epic is then closed and its Definition of done is verified, close the epic and tick its boxes too. Skip closing when the PR says the issue stays open (e.g. `Refs #N` with an open acceptance-criteria note).
- `gh issue close`/`gh issue edit`/`gh pr edit` fail in this repo with a Projects (classic) GraphQL error; use `gh api` (REST) instead, e.g. `PATCH repos/Tobischlick/tennis-stats/issues/<n>`.
- Leave `renovate/*` branches alone; Renovate manages them.

## CI
- `.github/workflows/backend-ci.yml` runs backend tests on every PR targeting `main` and on every push to `main` (i.e. after each merge).
- `.github/workflows/frontend-ci.yml` runs frontend lint, a stale API types check (`npm run gen:api` must not change `lib/api/schema.d.ts`), build and tests (once a `test` script exists) on every PR targeting `main` and on every push to `main`.
- `main` is protected by the "Protect main" ruleset: no direct pushes, PRs need a code-owner review (see `CODEOWNERS`) and both checks (`test`, `check`) passing on an up-to-date branch. Only Renovate may merge its own PRs without a review.
