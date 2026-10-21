package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.RubberParticipant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Narrow, query-only repository backing {@link PlayerClubService}: the team each player played for
 * on their most recent meeting date, newest meeting first.
 */
interface PlayerClubRepository extends Repository<RubberParticipant, Long> {

    @Query(
            """
            select new com.tennisstats.backend.stats.PlayerTeamRow(
                rp.player.id,
                case when rp.side = com.tennisstats.backend.domain.Side.HOME then h.name else a.name end)
            from RubberParticipant rp
              join rp.rubber r
              join r.meeting m
              join m.homeTeam h
              join m.awayTeam a
            where rp.player.id in :playerIds
              and m.playedOn = (
                select max(m2.playedOn) from RubberParticipant rp2
                  join rp2.rubber r2
                  join r2.meeting m2
                where rp2.player = rp.player)
            order by m.id desc
            """)
    List<PlayerTeamRow> findTeamsOnMostRecentMeetingDate(@Param("playerIds") Collection<Long> playerIds);
}
