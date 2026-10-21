package com.tennisstats.backend.api;

import com.tennisstats.backend.domain.Player;
import com.tennisstats.backend.domain.repository.PlayerRepository;
import com.tennisstats.backend.stats.PlayerClubService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerController {

    private final PlayerRepository playerRepository;
    private final PlayerClubService playerClubService;

    public PlayerController(PlayerRepository playerRepository, PlayerClubService playerClubService) {
        this.playerRepository = playerRepository;
        this.playerClubService = playerClubService;
    }

    @GetMapping
    public ResponseEntity<?> searchPlayers(@RequestParam(required = false) String query) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("query parameter 'query' is required"));
        }
        List<Player> players = playerRepository.searchByName(query);
        Map<Long, String> clubs =
                playerClubService.getClubs(players.stream().map(Player::getId).toList());
        List<PlayerSummary> results = players.stream()
                .map(player -> PlayerSummary.from(player, clubs.get(player.getId())))
                .toList();
        return ResponseEntity.ok(results);
    }
}
