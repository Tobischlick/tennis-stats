package com.tennisstats.backend.stats;

import static org.assertj.core.api.Assertions.assertThat;

import com.tennisstats.backend.domain.Meeting;
import com.tennisstats.backend.domain.Player;
import com.tennisstats.backend.domain.Rubber;
import com.tennisstats.backend.domain.RubberType;
import com.tennisstats.backend.domain.SetType;
import com.tennisstats.backend.domain.Side;
import com.tennisstats.backend.domain.Team;
import com.tennisstats.backend.domain.repository.MeetingRepository;
import com.tennisstats.backend.domain.repository.PlayerRepository;
import com.tennisstats.backend.domain.repository.TeamRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

@DataJpaTest
@Import(TieBreakService.class)
@DirtiesContext
class TieBreakServiceTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private TieBreakService tieBreakService;

    @Test
    void reportsNoDataForAPlayerWithZeroMatches() {
        Player player = playerRepository.save(new Player("Nora", "NoMatches"));

        TieBreakStats stats = tieBreakService.getTieBreakStats(player.getId());

        assertThat(stats.hasData()).isFalse();
        assertThat(stats.tieBreaksPlayed()).isZero();
        assertThat(stats.tieBreaksWon()).isZero();
        assertThat(stats.winRate()).isNull();
    }

    @Test
    void reportsZeroTieBreaksPlayedForAPlayerWhoseSetsNeverWentToABreaker() {
        Team home = teamRepository.save(new Team("TC Home"));
        Team away = teamRepository.save(new Team("TC Away"));
        Player player = playerRepository.save(new Player("Steady", "Sanders"));
        Player opponent = playerRepository.save(new Player("Other", "Opponent"));

        Meeting meeting = new Meeting(home, away, LocalDate.of(2026, 4, 1));
        Rubber rubber = meeting.addRubber(RubberType.SINGLES, 1);
        rubber.addParticipant(player, Side.HOME);
        rubber.addParticipant(opponent, Side.AWAY);
        rubber.addSet(1, SetType.REGULAR, 6, 4);
        rubber.addSet(2, SetType.REGULAR, 6, 2);
        meetingRepository.saveAndFlush(meeting);

        TieBreakStats stats = tieBreakService.getTieBreakStats(player.getId());

        assertThat(stats.hasData()).isFalse();
        assertThat(stats.tieBreaksPlayed()).isZero();
        assertThat(stats.tieBreaksWon()).isZero();
        assertThat(stats.winRate()).isNull();
    }

    @Test
    void doesNotCountAMatchTieBreakCutShortByARetirement() {
        Team home = teamRepository.save(new Team("TC Home"));
        Team away = teamRepository.save(new Team("TC Away"));
        Player player = playerRepository.save(new Player("Rita", "Retired"));
        Player opponent = playerRepository.save(new Player("Other", "Opponent"));

        Meeting meeting = new Meeting(home, away, LocalDate.of(2026, 4, 1));
        Rubber rubber = meeting.addRubber(RubberType.SINGLES, 1);
        rubber.addParticipant(player, Side.HOME);
        rubber.addParticipant(opponent, Side.AWAY);
        rubber.setRetiredSide(Side.HOME);
        rubber.addSet(1, SetType.REGULAR, 6, 7).setTiebreakScore(5, 7);
        rubber.addSet(2, SetType.REGULAR, 6, 3);
        rubber.addSet(3, SetType.MATCH_TIE_BREAK, 10, 10).setUnfinished(true);
        meetingRepository.saveAndFlush(meeting);

        TieBreakStats stats = tieBreakService.getTieBreakStats(player.getId());

        assertThat(stats.tieBreaksPlayed()).isEqualTo(1);
        assertThat(stats.tieBreaksWon()).isZero();
    }

    @Test
    void computesWinRateAcrossMatchTieBreaksAndRegularSetTiebreaksWonAndLost() {
        Team home = teamRepository.save(new Team("TC Home"));
        Team away = teamRepository.save(new Team("TC Away"));
        Player player = playerRepository.save(new Player("Tara", "Tiebreak"));
        Player opponent = playerRepository.save(new Player("Other", "Opponent"));

        // Meeting 1: player plays HOME, wins a match tie-break (won) and loses a regular set's
        // internal tiebreak (lost) in the same rubber.
        Meeting meeting1 = new Meeting(home, away, LocalDate.of(2026, 4, 1));
        Rubber rubber1 = meeting1.addRubber(RubberType.SINGLES, 1);
        rubber1.addParticipant(player, Side.HOME);
        rubber1.addParticipant(opponent, Side.AWAY);
        rubber1.addSet(1, SetType.REGULAR, 4, 6);
        rubber1.addSet(2, SetType.REGULAR, 6, 7).setTiebreakScore(4, 7); // player lost this tiebreak
        rubber1.addSet(3, SetType.MATCH_TIE_BREAK, 10, 8); // player won this match tie-break
        meetingRepository.saveAndFlush(meeting1);

        // Meeting 2: player plays AWAY, wins a regular set's internal tiebreak (won) and loses a
        // match tie-break (lost).
        Meeting meeting2 = new Meeting(home, away, LocalDate.of(2026, 4, 8));
        Rubber rubber2 = meeting2.addRubber(RubberType.SINGLES, 1);
        rubber2.addParticipant(opponent, Side.HOME);
        rubber2.addParticipant(player, Side.AWAY);
        rubber2.addSet(1, SetType.REGULAR, 3, 6);
        rubber2.addSet(2, SetType.REGULAR, 7, 6).setTiebreakScore(5, 7); // player won this tiebreak
        rubber2.addSet(3, SetType.MATCH_TIE_BREAK, 10, 7); // player lost this match tie-break
        meetingRepository.saveAndFlush(meeting2);

        TieBreakStats stats = tieBreakService.getTieBreakStats(player.getId());

        assertThat(stats.hasData()).isTrue();
        assertThat(stats.tieBreaksPlayed()).isEqualTo(4);
        assertThat(stats.tieBreaksWon()).isEqualTo(2);
        assertThat(stats.winRate()).isEqualTo(0.5);
    }
}
