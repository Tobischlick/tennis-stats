-- nuLiga's "unbekannt / wird nachgenannt" placeholder was stored as a player before the crawler
-- learned to leave it out; its rubbers stay, just without it as a participant
DELETE FROM rubber_participant
WHERE player_id IN (SELECT id FROM player WHERE last_name LIKE 'unbekannt / wird nachgenannt%');

DELETE FROM player
WHERE last_name LIKE 'unbekannt / wird nachgenannt%';
