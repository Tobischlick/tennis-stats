# Tennis Stats — User Journeys

## Persona
The amateur BTV player — the author and their circle of club/tennis
friends. Plays league and club matches regularly, is not a data nerd, just
curious and competitive. Uses Tennis Stats on a phone or laptop, casually,
never as a "tool" they have to learn.

## Journey 1: Scouting an opponent before a match (primary)
**Trigger:** Match scheduled for tomorrow, opponent's name known but
otherwise a stranger.
**Steps:** Open Tennis Stats → search the opponent's name → land on their
stats page → skim win/loss, tie-break record, recent form.
**Outcome:** Walks into the match knowing whether the opponent chokes in
tie-breaks or fades in the third set — something BTV's own site could
never tell them. This is the "why this matters" moment from the vision.

## Journey 2: Checking your own progress after a match
**Trigger:** Just finished a match (won or lost), curious how it moved the
needle.
**Steps:** Open Tennis Stats → go to your own profile → see the new match
reflected in your record and any trend (win streak, tie-break rate over
time).
**Outcome:** A small, immediate sense of progress or a clear read on what
to work on — instead of the result just disappearing into a league table.

## Journey 3: Head-to-head lookup
**Trigger:** About to play someone you've faced before, or reminiscing
after a close match.
**Steps:** Open Tennis Stats → search the opponent → view head-to-head
history against them specifically.
**Outcome:** Knows the score of the rivalry, not just the individual
match — adds a bit of story/stakes to the next meeting.

## Journey 4: Casual browsing out of curiosity
**Trigger:** No specific reason — bored, or heard a name at the club and
wants to know if they're any good.
**Steps:** Open Tennis Stats → search any BTV player → browse their stats.
**Outcome:** Satisfied curiosity in under a minute, no login, no friction.

## Edge case: player with no crawled data yet
**Trigger:** Searched player exists in BTV but hasn't played since
Tennis Stats started crawling, or crawl hasn't caught up yet.
**Steps:** Search returns the player but an empty or near-empty stats
page.
**Outcome:** Must not look broken — a clear "not enough matches yet"
state rather than a blank page or an error. Matters for the Experience
bar in the vision (self-explanatory, no clutter, no confusion).
