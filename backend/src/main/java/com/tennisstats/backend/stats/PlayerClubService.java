package com.tennisstats.backend.stats;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Derives a player's club from the team they played for in their most recent meeting, at read time
 * so a club change shows up as soon as the next meeting is stored and no backfill is needed.
 */
@Service
public class PlayerClubService {

    // Team numbers are small; a longer trailing number belongs to the club name (e.g. a founding year).
    private static final Pattern TEAM_NUMBER = Pattern.compile("\\s+\\d{1,2}$");

    private final PlayerClubRepository playerClubRepository;

    public PlayerClubService(PlayerClubRepository playerClubRepository) {
        this.playerClubRepository = playerClubRepository;
    }

    /** Clubs by player id, resolved in one query; players without a stored meeting are absent. */
    @Transactional(readOnly = true)
    public Map<Long, String> getClubs(Collection<Long> playerIds) {
        Map<Long, String> clubs = new HashMap<>();
        if (playerIds.isEmpty()) {
            return clubs;
        }
        for (PlayerTeamRow row : playerClubRepository.findTeamsOnMostRecentMeetingDate(playerIds)) {
            clubs.putIfAbsent(row.playerId(), clubName(row.teamName()));
        }
        return clubs;
    }

    static String clubName(String teamName) {
        return TEAM_NUMBER.matcher(teamName.strip()).replaceFirst("");
    }
}
