package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.Rubber;
import com.tennisstats.backend.domain.RubberParticipant;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Narrow, query-only repository backing {@link RecentMatchesService}. Sets and participants are
 * fetched in separate queries because Hibernate can't join-fetch two bags at once.
 */
interface RecentMatchRepository extends Repository<RubberParticipant, Long> {

    @Query(
            """
            select rp from RubberParticipant rp
              join fetch rp.rubber r
              join fetch r.meeting m
              join fetch m.homeTeam
              join fetch m.awayTeam
            where rp.player.id = :playerId
            order by m.playedOn desc, rp.id
            """)
    List<RubberParticipant> findParticipationsNewestFirst(@Param("playerId") Long playerId);

    @Query("select distinct r from Rubber r left join fetch r.sets where r in :rubbers")
    List<Rubber> fetchSets(@Param("rubbers") List<Rubber> rubbers);

    @Query("select distinct r from Rubber r join fetch r.participants p join fetch p.player where r in :rubbers")
    List<Rubber> fetchParticipants(@Param("rubbers") List<Rubber> rubbers);
}
