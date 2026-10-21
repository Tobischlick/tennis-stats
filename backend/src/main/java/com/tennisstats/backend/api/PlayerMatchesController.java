package com.tennisstats.backend.api;

import com.tennisstats.backend.domain.Player;
import com.tennisstats.backend.domain.repository.PlayerRepository;
import com.tennisstats.backend.stats.MatchDetail;
import com.tennisstats.backend.stats.PlayerClubService;
import com.tennisstats.backend.stats.RecentMatchesService;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerMatchesController {

    private static final int MAX_LIMIT = 10;

    private final PlayerRepository playerRepository;
    private final RecentMatchesService recentMatchesService;
    private final PlayerClubService playerClubService;

    public PlayerMatchesController(
            PlayerRepository playerRepository,
            RecentMatchesService recentMatchesService,
            PlayerClubService playerClubService) {
        this.playerRepository = playerRepository;
        this.recentMatchesService = recentMatchesService;
        this.playerClubService = playerClubService;
    }

    // limit is taken as a String so a non-numeric value gets the same ErrorResponse as an out-of-range one.
    @GetMapping("/{id}/matches")
    public ResponseEntity<?> getRecentMatches(@PathVariable String id, @RequestParam(required = false) String limit) {
        Integer parsedLimit = parseLimit(limit);
        if (parsedLimit == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("query parameter 'limit' must be an integer from 1 to " + MAX_LIMIT));
        }
        Long playerId = parsePlayerId(id);
        if (playerId == null || !playerRepository.existsById(playerId)) {
            return ResponseEntity.status(404).body(new ErrorResponse("No player with id '" + id + "'"));
        }
        List<MatchDetail> matches = recentMatchesService.getRecentMatches(playerId, parsedLimit);
        List<Long> playerIds = matches.stream()
                .flatMap(match -> Stream.concat(match.opponents().stream(), Stream.ofNullable(match.partner())))
                .map(Player::getId)
                .distinct()
                .toList();
        Map<Long, String> clubs = playerClubService.getClubs(playerIds);
        return ResponseEntity.ok(
                matches.stream().map(match -> RecentMatch.from(match, clubs)).toList());
    }

    private static Integer parseLimit(String rawLimit) {
        if (rawLimit == null) {
            return MAX_LIMIT;
        }
        try {
            int limit = Integer.parseInt(rawLimit);
            return limit >= 1 && limit <= MAX_LIMIT ? limit : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Long parsePlayerId(String rawId) {
        try {
            return Long.valueOf(rawId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
