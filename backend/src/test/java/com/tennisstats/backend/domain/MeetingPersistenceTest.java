package com.tennisstats.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tennisstats.backend.PostgresTestcontainer;
import com.tennisstats.backend.domain.repository.MeetingRepository;
import com.tennisstats.backend.domain.repository.PlayerRepository;
import com.tennisstats.backend.domain.repository.RubberParticipantRepository;
import com.tennisstats.backend.domain.repository.RubberRepository;
import com.tennisstats.backend.domain.repository.RubberSetRepository;
import com.tennisstats.backend.domain.repository.TeamRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class MeetingPersistenceTest implements PostgresTestcontainer {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private RubberRepository rubberRepository;

    @Autowired
    private RubberSetRepository rubberSetRepository;

    @Autowired
    private RubberParticipantRepository rubberParticipantRepository;

    @Test
    void savesAndLoadsAFullMeetingGraphIncludingADoublesPairing() {
        Team home = teamRepository.save(new Team("TC Blau-Weiss 1", "nuliga-team-home"));
        Team away = teamRepository.save(new Team("TC Rot-Gelb 1", "nuliga-team-away"));

        Player homeSingle = playerRepository.save(new Player("Anna", "Adler"));
        Player awaySingle = playerRepository.save(new Player("Bea", "Berger"));
        Player homeDoublesA = playerRepository.save(new Player("Clara", "Conrad"));
        Player homeDoublesB = playerRepository.save(new Player("Dana", "Diehl"));
        Player awayDoublesA = playerRepository.save(new Player("Eva", "Engel"));
        Player awayDoublesB = playerRepository.save(new Player("Frida", "Fischer"));

        Meeting meeting = new Meeting(home, away, LocalDate.of(2026, 5, 10));

        Rubber singles = meeting.addRubber(RubberType.SINGLES, 1);
        singles.addParticipant(homeSingle, Side.HOME);
        singles.addParticipant(awaySingle, Side.AWAY);
        singles.addSet(1, SetType.REGULAR, 6, 4);
        singles.addSet(2, SetType.REGULAR, 6, 7).setTiebreakScore(4, 7);
        // decided by a 10-point match tie-break instead of a normal 3rd set
        singles.addSet(3, SetType.MATCH_TIE_BREAK, 10, 8);

        Rubber doubles = meeting.addRubber(RubberType.DOUBLES, 2);
        doubles.addParticipant(homeDoublesA, Side.HOME);
        doubles.addParticipant(homeDoublesB, Side.HOME);
        doubles.addParticipant(awayDoublesA, Side.AWAY);
        doubles.addParticipant(awayDoublesB, Side.AWAY);
        doubles.addSet(1, SetType.REGULAR, 6, 3);
        doubles.addSet(2, SetType.REGULAR, 6, 2);

        Meeting saved = meetingRepository.saveAndFlush(meeting);
        Long meetingId = saved.getId();

        // reload everything from the database rather than relying on the in-memory graph
        Meeting reloaded = meetingRepository.findById(meetingId).orElseThrow();
        assertThat(reloaded.getHomeTeam().getName()).isEqualTo("TC Blau-Weiss 1");
        assertThat(reloaded.getAwayTeam().getName()).isEqualTo("TC Rot-Gelb 1");
        assertThat(reloaded.getPlayedOn()).isEqualTo(LocalDate.of(2026, 5, 10));

        List<Rubber> rubbers = rubberRepository.findAll();
        assertThat(rubbers).hasSize(2);

        Rubber reloadedSingles = rubbers.stream().filter(r -> r.getPosition() == 1).findFirst().orElseThrow();
        assertThat(reloadedSingles.getType()).isEqualTo(RubberType.SINGLES);
        assertThat(reloadedSingles.getMeeting().getId()).isEqualTo(meetingId);

        List<RubberSet> singlesSets = rubberSetRepository.findAll().stream()
                .filter(s -> s.getRubber().getId().equals(reloadedSingles.getId()))
                .toList();
        assertThat(singlesSets).hasSize(3);
        RubberSet matchTieBreak = singlesSets.stream()
                .filter(s -> s.getSetNumber() == 3)
                .findFirst()
                .orElseThrow();
        assertThat(matchTieBreak.getType()).isEqualTo(SetType.MATCH_TIE_BREAK);
        assertThat(matchTieBreak.getHomeScore()).isEqualTo(10);
        assertThat(matchTieBreak.getAwayScore()).isEqualTo(8);
        RubberSet tiebreakSet = singlesSets.stream()
                .filter(s -> s.getSetNumber() == 2)
                .findFirst()
                .orElseThrow();
        assertThat(tiebreakSet.getHomeTiebreakScore()).isEqualTo(4);
        assertThat(tiebreakSet.getAwayTiebreakScore()).isEqualTo(7);

        List<RubberParticipant> singlesParticipants = rubberParticipantRepository.findAll().stream()
                .filter(p -> p.getRubber().getId().equals(reloadedSingles.getId()))
                .toList();
        assertThat(singlesParticipants).hasSize(2);

        Rubber reloadedDoubles = rubbers.stream().filter(r -> r.getPosition() == 2).findFirst().orElseThrow();
        assertThat(reloadedDoubles.getType()).isEqualTo(RubberType.DOUBLES);
        List<RubberParticipant> doublesParticipants = rubberParticipantRepository.findAll().stream()
                .filter(p -> p.getRubber().getId().equals(reloadedDoubles.getId()))
                .toList();
        assertThat(doublesParticipants).hasSize(4);
        assertThat(doublesParticipants.stream()
                        .filter(p -> p.getSide() == Side.HOME)
                        .map(p -> p.getPlayer().getLastName()))
                .containsExactlyInAnyOrder("Conrad", "Diehl");
        assertThat(doublesParticipants.stream()
                        .filter(p -> p.getSide() == Side.AWAY)
                        .map(p -> p.getPlayer().getLastName()))
                .containsExactlyInAnyOrder("Engel", "Fischer");
    }
}
