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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

@DataJpaTest
@Import(OverviewService.class)
@DirtiesContext
class OverviewServiceTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private OverviewService overviewService;

    @Test
    void reportsZerosAndNullsOnAnEmptyDatabase() {
        DatabaseOverview overview = overviewService.getOverview();

        assertThat(overview).isEqualTo(new DatabaseOverview(0, 0, 0, 0, 0, 0, 0, null, null, 0, 0, 0, 0, null, 0));
    }

    @Nested
    class WithFixtureData {

        private DatabaseOverview overview;

        /*
         * Four meetings with Anna, Bea, Carla and Dora; Ella never plays. Per rubber (home first):
         * 05-03 S Anna-Bea 6:4 6:3 | S Carla-Dora 7:6 3:6 10:8 | D Anna/Carla-Bea/Dora 4:6 3:6
         * 05-10 S Bea-Anna 3:6 6:7 | S Dora-Carla 6:4 4:6 10:9, Dora retires | D Bea/Dora-Anna/Carla walkover
         * 05-17 S Anna-Dora 6:4 6:3 | S Carla-Bea 6:7 7:6 6:10 | D Anna/Carla-Bea/Dora 6:4 4:6, unsettled
         * 05-24 S Anna-Bea 6:1 6:1
         */
        @BeforeEach
        void setUp() {
            Team blauWeiss1 = teamRepository.save(new Team("TC Blau-Weiss 1"));
            Team blauWeiss2 = teamRepository.save(new Team("TC Blau-Weiss 2"));
            Team rotGelb1 = teamRepository.save(new Team("TC Rot-Gelb 1"));
            Player anna = playerRepository.save(new Player("Anna", "Adler"));
            Player bea = playerRepository.save(new Player("Bea", "Berger"));
            Player carla = playerRepository.save(new Player("Carla", "Clausen"));
            Player dora = playerRepository.save(new Player("Dora", "Dietz"));
            playerRepository.save(new Player("Ella", "Engel"));

            Meeting first = new Meeting(blauWeiss1, rotGelb1, LocalDate.of(2026, 5, 3));
            Rubber r = singles(first, 1, anna, bea);
            r.addSet(1, SetType.REGULAR, 6, 4);
            r.addSet(2, SetType.REGULAR, 6, 3);
            r = singles(first, 2, carla, dora);
            r.addSet(1, SetType.REGULAR, 7, 6);
            r.addSet(2, SetType.REGULAR, 3, 6);
            r.addSet(3, SetType.MATCH_TIE_BREAK, 10, 8);
            r = doubles(first, 3, anna, carla, bea, dora);
            r.addSet(1, SetType.REGULAR, 4, 6);
            r.addSet(2, SetType.REGULAR, 3, 6);
            meetingRepository.save(first);

            Meeting second = new Meeting(rotGelb1, blauWeiss2, LocalDate.of(2026, 5, 10));
            r = singles(second, 1, bea, anna);
            r.addSet(1, SetType.REGULAR, 3, 6);
            r.addSet(2, SetType.REGULAR, 6, 7);
            r = singles(second, 2, dora, carla);
            r.setRetiredSide(Side.HOME);
            r.addSet(1, SetType.REGULAR, 6, 4);
            r.addSet(2, SetType.REGULAR, 4, 6);
            r.addSet(3, SetType.MATCH_TIE_BREAK, 10, 9).setUnfinished(true);
            r = doubles(second, 3, bea, dora, anna, carla);
            r.setRetiredSide(Side.HOME);
            meetingRepository.save(second);

            Meeting third = new Meeting(blauWeiss1, rotGelb1, LocalDate.of(2026, 5, 17));
            r = singles(third, 1, anna, dora);
            r.addSet(1, SetType.REGULAR, 6, 4);
            r.addSet(2, SetType.REGULAR, 6, 3);
            r = singles(third, 2, carla, bea);
            r.addSet(1, SetType.REGULAR, 6, 7);
            r.addSet(2, SetType.REGULAR, 7, 6);
            r.addSet(3, SetType.MATCH_TIE_BREAK, 6, 10);
            r = doubles(third, 3, anna, carla, bea, dora);
            r.addSet(1, SetType.REGULAR, 6, 4);
            r.addSet(2, SetType.REGULAR, 4, 6);
            meetingRepository.save(third);

            Meeting fourth = new Meeting(blauWeiss1, rotGelb1, LocalDate.of(2026, 5, 24));
            r = singles(fourth, 1, anna, bea);
            r.addSet(1, SetType.REGULAR, 6, 1);
            r.addSet(2, SetType.REGULAR, 6, 1);
            meetingRepository.saveAndFlush(fourth);

            overview = overviewService.getOverview();
        }

        @Test
        void countsPlayersClubsTeamsAndMeetings() {
            assertThat(overview.players()).isEqualTo(5);
            assertThat(overview.clubs()).isEqualTo(2);
            assertThat(overview.teams()).isEqualTo(3);
            assertThat(overview.meetings()).isEqualTo(4);
        }

        @Test
        void countsEveryStoredRubberIncludingWalkoversRetirementsAndUnsettledOnes() {
            assertThat(overview.singlesMatches()).isEqualTo(7);
            assertThat(overview.doublesMatches()).isEqualTo(3);
        }

        @Test
        void countsFinishedSetsIncludingMatchTieBreaksButNotSetsCutShort() {
            assertThat(overview.sets()).isEqualTo(20);
        }

        @Test
        void reportsTheDateRangeOfTheMeetings() {
            assertThat(overview.firstPlayedOn()).isEqualTo(LocalDate.of(2026, 5, 3));
            assertThat(overview.lastPlayedOn()).isEqualTo(LocalDate.of(2026, 5, 24));
        }

        @Test
        void countsSevenSixSetsAsTieBreaks() {
            assertThat(overview.tieBreaks()).isEqualTo(4);
        }

        @Test
        void countsFinishedMatchTieBreaksOnly() {
            assertThat(overview.matchTieBreaks()).isEqualTo(2);
        }

        @Test
        void countsDecidingSetsAmongCompletedMatchesOnly() {
            assertThat(overview.completedMatches()).isEqualTo(7);
            assertThat(overview.decidingSetMatches()).isEqualTo(2);
        }

        @Test
        void findsTheMostCommonScoreLineFromTheWinnersPerspective() {
            assertThat(overview.mostCommonScoreLine()).isEqualTo(new ScoreLineCount("6:4 6:3", 3));
        }

        @Test
        void countsWalkoversInWinStreaksAndSkipsUnsettledRubbers() {
            assertThat(overview.longestWinStreak()).isEqualTo(4);
        }
    }

    @Test
    void breaksAScoreLineTieAlphabetically() {
        Team home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        Team away = teamRepository.save(new Team("TC Rot-Gelb 1"));
        Player anna = playerRepository.save(new Player("Anna", "Adler"));
        Player bea = playerRepository.save(new Player("Bea", "Berger"));
        Meeting meeting = new Meeting(home, away, LocalDate.of(2026, 5, 3));
        Rubber r = singles(meeting, 1, anna, bea);
        r.addSet(1, SetType.REGULAR, 6, 2);
        r.addSet(2, SetType.REGULAR, 6, 2);
        r = singles(meeting, 2, bea, anna);
        r.addSet(1, SetType.REGULAR, 6, 0);
        r.addSet(2, SetType.REGULAR, 6, 0);
        meetingRepository.saveAndFlush(meeting);

        assertThat(overviewService.getOverview().mostCommonScoreLine()).isEqualTo(new ScoreLineCount("6:0 6:0", 1));
    }

    private static Rubber singles(Meeting meeting, int position, Player homePlayer, Player awayPlayer) {
        Rubber rubber = meeting.addRubber(RubberType.SINGLES, position);
        rubber.addParticipant(homePlayer, Side.HOME);
        rubber.addParticipant(awayPlayer, Side.AWAY);
        return rubber;
    }

    private static Rubber doubles(
            Meeting meeting, int position, Player home1, Player home2, Player away1, Player away2) {
        Rubber rubber = meeting.addRubber(RubberType.DOUBLES, position);
        rubber.addParticipant(home1, Side.HOME);
        rubber.addParticipant(home2, Side.HOME);
        rubber.addParticipant(away1, Side.AWAY);
        rubber.addParticipant(away2, Side.AWAY);
        return rubber;
    }
}
