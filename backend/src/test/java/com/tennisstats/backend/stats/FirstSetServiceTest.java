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
@Import(FirstSetService.class)
class FirstSetServiceTest implements PostgresTestcontainer {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private FirstSetService firstSetService;

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
        FirstSetStats stats = firstSetService.getFirstSetStats(anna.getId());

        assertThat(stats.hasData()).isFalse();
        assertThat(stats.lostFirstSet()).isZero();
        assertThat(stats.wonFirstSet()).isZero();
        assertThat(stats.comebackRate()).isNull();
        assertThat(stats.blownLeadRate()).isNull();
    }

    @Test
    void countsAComebackAfterLosingTheFirstSet() {
        Rubber rubber = singlesRubber(anna, bea);
        rubber.addSet(1, SetType.REGULAR, 3, 6);
        rubber.addSet(2, SetType.REGULAR, 6, 4);
        rubber.addSet(3, SetType.MATCH_TIE_BREAK, 10, 7);
        meetingRepository.saveAndFlush(rubber.getMeeting());

        FirstSetStats stats = firstSetService.getFirstSetStats(anna.getId());

        assertThat(stats.lostFirstSet()).isEqualTo(1);
        assertThat(stats.comebacks()).isEqualTo(1);
        assertThat(stats.comebackRate()).isEqualTo(1.0);
        assertThat(stats.wonFirstSet()).isZero();
        assertThat(stats.blownLeadRate()).isNull();
    }

    @Test
    void countsABlownLeadAfterWinningTheFirstSet() {
        Rubber rubber = singlesRubber(anna, bea);
        rubber.addSet(1, SetType.REGULAR, 6, 2);
        rubber.addSet(2, SetType.REGULAR, 4, 6);
        rubber.addSet(3, SetType.REGULAR, 3, 6);
        meetingRepository.saveAndFlush(rubber.getMeeting());

        FirstSetStats stats = firstSetService.getFirstSetStats(anna.getId());

        assertThat(stats.wonFirstSet()).isEqualTo(1);
        assertThat(stats.blownLeads()).isEqualTo(1);
        assertThat(stats.blownLeadRate()).isEqualTo(1.0);
        assertThat(stats.lostFirstSet()).isZero();
        assertThat(stats.comebackRate()).isNull();
    }

    @Test
    void countsStraightSetsWinsAndLossesWithoutAComebackOrBlownLead() {
        Rubber win = singlesRubber(anna, bea);
        win.addSet(1, SetType.REGULAR, 6, 3);
        win.addSet(2, SetType.REGULAR, 7, 6).setTiebreakScore(7, 4);
        meetingRepository.save(win.getMeeting());

        Rubber loss = singlesRubber(anna, bea);
        loss.addSet(1, SetType.REGULAR, 2, 6);
        loss.addSet(2, SetType.REGULAR, 3, 6);
        meetingRepository.saveAndFlush(loss.getMeeting());

        FirstSetStats stats = firstSetService.getFirstSetStats(anna.getId());

        assertThat(stats.wonFirstSet()).isEqualTo(1);
        assertThat(stats.blownLeads()).isZero();
        assertThat(stats.blownLeadRate()).isEqualTo(0.0);
        assertThat(stats.lostFirstSet()).isEqualTo(1);
        assertThat(stats.comebacks()).isZero();
        assertThat(stats.comebackRate()).isEqualTo(0.0);
    }

    @Test
    void readsTheFirstSetFromTheAwaySide() {
        // Bea, playing AWAY, loses the first set 4-6 from her side and comes back.
        Rubber rubber = singlesRubber(anna, bea);
        rubber.addSet(1, SetType.REGULAR, 6, 4);
        rubber.addSet(2, SetType.REGULAR, 3, 6);
        rubber.addSet(3, SetType.MATCH_TIE_BREAK, 5, 10);
        meetingRepository.saveAndFlush(rubber.getMeeting());

        FirstSetStats beaStats = firstSetService.getFirstSetStats(bea.getId());
        FirstSetStats annaStats = firstSetService.getFirstSetStats(anna.getId());

        assertThat(beaStats.lostFirstSet()).isEqualTo(1);
        assertThat(beaStats.comebacks()).isEqualTo(1);
        assertThat(annaStats.wonFirstSet()).isEqualTo(1);
        assertThat(annaStats.blownLeads()).isEqualTo(1);
    }

    @Test
    void excludesARubberDecidedByARetirement() {
        // Anna loses the first set, and Bea retires in the second: not a comeback.
        Rubber rubber = singlesRubber(anna, bea);
        rubber.setRetiredSide(Side.AWAY);
        rubber.addSet(1, SetType.REGULAR, 4, 6);
        rubber.addSet(2, SetType.REGULAR, 3, 1).setUnfinished(true);
        meetingRepository.saveAndFlush(rubber.getMeeting());

        FirstSetStats stats = firstSetService.getFirstSetStats(anna.getId());

        assertThat(stats.hasData()).isFalse();
        assertThat(stats.lostFirstSet()).isZero();
        assertThat(stats.comebacks()).isZero();
    }

    @Test
    void excludesARubberWithAnUnfinishedFirstSet() {
        Rubber rubber = singlesRubber(anna, bea);
        rubber.addSet(1, SetType.REGULAR, 2, 1).setUnfinished(true);
        meetingRepository.saveAndFlush(rubber.getMeeting());

        FirstSetStats stats = firstSetService.getFirstSetStats(anna.getId());

        assertThat(stats.hasData()).isFalse();
    }

    @Test
    void countsDoublesAlongsideSingles() {
        Player clara = playerRepository.save(new Player("Clara", "Conrad"));
        Player dana = playerRepository.save(new Player("Dana", "Diehl"));

        Rubber singles = singlesRubber(anna, bea);
        singles.addSet(1, SetType.REGULAR, 6, 4);
        singles.addSet(2, SetType.REGULAR, 6, 3);
        meetingRepository.save(singles.getMeeting());

        // Anna and Clara, playing AWAY, lose the first set and come back.
        Meeting meeting = new Meeting(home, away, LocalDate.of(2026, 5, 10));
        Rubber doubles = meeting.addRubber(RubberType.DOUBLES, 1);
        doubles.addParticipant(bea, Side.HOME);
        doubles.addParticipant(dana, Side.HOME);
        doubles.addParticipant(anna, Side.AWAY);
        doubles.addParticipant(clara, Side.AWAY);
        doubles.addSet(1, SetType.REGULAR, 6, 4);
        doubles.addSet(2, SetType.REGULAR, 3, 6);
        doubles.addSet(3, SetType.MATCH_TIE_BREAK, 5, 10);
        meetingRepository.saveAndFlush(meeting);

        FirstSetStats stats = firstSetService.getFirstSetStats(anna.getId());

        assertThat(stats.wonFirstSet()).isEqualTo(1);
        assertThat(stats.blownLeads()).isZero();
        assertThat(stats.lostFirstSet()).isEqualTo(1);
        assertThat(stats.comebacks()).isEqualTo(1);
    }
}
