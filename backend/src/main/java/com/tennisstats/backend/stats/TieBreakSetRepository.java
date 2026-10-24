package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.RubberSet;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Narrow, query-only repository backing {@link TieBreakService} and {@link SeasonStatsService}: every {@link RubberSet} a player
 * took part in that was decided by a tie-break, i.e. a {@code MATCH_TIE_BREAK} "set" or a
 * {@code REGULAR} set that ended 7:6/6:7 or has a stored tiebreak score, leaving out sets cut short
 * by a retirement. Matches {@link OverviewRepository}'s tie-break definition.
 */
interface TieBreakSetRepository extends Repository<RubberSet, Long> {

    // nuLiga reports a tie-break set as plain "7:6" without the breaker score, so the score is what identifies it.
    @Query(
            """
            select new com.tennisstats.backend.stats.TieBreakSetRow(s.homeScore, s.awayScore, rp.side, m.playedOn)
            from RubberSet s
              join RubberParticipant rp on rp.rubber = s.rubber
              join s.rubber.meeting m
            where rp.player.id = :playerId
              and s.unfinished = false
              and (s.type = com.tennisstats.backend.domain.SetType.MATCH_TIE_BREAK
                or s.homeTiebreakScore is not null
                or (s.homeScore = 7 and s.awayScore = 6)
                or (s.homeScore = 6 and s.awayScore = 7))
            """)
    List<TieBreakSetRow> findTieBreakSetRowsForPlayer(@Param("playerId") Long playerId);
}
