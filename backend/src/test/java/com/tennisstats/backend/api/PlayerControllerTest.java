package com.tennisstats.backend.api;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class PlayerControllerTest implements PostgresTestcontainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @BeforeEach
    void setUp() {
        playerRepository.save(new Player("Anna", "Schmidt"));
        playerRepository.save(new Player("Anna", "Schuster"));
        playerRepository.save(new Player("Ben", "Meier"));
    }

    @Test
    void returnsPlayersMatchingAPartialQuery() throws Exception {
        mockMvc.perform(get("/api/v1/players").param("query", "Schm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Anna Schmidt"))
                .andExpect(jsonPath("$[0].club").value(nullValue()));
    }

    @Test
    void includesEachPlayersClubFromTheirMostRecentMeeting() throws Exception {
        Player schmidt = playerRepository.findByFirstNameAndLastName("Anna", "Schmidt").orElseThrow();
        Player schuster = playerRepository.findByFirstNameAndLastName("Anna", "Schuster").orElseThrow();
        Meeting meeting = new Meeting(
                teamRepository.save(new Team("TC Weinheim 1902 2")),
                teamRepository.save(new Team("TC BW 64 Leimen 1")),
                LocalDate.of(2026, 5, 3));
        Rubber rubber = meeting.addRubber(RubberType.SINGLES, 1);
        rubber.addParticipant(schmidt, Side.HOME);
        rubber.addParticipant(schuster, Side.AWAY);
        meetingRepository.saveAndFlush(meeting);

        mockMvc.perform(get("/api/v1/players").param("query", "anna"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Anna Schmidt"))
                .andExpect(jsonPath("$[0].club").value("TC Weinheim 1902"))
                .andExpect(jsonPath("$[1].name").value("Anna Schuster"))
                .andExpect(jsonPath("$[1].club").value("TC BW 64 Leimen"));
    }

    @Test
    void matchIsCaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/v1/players").param("query", "schm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Anna Schmidt"));
    }

    @Test
    void matchesAcrossFirstAndLastName() throws Exception {
        mockMvc.perform(get("/api/v1/players").param("query", "anna"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void idIsSerializedAsAString() throws Exception {
        Long id = playerRepository
                .findByFirstNameAndLastName("Anna", "Schmidt")
                .orElseThrow()
                .getId();

        mockMvc.perform(get("/api/v1/players").param("query", "Schmidt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(String.valueOf(id)));
    }

    @Test
    void returnsAnEmptyArrayRatherThanAnErrorWhenNothingMatches() throws Exception {
        mockMvc.perform(get("/api/v1/players").param("query", "Nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void returnsBadRequestWhenQueryParamIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/players"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void returnsBadRequestWhenQueryParamIsBlank() throws Exception {
        mockMvc.perform(get("/api/v1/players").param("query", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
