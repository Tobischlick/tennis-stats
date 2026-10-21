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
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Import({RecentMatchesService.class, StreakService.class})
@DirtiesContext
class RecentMatchesServiceTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RecentMatchesService recentMatchesService;

    @Autowired
    private StreakService streakService;

    private Team home;
    private Team away;
    private Player anna;
    private Player bea;
    private Player carla;
    private Player dora;

    @BeforeEach
    void setUp() {
        home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        away = teamRepository.save(new Team("TC Rot-Gelb 1"));
        anna = playerRepository.save(new Player("Anna", "Adler"));
        bea = playerRepository.save(new Player("Bea", "Berger"));
        carla = playerRepository.save(new Player("Carla", "Clausen"));
        dora = playerRepository.save(new Player("Dora", "Dietz"));
    }

    private Rubber singles(LocalDate playedOn, Player homePlayer, Player awayPlayer) {
        Meeting meeting = new Meeting(home, away, playedOn);
        Rubber rubber = meeting.addRubber(RubberType.SINGLES, 1);
        rubber.addParticipant(homePlayer, Side.HOME);
        rubber.addParticipant(awayPlayer, Side.AWAY);
        return rubber;
    }

    private void save(Rubber rubber) {
        meetingRepository.saveAndFlush(rubber.getMeeting());
    }

    private void annaWinsHomeSingles(LocalDate playedOn) {
        Rubber rubber = singles(playedOn, anna, bea);
        rubber.addSet(1, SetType.REGULAR, 6, 3);
        rubber.addSet(2, SetType.REGULAR, 6, 4);
        save(rubber);
    }

    private void annaLosesHomeSingles(LocalDate playedOn) {
        Rubber rubber = singles(playedOn, anna, bea);
        rubber.addSet(1, SetType.REGULAR, 3, 6);
        rubber.addSet(2, SetType.REGULAR, 4, 6);
        save(rubber);
    }

    private List<MatchDetail> recentMatchesOfAnna(int limit) {
        entityManager.clear();
        return recentMatchesService.getMatches(anna.getId(), 0, limit).matches();
    }

    private MatchPage matchPageOfAnna(int offset, int limit) {
        entityManager.clear();
        return recentMatchesService.getMatches(anna.getId(), offset, limit);
    }

    @Test
    void describesASinglesRubberFromTheHomePlayersPerspective() {
        annaWinsHomeSingles(LocalDate.of(2026, 5, 3));

        List<MatchDetail> matches = recentMatchesOfAnna(10);

        assertThat(matches).hasSize(1);
        MatchDetail match = matches.get(0);
        assertThat(match.playedOn()).isEqualTo(LocalDate.of(2026, 5, 3));
        assertThat(match.type()).isEqualTo(RubberType.SINGLES);
        assertThat(match.result()).isEqualTo(MatchResult.WIN);
        assertThat(match.opponents()).extracting(Player::getId).containsExactly(bea.getId());
        assertThat(match.partner()).isNull();
        assertThat(match.ownTeam()).isEqualTo("TC Blau-Weiss 1");
        assertThat(match.opponentTeam()).isEqualTo("TC Rot-Gelb 1");
        assertThat(match.sets())
                .containsExactly(new SetScore(6, 3, null, null, false), new SetScore(6, 4, null, null, false));
    }

    @Test
    void flipsScoresAndTeamsForAPlayerOnTheAwaySide() {
        Rubber rubber = singles(LocalDate.of(2026, 5, 3), bea, anna);
        rubber.addSet(1, SetType.REGULAR, 6, 7).setTiebreakScore(4, 7);
        rubber.addSet(2, SetType.REGULAR, 2, 6);
        save(rubber);

        MatchDetail match = recentMatchesOfAnna(10).get(0);

        assertThat(match.result()).isEqualTo(MatchResult.WIN);
        assertThat(match.opponents()).extracting(Player::getId).containsExactly(bea.getId());
        assertThat(match.ownTeam()).isEqualTo("TC Rot-Gelb 1");
        assertThat(match.opponentTeam()).isEqualTo("TC Blau-Weiss 1");
        assertThat(match.sets())
                .containsExactly(new SetScore(7, 6, 7, 4, false), new SetScore(6, 2, null, null, false));
    }

    @Test
    void marksAMatchTieBreakAndKeepsSetOrder() {
        Rubber rubber = singles(LocalDate.of(2026, 5, 3), anna, bea);
        rubber.addSet(3, SetType.MATCH_TIE_BREAK, 8, 10);
        rubber.addSet(1, SetType.REGULAR, 6, 4);
        rubber.addSet(2, SetType.REGULAR, 3, 6);
        save(rubber);

        MatchDetail match = recentMatchesOfAnna(10).get(0);

        assertThat(match.result()).isEqualTo(MatchResult.LOSS);
        assertThat(match.sets())
                .containsExactly(
                        new SetScore(6, 4, null, null, false),
                        new SetScore(3, 6, null, null, false),
                        new SetScore(8, 10, null, null, true));
    }

    @Test
    void namesPartnerAndBothOpponentsForADoublesRubber() {
        Meeting meeting = new Meeting(home, away, LocalDate.of(2026, 5, 3));
        Rubber rubber = meeting.addRubber(RubberType.DOUBLES, 7);
        rubber.addParticipant(carla, Side.AWAY);
        rubber.addParticipant(anna, Side.AWAY);
        rubber.addParticipant(bea, Side.HOME);
        rubber.addParticipant(dora, Side.HOME);
        rubber.addSet(1, SetType.REGULAR, 4, 6);
        rubber.addSet(2, SetType.REGULAR, 6, 3);
        rubber.addSet(3, SetType.MATCH_TIE_BREAK, 7, 10);
        save(rubber);

        MatchDetail match = recentMatchesOfAnna(10).get(0);

        assertThat(match.type()).isEqualTo(RubberType.DOUBLES);
        assertThat(match.result()).isEqualTo(MatchResult.WIN);
        assertThat(match.partner().getId()).isEqualTo(carla.getId());
        assertThat(match.opponents()).extracting(Player::getId).containsExactly(bea.getId(), dora.getId());
        assertThat(match.ownTeam()).isEqualTo("TC Rot-Gelb 1");
        assertThat(match.sets())
                .containsExactly(
                        new SetScore(6, 4, null, null, false),
                        new SetScore(3, 6, null, null, false),
                        new SetScore(10, 7, null, null, true));
    }

    @Test
    void listsNewestFirstAndSkipsUnsettledRubbers() {
        annaWinsHomeSingles(LocalDate.of(2026, 5, 3));
        annaLosesHomeSingles(LocalDate.of(2026, 5, 17));
        Rubber unsettled = singles(LocalDate.of(2026, 5, 24), anna, bea);
        unsettled.addSet(1, SetType.REGULAR, 6, 3);
        unsettled.addSet(2, SetType.REGULAR, 3, 6);
        save(unsettled);
        annaWinsHomeSingles(LocalDate.of(2026, 5, 10));

        List<MatchDetail> matches = recentMatchesOfAnna(10);

        assertThat(matches)
                .extracting(MatchDetail::playedOn)
                .containsExactly(LocalDate.of(2026, 5, 17), LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 3));
    }

    @Test
    void stopsAtTheLimit() {
        for (int day = 1; day <= 12; day++) {
            annaWinsHomeSingles(LocalDate.of(2026, 5, day));
        }

        assertThat(recentMatchesOfAnna(10)).hasSize(10);
        assertThat(recentMatchesOfAnna(3))
                .extracting(MatchDetail::playedOn)
                .containsExactly(LocalDate.of(2026, 5, 12), LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 10));
    }

    @Test
    void pagesThroughAllMatchesWithTheOffset() {
        for (int day = 1; day <= 12; day++) {
            annaWinsHomeSingles(LocalDate.of(2026, 5, day));
        }

        MatchPage secondPage = matchPageOfAnna(5, 5);
        MatchPage lastPage = matchPageOfAnna(10, 5);

        assertThat(secondPage.total()).isEqualTo(12);
        assertThat(secondPage.matches())
                .extracting(MatchDetail::playedOn)
                .containsExactly(
                        LocalDate.of(2026, 5, 7),
                        LocalDate.of(2026, 5, 6),
                        LocalDate.of(2026, 5, 5),
                        LocalDate.of(2026, 5, 4),
                        LocalDate.of(2026, 5, 3));
        assertThat(lastPage.total()).isEqualTo(12);
        assertThat(lastPage.matches())
                .extracting(MatchDetail::playedOn)
                .containsExactly(LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 1));
    }

    @Test
    void countsOnlySettledRubbersInTheTotal() {
        annaWinsHomeSingles(LocalDate.of(2026, 5, 3));
        Rubber unsettled = singles(LocalDate.of(2026, 5, 10), anna, bea);
        unsettled.addSet(1, SetType.REGULAR, 6, 3);
        unsettled.addSet(2, SetType.REGULAR, 3, 6);
        save(unsettled);

        MatchPage page = matchPageOfAnna(0, 10);

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.matches()).hasSize(1);
    }

    @Test
    void returnsAnEmptyPageWithTheTotalForAnOffsetPastTheEnd() {
        annaWinsHomeSingles(LocalDate.of(2026, 5, 3));
        annaWinsHomeSingles(LocalDate.of(2026, 5, 10));

        MatchPage page = matchPageOfAnna(2, 10);

        assertThat(page.matches()).isEmpty();
        assertThat(page.total()).isEqualTo(2);
    }

    @Test
    void agreesWithTheStreaksRecentResults() {
        annaWinsHomeSingles(LocalDate.of(2026, 5, 3));
        annaLosesHomeSingles(LocalDate.of(2026, 5, 10));
        annaWinsHomeSingles(LocalDate.of(2026, 5, 10));
        annaLosesHomeSingles(LocalDate.of(2026, 5, 17));
        annaWinsHomeSingles(LocalDate.of(2026, 5, 17));
        annaLosesHomeSingles(LocalDate.of(2026, 5, 24));

        List<MatchResult> streakResults = streakService.getStreakStats(anna.getId()).recentResults();

        assertThat(recentMatchesOfAnna(streakResults.size()))
                .extracting(MatchDetail::result)
                .containsExactlyElementsOf(streakResults);
    }

    @Test
    void returnsAnEmptyListForAPlayerWithoutMatches() {
        assertThat(matchPageOfAnna(0, 10)).isEqualTo(new MatchPage(List.of(), 0));
    }

    @Test
    void loadsAnyNumberOfMatchesWithAFixedNumberOfQueries() {
        for (int day = 1; day <= 8; day++) {
            annaWinsHomeSingles(LocalDate.of(2026, 5, day));
        }
        entityManager.clear();
        Statistics statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        List<MatchDetail> matches = recentMatchesService.getMatches(anna.getId(), 0, 10).matches();
        matches.forEach(match -> match.opponents().forEach(Player::getLastName));

        assertThat(matches).hasSize(8);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(3);
    }
}
