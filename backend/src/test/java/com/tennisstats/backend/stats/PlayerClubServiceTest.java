package com.tennisstats.backend.stats;

import static org.assertj.core.api.Assertions.assertThat;

import com.tennisstats.backend.PostgresTestcontainer;
import com.tennisstats.backend.domain.Meeting;
import com.tennisstats.backend.domain.Player;
import com.tennisstats.backend.domain.Rubber;
import com.tennisstats.backend.domain.RubberType;
import com.tennisstats.backend.domain.Side;
import com.tennisstats.backend.domain.Team;
import com.tennisstats.backend.domain.repository.MeetingRepository;
import com.tennisstats.backend.domain.repository.PlayerRepository;
import com.tennisstats.backend.domain.repository.TeamRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Import(PlayerClubService.class)
class PlayerClubServiceTest implements PostgresTestcontainer {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlayerClubService playerClubService;

    private Team weinheim;
    private Team leimen;
    private Team sandhausen;
    private Player anna;
    private Player bea;

    @BeforeEach
    void setUp() {
        weinheim = teamRepository.save(new Team("TC Weinheim 1902 2"));
        leimen = teamRepository.save(new Team("TC BW 64 Leimen 1"));
        sandhausen = teamRepository.save(new Team("TC 70 Sandhausen 1"));
        anna = playerRepository.save(new Player("Anna", "Adler"));
        bea = playerRepository.save(new Player("Bea", "Berger"));
    }

    private void singles(Team home, Team away, LocalDate playedOn, Player homePlayer, Player awayPlayer) {
        Meeting meeting = new Meeting(home, away, playedOn);
        Rubber rubber = meeting.addRubber(RubberType.SINGLES, 1);
        rubber.addParticipant(homePlayer, Side.HOME);
        rubber.addParticipant(awayPlayer, Side.AWAY);
        meetingRepository.saveAndFlush(meeting);
    }

    private Map<Long, String> clubsOf(Player... players) {
        entityManager.clear();
        return playerClubService.getClubs(List.of(players).stream().map(Player::getId).toList());
    }

    @Test
    void takesTheTeamOnThePlayersOwnSide() {
        singles(weinheim, leimen, LocalDate.of(2026, 5, 3), anna, bea);

        assertThat(clubsOf(anna, bea))
                .containsExactlyInAnyOrderEntriesOf(
                        Map.of(anna.getId(), "TC Weinheim 1902", bea.getId(), "TC BW 64 Leimen"));
    }

    @Test
    void usesTheMostRecentMeetingForAPlayerWhoChangedClubs() {
        singles(sandhausen, weinheim, LocalDate.of(2026, 6, 14), bea, anna);
        singles(leimen, sandhausen, LocalDate.of(2025, 5, 3), anna, bea);

        assertThat(clubsOf(anna, bea))
                .containsExactlyInAnyOrderEntriesOf(
                        Map.of(anna.getId(), "TC Weinheim 1902", bea.getId(), "TC 70 Sandhausen"));
    }

    @Test
    void followsThePlayerFromHomeToAwaySide() {
        singles(weinheim, leimen, LocalDate.of(2026, 5, 3), anna, bea);
        singles(sandhausen, weinheim, LocalDate.of(2026, 5, 10), bea, anna);

        assertThat(clubsOf(anna)).containsExactlyEntriesOf(Map.of(anna.getId(), "TC Weinheim 1902"));
    }

    @Test
    void leavesOutAPlayerWithoutMeetings() {
        Player carla = playerRepository.save(new Player("Carla", "Clausen"));
        singles(weinheim, leimen, LocalDate.of(2026, 5, 3), bea, carla);

        assertThat(clubsOf(anna)).isEmpty();
    }

    @Test
    void returnsNothingForNoPlayers() {
        assertThat(playerClubService.getClubs(List.of())).isEmpty();
    }

    @Test
    void resolvesAnyNumberOfPlayersWithOneQuery() {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Player home = playerRepository.save(new Player("Home", "Player " + i));
            Player away = playerRepository.save(new Player("Away", "Player " + i));
            singles(weinheim, leimen, LocalDate.of(2026, 5, i + 1), home, away);
            players.add(home);
            players.add(away);
        }
        entityManager.clear();
        Statistics statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        Map<Long, String> clubs = playerClubService.getClubs(players.stream().map(Player::getId).toList());

        assertThat(clubs).hasSize(12);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }
}
