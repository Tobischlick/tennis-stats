package com.tennisstats.backend.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
class PlayerSeasonsControllerTest implements PostgresTestcontainer {

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

    private void annaPlaysBea(LocalDate playedOn, int firstSetHome, int firstSetAway, int secondSetHome, int secondSetAway) {
        Meeting meeting = new Meeting(home, away, playedOn);
        Rubber rubber = meeting.addRubber(RubberType.SINGLES, 1);
        rubber.addParticipant(anna, Side.HOME);
        rubber.addParticipant(bea, Side.AWAY);
        rubber.addSet(1, SetType.REGULAR, firstSetHome, firstSetAway);
        rubber.addSet(2, SetType.REGULAR, secondSetHome, secondSetAway);
        meetingRepository.saveAndFlush(meeting);
    }

    @Test
    void returnsTheRecordPerSeasonOldestFirst() throws Exception {
        annaPlaysBea(LocalDate.of(2026, 5, 3), 7, 6, 6, 4); // Sommer 2026: win, tie-break won
        annaPlaysBea(LocalDate.of(2025, 11, 15), 6, 3, 6, 4); // Winter 2025/26: win
        annaPlaysBea(LocalDate.of(2026, 2, 7), 3, 6, 6, 7); // Winter 2025/26: loss, tie-break lost
        annaPlaysBea(LocalDate.of(2026, 3, 1), 4, 6, 2, 6); // Winter 2025/26: loss

        mockMvc.perform(get("/api/v1/players/{id}/seasons", anna.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {
                            "season": "Winter 2025/26",
                            "type": "WINTER",
                            "startsOn": "2025-10-01",
                            "endsOn": "2026-03-31",
                            "matchesPlayed": 3,
                            "record": {"wins": 1, "losses": 2, "winPercentage": 33.3},
                            "tieBreak": {"tieBreaksPlayed": 1, "tieBreaksWon": 0, "tieBreakWinPercentage": 0.0}
                          },
                          {
                            "season": "Sommer 2026",
                            "type": "SUMMER",
                            "startsOn": "2026-04-01",
                            "endsOn": "2026-09-30",
                            "matchesPlayed": 1,
                            "record": {"wins": 1, "losses": 0, "winPercentage": 100.0},
                            "tieBreak": {"tieBreaksPlayed": 1, "tieBreaksWon": 1, "tieBreakWinPercentage": 100.0}
                          }
                        ]
                        """, true));
    }

    @Test
    void returnsAnEmptyListForAPlayerWithoutMatches() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}/seasons", anna.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]", true));
    }

    @Test
    void returns404ForAnUnknownPlayer() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}/seasons", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No player with id '" + Long.MAX_VALUE + "'"));
    }

    @Test
    void returns404ForANonNumericId() throws Exception {
        mockMvc.perform(get("/api/v1/players/{id}/seasons", "abc"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No player with id 'abc'"));
    }
}
