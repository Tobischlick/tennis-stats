# 8. Crosscutting Concepts: UI Concept

How every Tennis Stats screen looks and behaves. It turns the
[vision](../01_introduction_and_goals/vision.md)'s Experience bar ("clean and
self-explanatory, no manual, no legend, no clutter") into rules the frontend
stories build on. The direction was chosen from three clickable prototypes in
`prototype/`; the reference
implementation is `prototype/app/clean/` with its components in
`prototype/components/clean/`. The prototype is throwaway: the real app in
`frontend/` rebuilds it, it does not copy it.

## Visual language

| Aspect | Rule |
|---|---|
| Palette | Near-white background, cool grey neutrals and exactly one accent (cobalt blue, `oklch(0.52 0.19 262)`). No red/green for win/loss. |
| Accent use | Wins, key numbers, meters, focus rings, links and the wordmark dot. Nothing else. |
| Typography | Manrope, tabular numerals everywhere so figures line up. |
| Numbers | The headline figure of each card is large (5xl–6xl); supporting text is small and muted. |
| Surfaces | Rounded cards (about 1.5 rem radius) with thin borders, no heavy shadows, generous whitespace. |
| Language | German UI copy; numbers and dates in German format (`64 %`, `20.09.`). |

The palette lives as shadcn/ui theme tokens (`--primary`, `--muted-foreground`,
…), so Tailwind classes like `bg-primary` pick up the accent.

## Encoding results

- A win is a filled accent dot, a loss a hollow grey ring. The same encoding is
  used in the form row, the match list and head-to-head.
- Result rows read newest first. The newest item is marked (extra ring or
  "Zuletzt"), and a short caption ("← Neuestes") states the order.
- No legend or key: meaning comes from the text next to the marks, such as
  "3 Siege in Folge" or "7 von 10 gewonnen".
- Percentages come with a plain-language verdict where a player would otherwise
  have to interpret them. Tie-break rate: below 40 % "Wackelt im Tie-Break",
  40–60 % "ausgeglichen", above 60 % "Stark im Tie-Break".
- Below 5 matches, a short note says the numbers may still change.

## Screens

| Screen | Structure |
|---|---|
| Search | Large centred heading and a tall search field. Results show name and club. The query is in the URL (`?q=`) so a search can be shared. |
| Player stats | Header with initials, name, club and match count. A one-sentence summary ("Anna gewinnt 64 % der Matches und 36 % der Tie-Breaks – aktuell 3 Siege in Folge"). Cards: Tie-Break-Quote, Aktuelle Form, Bilanz, Letzte Matches (5 visible, rest expandable, plus an "Alle Matches" link). Then "Vergleichen mit …". |
| All matches | Back link to the player, "Alle Matches" heading with name and match count, then every match newest first in one card per year (same rows as Letzte Matches), 50 per page with "Neuere"/"Ältere" links and "Seite x von y". The page is in the URL (`?seite=`). |
| Head-to-head | Both players with the singles score between them, a leader line with full names (first names can clash), recent singles meetings, "Seiten tauschen", links back to both stats pages. Doubles on opposite sides are a separate record, never mixed into the singles score: a line "Einzel 1:0 · Doppel 0:1" under the score and a list of doubles meetings naming both partners, with set scores. Doubles played together as partners don't count. |

## Empty and edge states

States that come from missing data are never styled as errors.

- **No matches yet:** a neutral card with an icon, "Noch nicht genug Matches",
  a sentence on when data will appear and a link to search again. The compare
  picker stays available.
- **No matches in the full list:** "Noch keine Matches" with a link back to the
  player's stats.
- **Never played each other:** "Noch kein direktes Duell" with links to both
  players' stats. Only when they have met neither in singles nor in doubles;
  if they only met in doubles, the page shows the doubles record and a short
  line that there is no singles meeting yet.
- **No search results:** "Kein Spieler gefunden" with a spelling hint.

## Loading and error states

- **Loading:** skeletons in the shape of the page that is coming (search
  results, stats header and cards, head-to-head), in a light pulsing grey. No
  spinners and no "Laden …" text beyond what screen readers announce.
- **Backend unreachable or failing:** "Daten gerade nicht erreichbar" with a
  cloud icon, a sentence that it is not the player's fault, an "Erneut
  versuchen" button and a link back to the search. Calm and grey like the
  empty states, but clearly a temporary problem rather than missing data.
- **Unknown player id:** "Spieler nicht gefunden" with a link to the search.
- **Compare picker:** if the opponent search fails, one muted line in the
  result list asks to try again; the rest of the page stays usable.

## Layout

Mobile-first. Phones get a single column with no horizontal scrolling down to
320 px. From the `md:` breakpoint the stats page uses two columns
(Tie-Break-Quote and Aktuelle Form side by side, Bilanz and Letzte Matches full
width). Content is centred with a maximum width of about 56 rem.
