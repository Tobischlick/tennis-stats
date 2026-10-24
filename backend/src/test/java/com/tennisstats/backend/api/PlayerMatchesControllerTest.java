package com.tennisstats.backend.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PlayerMatchesControllerTest implements PostgresTestcontainer {

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

    private void annaWinsAgainstBea(LocalDate playedOn) {
        Meeting meeting = new Meeting(home, away, playedOn);
        Rubber rubber = meeting.addRubber(RubberType.SINGLES, 1);
        rubber.addParticipant(anna, Side.HOME);
        rubber.addParticipant(bea, Side.AWAY);
        rubber.addSet(1, SetType.REGULAR, 6, 3);
        rubber.addSet(2, SetType.REGULAR, 6, 4);
        meetingRepository.saveAndFlush(meeting);
    }

    @Test
    void returnsSinglesAndDoublesFromThePlayersPerspectiveNewestFirst() throws Exception {
        Meeting singlesMeeting = new Meeting(home, away, LocalDate.of(2026, 5, 3));
        Rubber singles = singlesMeeting.addRubber(RubberType.SINGLES, 1);
        singles.addParticipant(bea, Side.HOME);
        singles.addParticipant(anna, Side.AWAY);
        singles.addSet(1, SetType.REGULAR, 6, 7).setTiebreakScore(5, 7);
        singles.addSet(2, SetType.REGULAR, 6, 3);
        singles.addSet(3, SetType.MATCH_TIE_BREAK, 10, 8);
        meetingRepository.saveAndFlush(singlesMeeting);

        Meeting doublesMeeting = new Meeting(home, away, LocalDate.of(2026, 5, 10));
        Rubber doubles = doublesMeeting.addRubber(RubberType.DOUBLES, 7);
        doubles.addParticipant(anna, Side.HOME);
        doubles.addParticipant(carla, Side.HOME);
        doubles.addParticipant(bea, Side.AWAY);
        doubles.addParticipant(dora, Side.AWAY);
        doubles.addSet(1, SetType.REGULAR, 6, 2);
        doubles.addSet(2, SetType.REGULAR, 6, 1);
        meetingRepository.saveAndFlush(doublesMeeting);

        mockMvc.perform(get("/api/v1/players/{id}/matches", anna.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].playedOn").value("2026-05-10"))
                .andExpect(jsonPath("$[0].type").value("DOUBLES"))
                .andExpect(jsonPath("$[0].result").value("WIN"))
                .andExpect(jsonPath("$[0].partner.id").value(String.valueOf(carla.getId())))
                .andExpect(jsonPath("$[0].partner.name").value("Carla Clausen"))
                .andExpect(jsonPath("$[0].partner.club").value("TC Blau-Weiss"))
                .andExpect(jsonPath("$[0].opponents.length()").value(2))
                .andExpect(jsonPath("$[0].opponents[0].id").value(String.valueOf(bea.getId())))
                .andExpect(jsonPath("$[0].opponents[0].club").value("TC Rot-Gelb"))
                .andExpect(jsonPath("$[0].opponents[1].id").value(String.valueOf(dora.getId())))
                .andExpect(jsonPath("$[0].opponents[1].club").value("TC Rot-Gelb"))
                .andExpect(jsonPath("$[0].ownTeam").value("TC Blau-Weiss 1"))
                .andExpect(jsonPath("$[0].opponentTeam").value("TC Rot-Gelb 1"))
                .andExpect(jsonPath("$[1].playedOn").value("2026-05-03"))
                .andExpect(jsonPath("$[1].type").value("SINGLES"))
                .andExpect(jsonPath("$[1].result").value("LOSS"))
                .andExpect(jsonPath("$[1].partner").value(nullValue()))
                .andExpect(jsonPath("$[1].opponents[0].name").value("Bea Berger"))
                .andExpect(jsonPath("$[1].opponents[0].club").value("TC Rot-Gelb"))
                .andExpect(jsonPath("$[1].ownTeam").value("TC Rot-Gelb 1"))
                .andExpect(jsonPath("$[1].opponentTeam").value("TC Blau-Weiss 1"))
                .andExpect(content().json("""
                        [{}, {"sets": [
                          {"own": 7, "opponent": 6, "ownTieBreak": 7, "opponentTieBreak": 5, "matchTieBreak": false},
                          {"own": 3, "opponent": 6, "ownTieBreak": null, "opponentTieBreak": null, "matchTieBreak": false},
                          {"own": 8, "opponent": 10, "ownTieBreak": null, "opponentTieBreak": null, "matchTieBreak": true}
                        ]}]
                        """));
    }

    @Test
    void returnsAtMostTenMatchesByDefault() throws Exception {
        for (int day = 1; day <= 12; day++) {
            annaWinsAgainstBea(LocalDate.of(2026, 5, day));
        }

        mockMvc.perform(get("/api/v1/players/{id}/matches", anna.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "12"))
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].playedOn").value("2026-05-12"));
    }

    @Test
    void pagesThroughAllMatchesWithLimitAndOffset() throws Exception {
        for (int day = 1; day <= 12; day++) {
            annaWinsAgainstBea(LocalDate.of(2026, 5, day));
        }

        mockMvc.perform(get("/api/v1/players/{id}/matches", anna.getId())
                        .param("limit", "100")
                        .param("offset", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "12"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].playedOn").value("2026-05-02"))
                .andExpect(jsonPath("$[1].playedOn").value("2026-05-01"));
    }

    @Test
    void returnsAnEmptyListForAnOffsetPastTheEnd() throws Exception {
        annaWinsAgainstBea(LocalDate.of(2026, 5, 1));

        mockMvc.perform(get("/api/v1/players/{id}/matches", anna.getId()).param("offset", "5"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(content().json("[]"));
    }

    @Test
    void honoursTheLimit() throws Exception {
        for (int day = 1; day <= 3; day++) {
            annaWinsAgainstBea(LocalDate.of(2026, 5, day));
        }

        mockMvc.perform(get("/api/v1/players/{id}/matches", anna.getId()).param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].playedOn").value("2026-05-02"));
    }

    @Test
    void rejectsALimitOutsideOneToHundred() throws Exception {
        for (String limit : new String[] {"0", "101", "-1", "ten", ""}) {
            mockMvc.perform(get("/api/v1/players/{id}/matches", anna.getId()).param("limit", limit))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("limit")));
        }
    }

    @Test
    void rejectsANegativeOrNonNumericOffset() throws Exception {
        for (String offset : new String[] {"-1", "two", ""}) {
            mockMvc.perform(get("/api/v1/players/{id}/matches", anna.getId()).param("offset", offset))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("offset")));
        }
    }

    @Test
    void returnsAnEmptyListForAPlayerWithoutMatches() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}/matches", anna.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "0"))
                .andExpect(content().json("[]"));
    }

    @Test
    void returnsNotFoundForAnUnknownPlayerId() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}/matches", "999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("999999")));
    }

    @Test
    void returnsNotFoundForANonNumericPlayerId() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}/matches", "zzz999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("zzz999")));
    }
}
