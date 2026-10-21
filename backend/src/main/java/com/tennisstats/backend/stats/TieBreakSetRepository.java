package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.RubberSet;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Narrow, query-only repository backing {@link TieBreakService}: every {@link RubberSet} a player
 * took part in that was actually decided by a tie-break, i.e. a {@code MATCH_TIE_BREAK} "set" or a
 * {@code REGULAR} set with a populated tiebreak score, leaving out sets cut short by a retirement.
 */
interface TieBreakSetRepository extends Repository<RubberSet, Long> {

    @Query(
            """
            select new com.tennisstats.backend.stats.TieBreakSetRow(
                s.type, s.homeScore, s.awayScore, s.homeTiebreakScore, s.awayTiebreakScore, rp.side)
            from RubberSet s
              join RubberParticipant rp on rp.rubber = s.rubber
            where rp.player.id = :playerId
              and s.unfinished = false
              and (s.type = com.tennisstats.backend.domain.SetType.MATCH_TIE_BREAK or s.homeTiebreakScore is not null)
            """)
    List<TieBreakSetRow> findTieBreakSetRowsForPlayer(@Param("playerId") Long playerId);
}
