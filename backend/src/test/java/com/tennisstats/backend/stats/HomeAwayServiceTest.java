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
@Import({HomeAwayService.class, WinLossService.class})
class HomeAwayServiceTest implements PostgresTestcontainer {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private HomeAwayService homeAwayService;

    @Autowired
    private WinLossService winLossService;

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

    private void homeWins(Player homePlayer, Player awayPlayer) {
        Rubber rubber = singlesRubber(homePlayer, awayPlayer);
        rubber.addSet(1, SetType.REGULAR, 6, 3);
        rubber.addSet(2, SetType.REGULAR, 6, 4);
        meetingRepository.saveAndFlush(rubber.getMeeting());
    }

    private void awayWins(Player homePlayer, Player awayPlayer) {
        Rubber rubber = singlesRubber(homePlayer, awayPlayer);
        rubber.addSet(1, SetType.REGULAR, 3, 6);
        rubber.addSet(2, SetType.REGULAR, 4, 6);
        meetingRepository.saveAndFlush(rubber.getMeeting());
    }

    @Test
    void reportsNoDataForAPlayerWithZeroMatches() {
        HomeAwayStats stats = homeAwayService.getHomeAwayStats(anna.getId());

        assertThat(stats.hasData()).isFalse();
        assertThat(stats).isEqualTo(HomeAwayStats.noData());
    }

    @Test
    void countsAWinAtHome() {
        homeWins(anna, bea);

        HomeAwayStats stats = homeAwayService.getHomeAwayStats(anna.getId());

        assertThat(stats).isEqualTo(new HomeAwayStats(1, 0, 0, 0));
    }

    @Test
    void countsALossAway() {
        homeWins(bea, anna);

        HomeAwayStats stats = homeAwayService.getHomeAwayStats(anna.getId());

        assertThat(stats).isEqualTo(new HomeAwayStats(0, 0, 0, 1));
    }

    @Test
    void splitsBothSidesOfTheSameRubber() {
        awayWins(anna, bea);

        assertThat(homeAwayService.getHomeAwayStats(anna.getId())).isEqualTo(new HomeAwayStats(0, 1, 0, 0));
        assertThat(homeAwayService.getHomeAwayStats(bea.getId())).isEqualTo(new HomeAwayStats(0, 0, 1, 0));
    }

    @Test
    void countsARetirementLikeTheWinLossRecord() {
        // Bea, playing AWAY, retires: a home win for Anna.
        Rubber rubber = singlesRubber(anna, bea);
        rubber.setRetiredSide(Side.AWAY);
        rubber.addSet(1, SetType.REGULAR, 4, 6);
        rubber.addSet(2, SetType.REGULAR, 3, 1).setUnfinished(true);
        meetingRepository.saveAndFlush(rubber.getMeeting());

        HomeAwayStats stats = homeAwayService.getHomeAwayStats(anna.getId());

        assertThat(stats).isEqualTo(new HomeAwayStats(1, 0, 0, 0));
    }

    @Test
    void skipsARubberWithoutAWinner() {
        // One set each and no deciding set recorded.
        Rubber rubber = singlesRubber(anna, bea);
        rubber.addSet(1, SetType.REGULAR, 6, 4);
        rubber.addSet(2, SetType.REGULAR, 4, 6);
        meetingRepository.saveAndFlush(rubber.getMeeting());

        HomeAwayStats stats = homeAwayService.getHomeAwayStats(anna.getId());

        assertThat(stats.hasData()).isFalse();
    }

    @Test
    void countsDoublesAlongsideSingles() {
        Player clara = playerRepository.save(new Player("Clara", "Conrad"));
        Player dana = playerRepository.save(new Player("Dana", "Diehl"));

        homeWins(anna, bea);

        // Anna and Clara, playing AWAY, win the doubles.
        Meeting meeting = new Meeting(home, away, LocalDate.of(2026, 5, 10));
        Rubber doubles = meeting.addRubber(RubberType.DOUBLES, 1);
        doubles.addParticipant(bea, Side.HOME);
        doubles.addParticipant(dana, Side.HOME);
        doubles.addParticipant(anna, Side.AWAY);
        doubles.addParticipant(clara, Side.AWAY);
        doubles.addSet(1, SetType.REGULAR, 4, 6);
        doubles.addSet(2, SetType.REGULAR, 2, 6);
        meetingRepository.saveAndFlush(meeting);

        HomeAwayStats stats = homeAwayService.getHomeAwayStats(anna.getId());

        assertThat(stats).isEqualTo(new HomeAwayStats(1, 0, 1, 0));
    }

    @Test
    void homeAndAwayAddUpToTheOverallRecord() {
        homeWins(anna, bea);
        homeWins(anna, bea);
        awayWins(anna, bea);
        homeWins(bea, anna);
        awayWins(bea, anna);
        awayWins(bea, anna);
        Rubber unsettled = singlesRubber(anna, bea);
        unsettled.addSet(1, SetType.REGULAR, 6, 4);
        unsettled.addSet(2, SetType.REGULAR, 4, 6);
        meetingRepository.saveAndFlush(unsettled.getMeeting());

        HomeAwayStats stats = homeAwayService.getHomeAwayStats(anna.getId());
        WinLossRecord overall = winLossService.getWinLossRecord(anna.getId());

        assertThat(stats).isEqualTo(new HomeAwayStats(2, 1, 2, 1));
        assertThat(stats.homeWins() + stats.awayWins()).isEqualTo(overall.wins());
        assertThat(stats.homeLosses() + stats.awayLosses()).isEqualTo(overall.losses());
    }
}
