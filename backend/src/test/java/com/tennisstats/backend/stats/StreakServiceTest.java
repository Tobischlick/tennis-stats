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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(StreakService.class)
class StreakServiceTest implements PostgresTestcontainer {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private StreakService streakService;

    private Rubber singlesRubber(Meeting meeting, Player home, Player away, boolean homeWins) {
        Rubber rubber = meeting.addRubber(RubberType.SINGLES, 1);
        rubber.addParticipant(home, Side.HOME);
        rubber.addParticipant(away, Side.AWAY);
        if (homeWins) {
            rubber.addSet(1, SetType.REGULAR, 6, 3);
            rubber.addSet(2, SetType.REGULAR, 6, 4);
        } else {
            rubber.addSet(1, SetType.REGULAR, 3, 6);
            rubber.addSet(2, SetType.REGULAR, 4, 6);
        }
        return rubber;
    }

    @Test
    void reportsCurrentWinStreak() {
        Team home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        Team away = teamRepository.save(new Team("TC Rot-Gelb 1"));
        Player anna = playerRepository.save(new Player("Anna", "Adler"));
        Player opponent = playerRepository.save(new Player("Bea", "Berger"));

        // Anna wins her last 3 rubbers, playing HOME each time, on increasing dates.
        Meeting first = new Meeting(home, away, LocalDate.of(2026, 5, 3));
        singlesRubber(first, anna, opponent, true);
        meetingRepository.save(first);

        Meeting second = new Meeting(home, away, LocalDate.of(2026, 5, 10));
        singlesRubber(second, anna, opponent, true);
        meetingRepository.save(second);

        Meeting third = new Meeting(home, away, LocalDate.of(2026, 5, 17));
        singlesRubber(third, anna, opponent, true);
        meetingRepository.saveAndFlush(third);

        StreakStats stats = streakService.getStreakStats(anna.getId());

        assertThat(stats.hasData()).isTrue();
        assertThat(stats.streakResult()).isEqualTo(MatchResult.WIN);
        assertThat(stats.streakLength()).isEqualTo(3);
        assertThat(stats.recentResults()).containsExactly(MatchResult.WIN, MatchResult.WIN, MatchResult.WIN);
    }

    @Test
    void reportsCurrentLossStreak() {
        Team home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        Team away = teamRepository.save(new Team("TC Rot-Gelb 1"));
        Player anna = playerRepository.save(new Player("Anna", "Adler"));
        Player opponent = playerRepository.save(new Player("Bea", "Berger"));

        // Anna loses her last 2 rubbers, playing HOME each time, on increasing dates.
        Meeting first = new Meeting(home, away, LocalDate.of(2026, 5, 3));
        singlesRubber(first, anna, opponent, false);
        meetingRepository.save(first);

        Meeting second = new Meeting(home, away, LocalDate.of(2026, 5, 10));
        singlesRubber(second, anna, opponent, false);
        meetingRepository.saveAndFlush(second);

        StreakStats stats = streakService.getStreakStats(anna.getId());

        assertThat(stats.hasData()).isTrue();
        assertThat(stats.streakResult()).isEqualTo(MatchResult.LOSS);
        assertThat(stats.streakLength()).isEqualTo(2);
        assertThat(stats.recentResults()).containsExactly(MatchResult.LOSS, MatchResult.LOSS);
    }

    @Test
    void currentStreakStopsAtTheMostRecentBreak() {
        Team home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        Team away = teamRepository.save(new Team("TC Rot-Gelb 1"));
        Player anna = playerRepository.save(new Player("Anna", "Adler"));
        Player opponent = playerRepository.save(new Player("Bea", "Berger"));

        // Chronological order: Win, Loss, Win, Win -> current streak is "won 2", not "won 4".
        Meeting m1 = new Meeting(home, away, LocalDate.of(2026, 5, 3));
        singlesRubber(m1, anna, opponent, true);
        meetingRepository.save(m1);

        Meeting m2 = new Meeting(home, away, LocalDate.of(2026, 5, 10));
        singlesRubber(m2, anna, opponent, false);
        meetingRepository.save(m2);

        Meeting m3 = new Meeting(home, away, LocalDate.of(2026, 5, 17));
        singlesRubber(m3, anna, opponent, true);
        meetingRepository.save(m3);

        Meeting m4 = new Meeting(home, away, LocalDate.of(2026, 5, 24));
        singlesRubber(m4, anna, opponent, true);
        meetingRepository.saveAndFlush(m4);

        StreakStats stats = streakService.getStreakStats(anna.getId());

        assertThat(stats.hasData()).isTrue();
        assertThat(stats.streakResult()).isEqualTo(MatchResult.WIN);
        assertThat(stats.streakLength()).isEqualTo(2);
        assertThat(stats.recentResults())
                .containsExactly(MatchResult.WIN, MatchResult.WIN, MatchResult.LOSS, MatchResult.WIN);
    }

    @Test
    void returnsNoDataForAPlayerWithNoMatches() {
        Player noMatches = playerRepository.saveAndFlush(new Player("Frida", "Fischer"));

        StreakStats stats = streakService.getStreakStats(noMatches.getId());

        assertThat(stats.hasData()).isFalse();
        assertThat(stats.streakResult()).isNull();
        assertThat(stats.streakLength()).isZero();
        assertThat(stats.recentResults()).isEmpty();
    }
}
