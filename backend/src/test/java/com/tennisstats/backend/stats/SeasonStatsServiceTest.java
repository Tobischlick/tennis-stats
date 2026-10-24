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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({SeasonStatsService.class, WinLossService.class, TieBreakService.class})
class SeasonStatsServiceTest implements PostgresTestcontainer {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private SeasonStatsService seasonStatsService;

    @Autowired
    private WinLossService winLossService;

    @Autowired
    private TieBreakService tieBreakService;

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

    /** A singles rubber with Anna on {@code annaSide}; scores are home:away per set. */
    private Rubber singles(LocalDate playedOn, Side annaSide, int[]... sets) {
        Meeting meeting = new Meeting(home, away, playedOn);
        Rubber rubber = meeting.addRubber(RubberType.SINGLES, 1);
        rubber.addParticipant(anna, annaSide);
        rubber.addParticipant(bea, annaSide == Side.HOME ? Side.AWAY : Side.HOME);
        for (int i = 0; i < sets.length; i++) {
            SetType type = i == 2 && sets[i][0] + sets[i][1] > 13 ? SetType.MATCH_TIE_BREAK : SetType.REGULAR;
            rubber.addSet(i + 1, type, sets[i][0], sets[i][1]);
        }
        meetingRepository.saveAndFlush(meeting);
        return rubber;
    }

    private static int[] set(int homeScore, int awayScore) {
        return new int[] {homeScore, awayScore};
    }

    @Test
    void returnsNoSeasonsForAPlayerWithoutMatches() {
        assertThat(seasonStatsService.getSeasonStats(anna.getId())).isEmpty();
    }

    @Test
    void groupsAWinterAcrossNewYearIntoOneSeason() {
        singles(LocalDate.of(2025, 11, 15), Side.HOME, set(6, 3), set(6, 4)); // win
        singles(LocalDate.of(2026, 2, 7), Side.AWAY, set(6, 2), set(6, 1)); // loss

        List<SeasonStats> seasons = seasonStatsService.getSeasonStats(anna.getId());

        assertThat(seasons).hasSize(1);
        assertThat(seasons.getFirst().season()).isEqualTo(new Season(Season.Type.WINTER, 2025));
        assertThat(seasons.getFirst().season().label()).isEqualTo("Winter 2025/26");
        assertThat(seasons.getFirst().record().wins()).isEqualTo(1);
        assertThat(seasons.getFirst().record().losses()).isEqualTo(1);
    }

    @Test
    void splitsSeasonsAtTheMonthBoundariesOldestFirst() {
        singles(LocalDate.of(2025, 9, 30), Side.HOME, set(6, 3), set(6, 4)); // Sommer 2025, win
        singles(LocalDate.of(2025, 10, 1), Side.HOME, set(3, 6), set(4, 6)); // Winter 2025/26, loss
        singles(LocalDate.of(2026, 3, 31), Side.HOME, set(6, 0), set(6, 0)); // Winter 2025/26, win
        singles(LocalDate.of(2026, 4, 1), Side.AWAY, set(0, 6), set(0, 6)); // Sommer 2026, win

        List<SeasonStats> seasons = seasonStatsService.getSeasonStats(anna.getId());

        assertThat(seasons).extracting(s -> s.season().label())
                .containsExactly("Sommer 2025", "Winter 2025/26", "Sommer 2026");
        assertThat(seasons).extracting(s -> s.record().wins()).containsExactly(1, 1, 1);
        assertThat(seasons).extracting(s -> s.record().losses()).containsExactly(0, 1, 0);
        assertThat(seasons.get(1).record().winPercentage()).isEqualTo(50.0);
    }

    @Test
    void countsTieBreaksPerSeasonFromThePlayersSide() {
        // Sommer 2026: Anna (AWAY) wins a 6:7 set and loses a match tie-break 10:8.
        singles(LocalDate.of(2026, 5, 3), Side.AWAY, set(6, 7), set(6, 3), set(10, 8));
        // Winter 2026/27: Anna (HOME) wins a 7:6 set.
        singles(LocalDate.of(2026, 11, 8), Side.HOME, set(7, 6), set(6, 2));

        List<SeasonStats> seasons = seasonStatsService.getSeasonStats(anna.getId());

        assertThat(seasons).hasSize(2);
        assertThat(seasons.get(0).tieBreak().tieBreaksPlayed()).isEqualTo(2);
        assertThat(seasons.get(0).tieBreak().tieBreaksWon()).isEqualTo(1);
        assertThat(seasons.get(1).season().label()).isEqualTo("Winter 2026/27");
        assertThat(seasons.get(1).tieBreak().tieBreaksPlayed()).isEqualTo(1);
        assertThat(seasons.get(1).tieBreak().tieBreaksWon()).isEqualTo(1);
    }

    @Test
    void listsASeasonWithOnlyATieBreakInAnUnsettledRubber() {
        singles(LocalDate.of(2026, 5, 3), Side.HOME, set(6, 3), set(6, 4));
        // One set each and nothing more stored: no winner, but the 7:6 still counts as a tie-break.
        singles(LocalDate.of(2026, 10, 10), Side.HOME, set(7, 6), set(3, 6));

        List<SeasonStats> seasons = seasonStatsService.getSeasonStats(anna.getId());

        assertThat(seasons).hasSize(2);
        assertThat(seasons.get(1).season().label()).isEqualTo("Winter 2026/27");
        assertThat(seasons.get(1).record().wins() + seasons.get(1).record().losses()).isZero();
        assertThat(seasons.get(1).tieBreak().tieBreaksPlayed()).isEqualTo(1);
    }

    @Test
    void seasonTotalsAddUpToTheOverallRecord() {
        singles(LocalDate.of(2025, 5, 4), Side.HOME, set(7, 6), set(6, 4)); // win, TB won
        singles(LocalDate.of(2025, 9, 30), Side.AWAY, set(6, 4), set(3, 6), set(10, 7)); // loss, MTB lost
        singles(LocalDate.of(2025, 11, 15), Side.HOME, set(6, 7), set(7, 6), set(10, 12)); // loss, 1 of 3
        singles(LocalDate.of(2026, 2, 7), Side.AWAY, set(2, 6), set(6, 7)); // win, TB won
        Rubber walkover = singles(LocalDate.of(2026, 3, 31), Side.HOME);
        walkover.setRetiredSide(Side.AWAY); // win by walkover
        meetingRepository.flush();
        singles(LocalDate.of(2026, 4, 1), Side.HOME, set(6, 7), set(6, 7)); // loss, 2 TBs lost
        singles(LocalDate.of(2026, 8, 9), Side.HOME, set(6, 3), set(6, 1)); // win
        // Doubles with a partner count as well.
        Player carla = playerRepository.save(new Player("Carla", "Clausen"));
        Player dora = playerRepository.save(new Player("Dora", "Dietz"));
        Player eva = playerRepository.save(new Player("Eva", "Engel"));
        Meeting doublesMeeting = new Meeting(home, away, LocalDate.of(2026, 10, 1));
        Rubber doubles = doublesMeeting.addRubber(RubberType.DOUBLES, 7);
        doubles.addParticipant(anna, Side.HOME);
        doubles.addParticipant(carla, Side.HOME);
        doubles.addParticipant(dora, Side.AWAY);
        doubles.addParticipant(eva, Side.AWAY);
        doubles.addSet(1, SetType.REGULAR, 7, 6);
        doubles.addSet(2, SetType.REGULAR, 6, 4);
        meetingRepository.saveAndFlush(doublesMeeting);

        List<SeasonStats> seasons = seasonStatsService.getSeasonStats(anna.getId());
        WinLossRecord overall = winLossService.getWinLossRecord(anna.getId());
        TieBreakStats overallTieBreaks = tieBreakService.getTieBreakStats(anna.getId());

        assertThat(seasons).extracting(s -> s.season().label())
                .containsExactly("Sommer 2025", "Winter 2025/26", "Sommer 2026", "Winter 2026/27");
        assertThat(seasons.stream().mapToInt(s -> s.record().wins()).sum()).isEqualTo(overall.wins());
        assertThat(seasons.stream().mapToInt(s -> s.record().losses()).sum()).isEqualTo(overall.losses());
        assertThat(seasons.stream().mapToLong(s -> s.tieBreak().tieBreaksPlayed()).sum())
                .isEqualTo(overallTieBreaks.tieBreaksPlayed());
        assertThat(seasons.stream().mapToLong(s -> s.tieBreak().tieBreaksWon()).sum())
                .isEqualTo(overallTieBreaks.tieBreaksWon());
        assertThat(overall.wins()).isEqualTo(5);
        assertThat(overall.losses()).isEqualTo(3);
        assertThat(overallTieBreaks.tieBreaksPlayed()).isEqualTo(9);
        assertThat(overallTieBreaks.tieBreaksWon()).isEqualTo(4);
    }
}
