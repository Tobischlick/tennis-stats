package com.tennisstats.backend.stats;

import static org.assertj.core.api.Assertions.assertThat;

import com.tennisstats.backend.PostgresTestcontainer;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(DecidingSetService.class)
class DecidingSetServiceTest implements PostgresTestcontainer {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private DecidingSetService decidingSetService;

    private Team home;
    private Team away;
    private Player anna;
    private Player bea;

    @BeforeEach
    void setUp() {
        home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        away = teamRepository.save(new Team("TC Rot-Gelb 1"));
        anna = playerRepository.save(new Player("Anna", "Adler"));
        bea = playerRepository.save(new Player("Bea", "Berger"));
    }

    private Rubber singlesRubber(Player homePlayer, Player awayPlayer) {
        Meeting meeting = new Meeting(home, away, LocalDate.of(2026, 5, 3));
        Rubber rubber = meeting.addRubber(RubberType.SINGLES, 1);
        rubber.addParticipant(homePlayer, Side.HOME);
        rubber.addParticipant(awayPlayer, Side.AWAY);
        return rubber;
    }

    @Test
    void reportsNoDataForAPlayerWithZeroMatches() {
        DecidingSetStats stats = decidingSetService.getDecidingSetStats(anna.getId());

        assertThat(stats.hasData()).isFalse();
        assertThat(stats.played()).isZero();
        assertThat(stats.won()).isZero();
        assertThat(stats.winRate()).isNull();
    }

    @Test
    void doesNotCountTwoSetWinsOrLosses() {
        Rubber win = singlesRubber(anna, bea);
        win.addSet(1, SetType.REGULAR, 6, 3);
        win.addSet(2, SetType.REGULAR, 7, 6).setTiebreakScore(7, 4);
        meetingRepository.save(win.getMeeting());

        Rubber loss = singlesRubber(anna, bea);
        loss.addSet(1, SetType.REGULAR, 2, 6);
        loss.addSet(2, SetType.REGULAR, 3, 6);
        meetingRepository.saveAndFlush(loss.getMeeting());

        DecidingSetStats stats = decidingSetService.getDecidingSetStats(anna.getId());

        assertThat(stats.hasData()).isFalse();
        assertThat(stats.played()).isZero();
        assertThat(stats.winRate()).isNull();
    }

    @Test
    void countsARegularThirdSet() {
        Rubber rubber = singlesRubber(anna, bea);
        rubber.addSet(1, SetType.REGULAR, 6, 4);
        rubber.addSet(2, SetType.REGULAR, 3, 6);
        rubber.addSet(3, SetType.REGULAR, 6, 2);
        meetingRepository.saveAndFlush(rubber.getMeeting());

        DecidingSetStats stats = decidingSetService.getDecidingSetStats(anna.getId());

        assertThat(stats.played()).isEqualTo(1);
        assertThat(stats.won()).isEqualTo(1);
        assertThat(stats.winRate()).isEqualTo(1.0);
    }

    @Test
    void countsAMatchTieBreak() {
        Rubber rubber = singlesRubber(anna, bea);
        rubber.addSet(1, SetType.REGULAR, 6, 4);
        rubber.addSet(2, SetType.REGULAR, 3, 6);
        rubber.addSet(3, SetType.MATCH_TIE_BREAK, 8, 10);
        meetingRepository.saveAndFlush(rubber.getMeeting());

        DecidingSetStats stats = decidingSetService.getDecidingSetStats(anna.getId());

        assertThat(stats.played()).isEqualTo(1);
        assertThat(stats.won()).isZero();
        assertThat(stats.winRate()).isEqualTo(0.0);
    }

    @Test
    void readsTheResultFromTheAwaySide() {
        // Bea loses a match tie-break from the AWAY side; Anna wins a regular third set from the AWAY side.
        Rubber lost = singlesRubber(anna, bea);
        lost.addSet(1, SetType.REGULAR, 6, 4);
        lost.addSet(2, SetType.REGULAR, 3, 6);
        lost.addSet(3, SetType.MATCH_TIE_BREAK, 10, 7);
        meetingRepository.save(lost.getMeeting());

        Rubber won = singlesRubber(bea, anna);
        won.addSet(1, SetType.REGULAR, 6, 4);
        won.addSet(2, SetType.REGULAR, 3, 6);
        won.addSet(3, SetType.REGULAR, 4, 6);
        meetingRepository.saveAndFlush(won.getMeeting());

        DecidingSetStats beaStats = decidingSetService.getDecidingSetStats(bea.getId());
        DecidingSetStats annaStats = decidingSetService.getDecidingSetStats(anna.getId());

        assertThat(beaStats.played()).isEqualTo(2);
        assertThat(beaStats.won()).isZero();
        assertThat(annaStats.played()).isEqualTo(2);
        assertThat(annaStats.won()).isEqualTo(2);
    }

    @Test
    void doesNotCountAThirdSetCutShortByARetirement() {
        Rubber rubber = singlesRubber(anna, bea);
        rubber.setRetiredSide(Side.AWAY);
        rubber.addSet(1, SetType.REGULAR, 6, 4);
        rubber.addSet(2, SetType.REGULAR, 3, 6);
        rubber.addSet(3, SetType.REGULAR, 2, 1).setUnfinished(true);
        meetingRepository.saveAndFlush(rubber.getMeeting());

        DecidingSetStats stats = decidingSetService.getDecidingSetStats(anna.getId());

        assertThat(stats.hasData()).isFalse();
        assertThat(stats.played()).isZero();
    }

    @Test
    void countsDoublesAlongsideSingles() {
        Player clara = playerRepository.save(new Player("Clara", "Conrad"));
        Player dana = playerRepository.save(new Player("Dana", "Diehl"));

        Rubber singles = singlesRubber(anna, bea);
        singles.addSet(1, SetType.REGULAR, 6, 4);
        singles.addSet(2, SetType.REGULAR, 3, 6);
        singles.addSet(3, SetType.MATCH_TIE_BREAK, 10, 8);
        meetingRepository.save(singles.getMeeting());

        Meeting meeting = new Meeting(home, away, LocalDate.of(2026, 5, 10));
        Rubber doubles = meeting.addRubber(RubberType.DOUBLES, 1);
        doubles.addParticipant(bea, Side.HOME);
        doubles.addParticipant(dana, Side.HOME);
        doubles.addParticipant(anna, Side.AWAY);
        doubles.addParticipant(clara, Side.AWAY);
        doubles.addSet(1, SetType.REGULAR, 6, 4);
        doubles.addSet(2, SetType.REGULAR, 3, 6);
        doubles.addSet(3, SetType.MATCH_TIE_BREAK, 10, 5);
        meetingRepository.saveAndFlush(meeting);

        DecidingSetStats stats = decidingSetService.getDecidingSetStats(anna.getId());

        assertThat(stats.played()).isEqualTo(2);
        assertThat(stats.won()).isEqualTo(1);
        assertThat(stats.winRate()).isEqualTo(0.5);
    }
}
