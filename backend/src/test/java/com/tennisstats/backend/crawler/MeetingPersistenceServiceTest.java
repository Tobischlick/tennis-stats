package com.tennisstats.backend.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.tennisstats.backend.domain.Meeting;
import com.tennisstats.backend.domain.RubberSet;
import com.tennisstats.backend.domain.RubberType;
import com.tennisstats.backend.domain.SetType;
import com.tennisstats.backend.domain.Side;
import com.tennisstats.backend.domain.repository.MeetingRepository;
import com.tennisstats.backend.domain.repository.PlayerRepository;
import com.tennisstats.backend.domain.repository.RubberParticipantRepository;
import com.tennisstats.backend.domain.repository.RubberRepository;
import com.tennisstats.backend.domain.repository.RubberSetRepository;
import com.tennisstats.backend.domain.repository.TeamRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

@DataJpaTest
@Import(MeetingPersistenceService.class)
@DirtiesContext
class MeetingPersistenceServiceTest {

    @Autowired
    private MeetingPersistenceService persistenceService;

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

    private static ScheduledMeetingDto scheduledMeeting() {
        return new ScheduledMeetingDto("TC Blau-Weiss 1", "TC Rot-Gelb 1", LocalDateTime.of(2026, 5, 10, 10, 0), 12345L);
    }

    private static MatchReport matchReport() {
        Rubber singleOne = new Rubber(
                1,
                List.of("Pohlmann, Ole"),
                List.of("Oberle, Leon"),
                List.of(new Score(6, 4), new Score(6, 3)),
                new Score(1, 0),
                new Score(2, 0),
                new Score(12, 7),
                null);
        Rubber singleTwo = new Rubber(
                2,
                List.of("Fahrbach, Ansgar"),
                List.of("Hasselbach, Leon"),
                List.of(new Score(1, 6), new Score(7, 5), new Score(10, 8)),
                new Score(1, 0),
                new Score(2, 1),
                new Score(18, 19),
                null);
        Rubber doubleOne = new Rubber(
                1,
                List.of("Pohlmann, Ole", "Zirngibl, Jakob"),
                List.of("Oberle, Leon", "Jurisic, Bastian"),
                List.of(new Score(6, 1), new Score(6, 2)),
                new Score(1, 0),
                new Score(2, 0),
                new Score(12, 3),
                null);

        return new MatchReport(
                "TC Blau-Weiss 1", "TC Rot-Gelb 1", new Score(3, 0), List.of(singleOne, singleTwo), List.of(doubleOne));
    }

    @Test
    void persistsTheFullEntityGraphFromTheParsedDtos() {
        Meeting meeting = persistenceService.persist(scheduledMeeting(), matchReport());

        assertThat(meeting.getExternalId()).isEqualTo("12345");
        assertThat(meeting.getHomeTeam().getName()).isEqualTo("TC Blau-Weiss 1");
        assertThat(meeting.getAwayTeam().getName()).isEqualTo("TC Rot-Gelb 1");
        assertThat(meeting.getPlayedOn()).isEqualTo(LocalDate.of(2026, 5, 10));

        assertThat(teamRepository.count()).isEqualTo(2);
        // Pohlmann, Ole plays both a single and the double - must resolve to one player, not two.
        assertThat(playerRepository.count()).isEqualTo(6);
        assertThat(meetingRepository.count()).isEqualTo(1);
        assertThat(rubberRepository.count()).isEqualTo(3);
        assertThat(rubberSetRepository.count()).isEqualTo(7);
        assertThat(rubberParticipantRepository.count()).isEqualTo(8);

        var rubbers = rubberRepository.findAll();
        var doubles = rubbers.stream().filter(r -> r.getType() == RubberType.DOUBLES).findFirst().orElseThrow();
        assertThat(doubles.getPosition()).isEqualTo(3);
        assertThat(doubles.getParticipants()).hasSize(4);

        var tieBreakRubber = rubbers.stream().filter(r -> r.getPosition() == 2).findFirst().orElseThrow();
        var tieBreakSet = tieBreakRubber.getSets().stream()
                .filter(s -> s.getSetNumber() == 3)
                .findFirst()
                .orElseThrow();
        assertThat(tieBreakSet.getType()).isEqualTo(SetType.MATCH_TIE_BREAK);
        assertThat(tieBreakSet.getHomeScore()).isEqualTo(10);
        assertThat(tieBreakSet.getAwayScore()).isEqualTo(8);
    }

    @Test
    void reRunningPersistenceForTheSameMeetingDoesNotDuplicateRows() {
        Meeting first = persistenceService.persist(scheduledMeeting(), matchReport());
        Meeting second = persistenceService.persist(scheduledMeeting(), matchReport());

        assertThat(second.getId()).isEqualTo(first.getId());

        assertThat(teamRepository.count()).isEqualTo(2);
        assertThat(playerRepository.count()).isEqualTo(6);
        assertThat(meetingRepository.count()).isEqualTo(1);
        assertThat(rubberRepository.count()).isEqualTo(3);
        assertThat(rubberSetRepository.count()).isEqualTo(7);
        assertThat(rubberParticipantRepository.count()).isEqualTo(8);
    }

    @Test
    void newPlayersAndTeamsAreCreatedOnFirstSightingAndReusedAfterThat() {
        persistenceService.persist(scheduledMeeting(), matchReport());

        // a second meeting, re-using one team and one player from the first, introducing new ones too
        ScheduledMeetingDto secondScheduled =
                new ScheduledMeetingDto("TC Blau-Weiss 1", "TC Gruen-Weiss 1", LocalDateTime.of(2026, 5, 17, 10, 0), 67890L);
        Rubber single = new Rubber(
                1,
                List.of("Pohlmann, Ole"),
                List.of("Neumann, Otto"),
                List.of(new Score(6, 2), new Score(6, 1)),
                new Score(1, 0),
                new Score(2, 0),
                new Score(12, 3),
                null);
        MatchReport secondReport =
                new MatchReport("TC Blau-Weiss 1", "TC Gruen-Weiss 1", new Score(1, 0), List.of(single), List.of());

        persistenceService.persist(secondScheduled, secondReport);

        // 3 teams total: TC Blau-Weiss 1 (reused), TC Rot-Gelb 1, TC Gruen-Weiss 1
        assertThat(teamRepository.count()).isEqualTo(3);
        // 7 players total: the 6 from the first meeting (Pohlmann, Ole reused) + new Neumann, Otto
        assertThat(playerRepository.count()).isEqualTo(7);
        assertThat(meetingRepository.count()).isEqualTo(2);
    }

    @Test
    void storesTheRetiredSideAndFlagsTheSetsCutShort() throws IOException {
        String html = Files.readString(Path.of("src/test/resources/fixtures/meeting-report-12710515.html"));
        MatchReport report = MatchReportParser.parse(html);
        ScheduledMeetingDto scheduled = new ScheduledMeetingDto(
                report.homeTeam(), report.awayTeam(), LocalDateTime.of(2026, 5, 10, 10, 0), 12710515L);

        persistenceService.persist(scheduled, report);

        Map<Integer, com.tennisstats.backend.domain.Rubber> byPosition = rubberRepository.findAll().stream()
                .collect(Collectors.toMap(com.tennisstats.backend.domain.Rubber::getPosition, r -> r));

        var homeRetired = byPosition.get(2);
        assertThat(homeRetired.getRetiredSide()).isEqualTo(Side.HOME);
        assertThat(homeRetired.getSets())
                .extracting(RubberSet::getHomeScore, RubberSet::getAwayScore, RubberSet::isUnfinished)
                .containsExactly(tuple(0, 3, true));

        var awayRetired = byPosition.get(5);
        assertThat(awayRetired.getRetiredSide()).isEqualTo(Side.AWAY);
        assertThat(awayRetired.getSets())
                .extracting(RubberSet::getHomeScore, RubberSet::getAwayScore, RubberSet::isUnfinished)
                .containsExactlyInAnyOrder(tuple(6, 0, false), tuple(1, 0, true));

        var walkover = byPosition.get(7);
        assertThat(walkover.getRetiredSide()).isEqualTo(Side.HOME);
        assertThat(walkover.getSets()).isEmpty();

        var playedOut = byPosition.get(3);
        assertThat(playedOut.getRetiredSide()).isNull();
        assertThat(playedOut.getSets()).noneMatch(RubberSet::isUnfinished);
    }

    // real reports whose absent-player doubles made the second participant violate
    // uk_rubber_participant_rubber_side_player
    @ParameterizedTest
    @CsvSource({"12710538, '1,2,3,4,7,8'", "12710515, '1,2,3,4,5,7,8'", "12895933, '1,2,3,4,5,7,8'"})
    void skipsRubbersWithAnAbsentPlayerPlaceholder(long meetingId, String storedPositions) throws IOException {
        String html = Files.readString(Path.of("src/test/resources/fixtures/meeting-report-" + meetingId + ".html"));
        MatchReport report = MatchReportParser.parse(html);
        ScheduledMeetingDto scheduled = new ScheduledMeetingDto(
                report.homeTeam(), report.awayTeam(), LocalDateTime.of(2026, 5, 10, 10, 0), meetingId);

        persistenceService.persist(scheduled, report);

        assertThat(rubberRepository.findAll())
                .extracting(r -> String.valueOf(r.getPosition()))
                .containsExactlyInAnyOrder(storedPositions.split(","));
        assertThat(playerRepository.findAll())
                .noneMatch(p -> p.getLastName().contains("nicht anwesend") || p.getFirstName().contains("nicht anwesend"));
    }
}
