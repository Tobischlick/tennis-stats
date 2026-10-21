package com.tennisstats.backend.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PlayerStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MeetingRepository meetingRepository;

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

    private Rubber singlesRubber(LocalDate playedOn, Player homePlayer, Player awayPlayer) {
        Meeting meeting = new Meeting(home, away, playedOn);
        Rubber rubber = meeting.addRubber(RubberType.SINGLES, 1);
        rubber.addParticipant(homePlayer, Side.HOME);
        rubber.addParticipant(awayPlayer, Side.AWAY);
        meetingRepository.saveAndFlush(meeting);
        return rubber;
    }

    private void annaWinsAgainstBea(LocalDate playedOn) {
        Rubber rubber = singlesRubber(playedOn, anna, bea);
        rubber.addSet(1, SetType.REGULAR, 6, 3);
        rubber.addSet(2, SetType.REGULAR, 6, 4);
        meetingRepository.saveAndFlush(rubber.getMeeting());
    }

    private void annaLosesAgainstBea(LocalDate playedOn) {
        Rubber rubber = singlesRubber(playedOn, anna, bea);
        rubber.addSet(1, SetType.REGULAR, 3, 6);
        rubber.addSet(2, SetType.REGULAR, 4, 6);
        meetingRepository.saveAndFlush(rubber.getMeeting());
    }

    @Test
    void returnsWinLossTieBreakAndStreakForAPlayerWithMatches() throws Exception {
        annaWinsAgainstBea(LocalDate.of(2026, 5, 3));

        // Anna loses on a match tie-break from her side (8-10).
        Rubber lost = singlesRubber(LocalDate.of(2026, 5, 10), anna, bea);
        lost.addSet(1, SetType.REGULAR, 6, 4);
        lost.addSet(2, SetType.REGULAR, 3, 6);
        lost.addSet(3, SetType.MATCH_TIE_BREAK, 8, 10);
        meetingRepository.saveAndFlush(lost.getMeeting());

        // Anna wins, taking a 7-6 set on a 7-5 tie-break.
        Rubber won = singlesRubber(LocalDate.of(2026, 5, 17), anna, bea);
        won.addSet(1, SetType.REGULAR, 7, 6).setTiebreakScore(7, 5);
        won.addSet(2, SetType.REGULAR, 6, 4);
        meetingRepository.saveAndFlush(won.getMeeting());

        mockMvc.perform(get("/api/v1/players/{id}/stats", anna.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(String.valueOf(anna.getId())))
                .andExpect(jsonPath("$.player.id").value(String.valueOf(anna.getId())))
                .andExpect(jsonPath("$.player.name").value("Anna Adler"))
                .andExpect(jsonPath("$.player.club").value("TC Blau-Weiss"))
                .andExpect(jsonPath("$.matchesPlayed").value(3))
                .andExpect(jsonPath("$.hasEnoughData").value(true))
                .andExpect(jsonPath("$.record.wins").value(2))
                .andExpect(jsonPath("$.record.losses").value(1))
                .andExpect(jsonPath("$.record.winPercentage").value(66.7))
                .andExpect(jsonPath("$.tieBreak.tieBreaksPlayed").value(2))
                .andExpect(jsonPath("$.tieBreak.tieBreaksWon").value(1))
                .andExpect(jsonPath("$.tieBreak.tieBreakWinPercentage").value(50.0))
                .andExpect(jsonPath("$.streak.current.result").value("WIN"))
                .andExpect(jsonPath("$.streak.current.count").value(1))
                .andExpect(jsonPath("$.streak.recentResults.length()").value(3))
                .andExpect(jsonPath("$.streak.recentResults[0]").value("WIN"))
                .andExpect(jsonPath("$.streak.recentResults[1]").value("LOSS"))
                .andExpect(jsonPath("$.streak.recentResults[2]").value("WIN"));
    }

    @Test
    void returnsTheDecidingSetRecordCountingRegularThirdSetsAndMatchTieBreaks() throws Exception {
        annaWinsAgainstBea(LocalDate.of(2026, 5, 3));

        Rubber wonThirdSet = singlesRubber(LocalDate.of(2026, 5, 10), anna, bea);
        wonThirdSet.addSet(1, SetType.REGULAR, 6, 4);
        wonThirdSet.addSet(2, SetType.REGULAR, 3, 6);
        wonThirdSet.addSet(3, SetType.REGULAR, 6, 3);
        meetingRepository.saveAndFlush(wonThirdSet.getMeeting());

        Rubber lostMatchTieBreak = singlesRubber(LocalDate.of(2026, 5, 17), anna, bea);
        lostMatchTieBreak.addSet(1, SetType.REGULAR, 6, 4);
        lostMatchTieBreak.addSet(2, SetType.REGULAR, 3, 6);
        lostMatchTieBreak.addSet(3, SetType.MATCH_TIE_BREAK, 8, 10);
        meetingRepository.saveAndFlush(lostMatchTieBreak.getMeeting());

        Rubber wonMatchTieBreak = singlesRubber(LocalDate.of(2026, 5, 24), bea, anna);
        wonMatchTieBreak.addSet(1, SetType.REGULAR, 6, 4);
        wonMatchTieBreak.addSet(2, SetType.REGULAR, 3, 6);
        wonMatchTieBreak.addSet(3, SetType.MATCH_TIE_BREAK, 7, 10);
        meetingRepository.saveAndFlush(wonMatchTieBreak.getMeeting());

        mockMvc.perform(get("/api/v1/players/{id}/stats", anna.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchesPlayed").value(4))
                .andExpect(jsonPath("$.tieBreak.tieBreaksPlayed").value(2))
                .andExpect(jsonPath("$.decidingSet.played").value(3))
                .andExpect(jsonPath("$.decidingSet.won").value(2))
                .andExpect(jsonPath("$.decidingSet.winPercentage").value(66.7));
    }

    @Test
    void reportsZeroDecidingSetPercentageForAPlayerWhoNeverWentToOne() throws Exception {
        annaWinsAgainstBea(LocalDate.of(2026, 5, 3));

        mockMvc.perform(get("/api/v1/players/{id}/stats", anna.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decidingSet.played").value(0))
                .andExpect(jsonPath("$.decidingSet.won").value(0))
                .andExpect(jsonPath("$.decidingSet.winPercentage").value(0.0));
    }

    @Test
    void reportsZeroTieBreakPercentageForAPlayerWhoNeverPlayedOne() throws Exception {
        annaWinsAgainstBea(LocalDate.of(2026, 5, 3));

        mockMvc.perform(get("/api/v1/players/{id}/stats", anna.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tieBreak.tieBreaksPlayed").value(0))
                .andExpect(jsonPath("$.tieBreak.tieBreaksWon").value(0))
                .andExpect(jsonPath("$.tieBreak.tieBreakWinPercentage").value(0.0));
    }

    @Test
    void returnsANoDataResponseForAPlayerWithoutMatches() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}/stats", anna.getId()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{\"matchesPlayed\":0,\"hasEnoughData\":false,\"record\":null,\"tieBreak\":null,\"streak\":null}"))
                .andExpect(jsonPath("$.player.id").value(String.valueOf(anna.getId())))
                .andExpect(jsonPath("$.player.name").value("Anna Adler"))
                .andExpect(jsonPath("$.player.club").value(nullValue()))
                .andExpect(jsonPath("$.record").value(nullValue()))
                .andExpect(jsonPath("$.tieBreak").value(nullValue()))
                .andExpect(jsonPath("$.streak").value(nullValue()))
                .andExpect(jsonPath("$.decidingSet").value(nullValue()));
    }

    @Test
    void returnsNotFoundForAnUnknownPlayerId() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}/stats", "999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("999999")));
    }

    @Test
    void returnsNotFoundForANonNumericPlayerId() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}/stats", "zzz999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("zzz999")));
    }

    @Test
    void playerIdIsSerializedAsAString() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}/stats", anna.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(String.valueOf(anna.getId())));
    }

    @Test
    void returnsHeadToHeadFromThePlayersPerspective() throws Exception {
        annaWinsAgainstBea(LocalDate.of(2026, 5, 3));
        annaLosesAgainstBea(LocalDate.of(2026, 5, 17));

        mockMvc.perform(get("/api/v1/players/{id}/head-to-head/{opponentId}", anna.getId(), bea.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(String.valueOf(anna.getId())))
                .andExpect(jsonPath("$.opponentId").value(String.valueOf(bea.getId())))
                .andExpect(jsonPath("$.player.id").value(String.valueOf(anna.getId())))
                .andExpect(jsonPath("$.player.name").value("Anna Adler"))
                .andExpect(jsonPath("$.opponent.id").value(String.valueOf(bea.getId())))
                .andExpect(jsonPath("$.player.club").value("TC Blau-Weiss"))
                .andExpect(jsonPath("$.opponent.name").value("Bea Berger"))
                .andExpect(jsonPath("$.opponent.club").value("TC Rot-Gelb"))
                .andExpect(jsonPath("$.matchesPlayed").value(2))
                .andExpect(jsonPath("$.hasEnoughData").value(true))
                .andExpect(jsonPath("$.record.wins").value(1))
                .andExpect(jsonPath("$.record.losses").value(1))
                .andExpect(jsonPath("$.record.winPercentage").value(50.0))
                .andExpect(jsonPath("$.recentResults.length()").value(2))
                .andExpect(jsonPath("$.recentResults[0]").value("LOSS"))
                .andExpect(jsonPath("$.recentResults[1]").value("WIN"))
                .andExpect(jsonPath("$.doubles.matchesPlayed").value(0))
                .andExpect(jsonPath("$.doubles.hasEnoughData").value(false))
                .andExpect(jsonPath("$.doubles.record").value(nullValue()))
                .andExpect(jsonPath("$.doubles.meetings.length()").value(0));
    }

    @Test
    void returnsDoublesMeetingsSeparatelyFromTheSinglesRecord() throws Exception {
        annaWinsAgainstBea(LocalDate.of(2026, 5, 3));
        Player clara = playerRepository.save(new Player("Clara", "Conrad"));
        Player dana = playerRepository.save(new Player("Dana", "Diehl"));

        // Bea and Dana beat Anna and Clara; Bea plays HOME, so Anna's view is flipped.
        Meeting meeting = new Meeting(away, home, LocalDate.of(2026, 5, 10));
        Rubber rubber = meeting.addRubber(RubberType.DOUBLES, 1);
        rubber.addParticipant(bea, Side.HOME);
        rubber.addParticipant(dana, Side.HOME);
        rubber.addParticipant(anna, Side.AWAY);
        rubber.addParticipant(clara, Side.AWAY);
        rubber.addSet(1, SetType.REGULAR, 6, 4);
        rubber.addSet(2, SetType.REGULAR, 7, 6).setTiebreakScore(7, 3);
        meetingRepository.saveAndFlush(meeting);

        mockMvc.perform(get("/api/v1/players/{id}/head-to-head/{opponentId}", anna.getId(), bea.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchesPlayed").value(1))
                .andExpect(jsonPath("$.record.wins").value(1))
                .andExpect(jsonPath("$.record.losses").value(0))
                .andExpect(jsonPath("$.recentResults.length()").value(1))
                .andExpect(jsonPath("$.doubles.matchesPlayed").value(1))
                .andExpect(jsonPath("$.doubles.hasEnoughData").value(true))
                .andExpect(jsonPath("$.doubles.record.wins").value(0))
                .andExpect(jsonPath("$.doubles.record.losses").value(1))
                .andExpect(jsonPath("$.doubles.record.winPercentage").value(0.0))
                .andExpect(jsonPath("$.doubles.meetings.length()").value(1))
                .andExpect(jsonPath("$.doubles.meetings[0].playedOn").value("2026-05-10"))
                .andExpect(jsonPath("$.doubles.meetings[0].result").value("LOSS"))
                .andExpect(jsonPath("$.doubles.meetings[0].partner.id").value(String.valueOf(clara.getId())))
                .andExpect(jsonPath("$.doubles.meetings[0].partner.name").value("Clara Conrad"))
                .andExpect(jsonPath("$.doubles.meetings[0].partner.club").value("TC Blau-Weiss"))
                .andExpect(jsonPath("$.doubles.meetings[0].opponentPartner.name").value("Dana Diehl"))
                .andExpect(jsonPath("$.doubles.meetings[0].opponentPartner.club").value("TC Rot-Gelb"))
                .andExpect(jsonPath("$.doubles.meetings[0].ownTeam").value("TC Blau-Weiss 1"))
                .andExpect(jsonPath("$.doubles.meetings[0].opponentTeam").value("TC Rot-Gelb 1"))
                .andExpect(jsonPath("$.doubles.meetings[0].sets[0].own").value(4))
                .andExpect(jsonPath("$.doubles.meetings[0].sets[0].opponent").value(6))
                .andExpect(jsonPath("$.doubles.meetings[0].sets[1].ownTieBreak").value(3))
                .andExpect(jsonPath("$.doubles.meetings[0].sets[1].opponentTieBreak").value(7));
    }

    @Test
    void reportsADoublesOnlyHeadToHeadWithoutASinglesRecord() throws Exception {
        Player clara = playerRepository.save(new Player("Clara", "Conrad"));
        Player dana = playerRepository.save(new Player("Dana", "Diehl"));
        Meeting meeting = new Meeting(home, away, LocalDate.of(2026, 5, 10));
        Rubber rubber = meeting.addRubber(RubberType.DOUBLES, 1);
        rubber.addParticipant(anna, Side.HOME);
        rubber.addParticipant(clara, Side.HOME);
        rubber.addParticipant(bea, Side.AWAY);
        rubber.addParticipant(dana, Side.AWAY);
        rubber.addSet(1, SetType.REGULAR, 6, 1);
        rubber.addSet(2, SetType.REGULAR, 6, 2);
        meetingRepository.saveAndFlush(meeting);

        mockMvc.perform(get("/api/v1/players/{id}/head-to-head/{opponentId}", anna.getId(), bea.getId()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{\"matchesPlayed\":0,\"hasEnoughData\":false,\"record\":null,\"recentResults\":[]}"))
                .andExpect(jsonPath("$.doubles.hasEnoughData").value(true))
                .andExpect(jsonPath("$.doubles.record.wins").value(1))
                .andExpect(jsonPath("$.doubles.meetings[0].result").value("WIN"))
                .andExpect(jsonPath("$.doubles.meetings[0].partner.name").value("Clara Conrad"))
                .andExpect(jsonPath("$.doubles.meetings[0].opponentPartner.name").value("Dana Diehl"));
    }

    @Test
    void returnsANoDataHeadToHeadWhenThePlayersHaveNeverMet() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}/head-to-head/{opponentId}", anna.getId(), bea.getId()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{\"matchesPlayed\":0,\"hasEnoughData\":false,\"record\":null,\"recentResults\":[]}"))
                .andExpect(jsonPath("$.record").value(nullValue()))
                .andExpect(jsonPath("$.player.name").value("Anna Adler"))
                .andExpect(jsonPath("$.opponent.id").value(String.valueOf(bea.getId())))
                .andExpect(jsonPath("$.opponent.name").value("Bea Berger"))
                .andExpect(jsonPath("$.opponent.club").value(nullValue()));
    }

    @Test
    void returnsNotFoundWhenTheOpponentDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}/head-to-head/{opponentId}", anna.getId(), "999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("999999")));
    }

    @Test
    void returnsNotFoundWhenThePlayerDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}/head-to-head/{opponentId}", "999999", bea.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("999999")));
    }

    @Test
    void returnsNotFoundForANonNumericOpponentId() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}/head-to-head/{opponentId}", anna.getId(), "zzz999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("zzz999")));
    }

    @Test
    void comparingAPlayerWithThemselvesReturnsTheNoDataResponse() throws Exception {
        annaWinsAgainstBea(LocalDate.of(2026, 5, 3));

        mockMvc.perform(get("/api/v1/players/{id}/head-to-head/{opponentId}", anna.getId(), anna.getId()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{\"matchesPlayed\":0,\"hasEnoughData\":false,\"record\":null,\"recentResults\":[]}"));
    }
}
