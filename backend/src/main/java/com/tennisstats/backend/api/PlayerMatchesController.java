package com.tennisstats.backend.api;

import com.tennisstats.backend.domain.Player;
import com.tennisstats.backend.domain.repository.PlayerRepository;
import com.tennisstats.backend.stats.MatchDetail;
import com.tennisstats.backend.stats.MatchPage;
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

    private static final String TOTAL_COUNT_HEADER = "X-Total-Count";

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;

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

    // limit and offset are taken as Strings so a non-numeric value gets the same ErrorResponse as an out-of-range one.
    @GetMapping("/{id}/matches")
    public ResponseEntity<?> getMatches(
            @PathVariable String id,
            @RequestParam(required = false) String limit,
            @RequestParam(required = false) String offset) {
        Integer parsedLimit = parseInt(limit, DEFAULT_LIMIT, 1, MAX_LIMIT);
        if (parsedLimit == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("query parameter 'limit' must be an integer from 1 to " + MAX_LIMIT));
        }
        Integer parsedOffset = parseInt(offset, 0, 0, Integer.MAX_VALUE);
        if (parsedOffset == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("query parameter 'offset' must be a non-negative integer"));
        }
        Long playerId = parsePlayerId(id);
        if (playerId == null || !playerRepository.existsById(playerId)) {
            return ResponseEntity.status(404).body(new ErrorResponse("No player with id '" + id + "'"));
        }
        MatchPage page = recentMatchesService.getMatches(playerId, parsedOffset, parsedLimit);
        List<MatchDetail> matches = page.matches();
        List<Long> playerIds = matches.stream()
                .flatMap(match -> Stream.concat(match.opponents().stream(), Stream.ofNullable(match.partner())))
                .map(Player::getId)
                .distinct()
                .toList();
        Map<Long, String> clubs = playerClubService.getClubs(playerIds);
        return ResponseEntity.ok()
                .header(TOTAL_COUNT_HEADER, String.valueOf(page.total()))
                .body(matches.stream().map(match -> RecentMatch.from(match, clubs)).toList());
    }

    private static Integer parseInt(String raw, int defaultValue, int min, int max) {
        if (raw == null) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(raw);
            return value >= min && value <= max ? value : null;
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
