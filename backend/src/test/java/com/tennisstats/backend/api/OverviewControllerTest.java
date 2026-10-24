package com.tennisstats.backend.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OverviewControllerTest implements PostgresTestcontainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Test
    void returnsZerosAndNullsOnAnEmptyDatabase() throws Exception {
        mockMvc.perform(get("/api/v1/overview"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "hasData": false,
                          "totals": {
                            "players": 0, "clubs": 0, "teams": 0, "meetings": 0,
                            "matches": {"total": 0, "singles": 0, "doubles": 0},
                            "sets": 0, "firstPlayedOn": null, "lastPlayedOn": null
                          },
                          "funStats": {
                            "tieBreaks": 0, "matchTieBreaks": 0, "completedMatches": 0, "decidingSetMatches": 0,
                            "decidingSetPercentage": null, "mostCommonScoreLine": null, "longestWinStreak": 0
                          }
                        }
                        """, true));
    }

    @Test
    void returnsTotalsAndFunStats() throws Exception {
        Team home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        Team away = teamRepository.save(new Team("TC Rot-Gelb 1"));
        Player anna = playerRepository.save(new Player("Anna", "Adler"));
        Player bea = playerRepository.save(new Player("Bea", "Berger"));
        Player carla = playerRepository.save(new Player("Carla", "Clausen"));
        Player dora = playerRepository.save(new Player("Dora", "Dietz"));

        Meeting meeting = new Meeting(home, away, LocalDate.of(2026, 5, 3));
        Rubber singles = meeting.addRubber(RubberType.SINGLES, 1);
        singles.addParticipant(anna, Side.HOME);
        singles.addParticipant(bea, Side.AWAY);
        singles.addSet(1, SetType.REGULAR, 6, 7);
        singles.addSet(2, SetType.REGULAR, 6, 3);
        singles.addSet(3, SetType.MATCH_TIE_BREAK, 10, 8);
        Rubber doubles = meeting.addRubber(RubberType.DOUBLES, 2);
        doubles.addParticipant(anna, Side.HOME);
        doubles.addParticipant(carla, Side.HOME);
        doubles.addParticipant(bea, Side.AWAY);
        doubles.addParticipant(dora, Side.AWAY);
        doubles.addSet(1, SetType.REGULAR, 6, 2);
        doubles.addSet(2, SetType.REGULAR, 6, 1);
        meetingRepository.saveAndFlush(meeting);

        mockMvc.perform(get("/api/v1/overview"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "hasData": true,
                          "totals": {
                            "players": 4, "clubs": 2, "teams": 2, "meetings": 1,
                            "matches": {"total": 2, "singles": 1, "doubles": 1},
                            "sets": 5, "firstPlayedOn": "2026-05-03", "lastPlayedOn": "2026-05-03"
                          },
                          "funStats": {
                            "tieBreaks": 1, "matchTieBreaks": 1, "completedMatches": 2, "decidingSetMatches": 1,
                            "decidingSetPercentage": 50.0,
                            "mostCommonScoreLine": {"scoreLine": "6:2 6:1", "count": 1},
                            "longestWinStreak": 2
                          }
                        }
                        """, true));
    }
}
