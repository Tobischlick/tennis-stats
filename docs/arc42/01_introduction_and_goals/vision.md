# Tennis Stats — Product Vision

## Vision statement
Tennis Stats gives Badischer Tennisverband (BTV) players a fast way to look up any
player and see the stats BTV itself doesn't show: win/loss record, tie-break
performance, and other derived numbers, kept up to date automatically.

## Why this matters
The night before a match, I want to pull up my opponent on Tennis Stats and
actually know something: are they shaky in tie-breaks, do they fade in the
third set, have we played before and who won. Right now that knowledge just
doesn't exist anywhere — BTV has the raw results buried in a results table
and nothing else. Using Tennis Stats should feel like finally being able to
check my own stats and see myself improve match by match, and like walking
onto the court knowing something my opponent doesn't. That "I checked my
stats before this match" moment is the whole point — everything else in
this document is in service of that feeling.

## Problem
BTV publishes match results but no aggregated stats and no API. Today,
answering "how does this player perform under pressure" or "what's my
head-to-head" means manually digging through result pages.

## Target users
Amateur tennis players in BTV (including the club/friend circle around the
author) who want to check their own or an opponent's stats before a match, or
just out of curiosity.

## Scope (v1)
- BTV only — no other Landesverband, no generalized multi-association model.
- Read-only stats lookup. No club/coach dashboards, no team management.
- Personal/hobby project: free, no ads, no paid tier, no growth targets.

## Goals
1. Search any BTV player and see their match history.
2. Surface stats BTV doesn't: win/loss, tie-break record, surface splits,
   streaks, etc.
3. Keep data fresh via a regular crawl — no manual data entry.
4. Run locally for personal use only — no hosting, no published data
   ([ADR-0003](../09_architecture_decisions/adr-0003-local-only.md)).

## Experience
Tennis Stats should feel modern and effortless, not like a digitized results
table. A player should search a name, land on a clean stats page, and
immediately understand what they're looking at — no manual, no legend, no
clutter. Design and framework choices come later (architecture step), but
"clean and self-explanatory" is a bar every screen has to clear.

## Non-goals (for now)
- Auth / accounts — comes later once there's a reason (favorites, alerts).
- Any BTV association other than Baden.
- Mobile app — web only.
- Monetization in any form.

## Key constraints & risks
- **No official API.** Data comes from crawling BTV's results system
  (likely nuLiga/mybigpoint). This is fragile (HTML changes break the
  crawler) and its legality/ToS compliance needs a deliberate check before
  the crawler is built, not after — republishing scraped data can run afoul
  of the source's terms even for a hobby project. Checked: player results
  are personal data and nuLiga is likely protected by a database right, so
  Tennis Stats stays local
  ([ADR-0003](../09_architecture_decisions/adr-0003-local-only.md)); only
  the source code is public
  ([ADR-0004](../09_architecture_decisions/adr-0004-public-source.md)).
- **Crawl politeness.** Regular polling must stay low-frequency and
  respectful of the source (no aggressive scraping) — partly ethical, partly
  self-preservation if BTV blocks the IP.
- **Local only.** Tennis Stats runs on the author's machine for personal use.
  Going public would need a legal basis, a privacy policy, opt-out handling
  and care for minors — out of scope
  ([ADR-0003](../09_architecture_decisions/adr-0003-local-only.md)).

## Success looks like
The author actually uses it before matches instead of guessing, a local
crawl run brings new results in without breaking every week, and the whole
thing costs €0/month to run.
