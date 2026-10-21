package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.Meeting;
import com.tennisstats.backend.domain.Rubber;
import com.tennisstats.backend.domain.RubberType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Narrow, query-only repository backing {@link OverviewService}: database-wide counts and rows. */
interface OverviewRepository extends Repository<Meeting, Long> {

    @Query("select count(p) from Player p")
    long countPlayers();

    @Query("select t.name from Team t")
    List<String> findTeamNames();

    @Query("select count(m) from Meeting m")
    long countMeetings();

    @Query("select min(m.playedOn) from Meeting m")
    LocalDate findFirstPlayedOn();

    @Query("select max(m.playedOn) from Meeting m")
    LocalDate findLastPlayedOn();

    @Query("select count(r) from Rubber r where r.type = :type")
    long countRubbers(@Param("type") RubberType type);

    @Query("select count(s) from RubberSet s where s.unfinished = false")
    long countFinishedSets();

    // nuLiga reports a tie-break set as plain "7:6" without the breaker score, so the score is what identifies it.
    @Query(
            """
            select count(s) from RubberSet s
            where s.unfinished = false
              and s.type = com.tennisstats.backend.domain.SetType.REGULAR
              and (s.homeTiebreakScore is not null
                or (s.homeScore = 7 and s.awayScore = 6)
                or (s.homeScore = 6 and s.awayScore = 7))
            """)
    long countTieBreaks();

    @Query(
            """
            select count(s) from RubberSet s
            where s.unfinished = false
              and s.type = com.tennisstats.backend.domain.SetType.MATCH_TIE_BREAK
            """)
    long countMatchTieBreaks();

    @Query("select distinct r from Rubber r join fetch r.meeting left join fetch r.sets")
    List<Rubber> findRubbersWithSets();

    @Query(
            """
            select new com.tennisstats.backend.stats.ParticipationRow(rp.player.id, rp.rubber.id, rp.side)
            from RubberParticipant rp
            """)
    List<ParticipationRow> findParticipations();
}
