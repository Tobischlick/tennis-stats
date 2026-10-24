package com.tennisstats.backend.api;

import com.tennisstats.backend.domain.repository.PlayerRepository;
import com.tennisstats.backend.stats.SeasonStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerSeasonsController {

    private final PlayerRepository playerRepository;
    private final SeasonStatsService seasonStatsService;

    public PlayerSeasonsController(PlayerRepository playerRepository, SeasonStatsService seasonStatsService) {
        this.playerRepository = playerRepository;
        this.seasonStatsService = seasonStatsService;
    }

    // The spec defines no 400 on this path, so an id that can't be a stored id is just an unknown player.
    @GetMapping("/{id}/seasons")
    public ResponseEntity<?> getSeasons(@PathVariable String id) {
        Long playerId;
        try {
            playerId = Long.valueOf(id);
        } catch (NumberFormatException e) {
            playerId = null;
        }
        if (playerId == null || !playerRepository.existsById(playerId)) {
            return ResponseEntity.status(404).body(new ErrorResponse("No player with id '" + id + "'"));
        }
        return ResponseEntity.ok(seasonStatsService.getSeasonStats(playerId).stream()
                .map(PlayerSeason::from)
                .toList());
    }
}
