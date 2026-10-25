ALTER TABLE meeting ADD COLUMN has_result BOOLEAN NOT NULL DEFAULT FALSE;

-- meetings given entirely as a walkover have no stored rubbers, so they stay FALSE here and get
-- their flag on the next crawl
UPDATE meeting m
SET has_result = TRUE
WHERE EXISTS (
    SELECT 1 FROM rubber r
    WHERE r.meeting_id = m.id
      AND (r.retired_side IS NOT NULL OR EXISTS (SELECT 1 FROM rubber_set s WHERE s.rubber_id = r.id)));
