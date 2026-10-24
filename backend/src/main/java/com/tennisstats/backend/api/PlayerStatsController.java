package com.tennisstats.backend.api;

import com.tennisstats.backend.domain.Player;
import com.tennisstats.backend.domain.repository.PlayerRepository;
import com.tennisstats.backend.stats.DecidingSetService;
import com.tennisstats.backend.stats.DoublesPartnerRecord;
import com.tennisstats.backend.stats.DoublesPartnerService;
import com.tennisstats.backend.stats.FirstSetService;
import com.tennisstats.backend.stats.HeadToHeadRecord;
import com.tennisstats.backend.stats.HeadToHeadService;
import com.tennisstats.backend.stats.HomeAwayService;
import com.tennisstats.backend.stats.PlayerClubService;
import com.tennisstats.backend.stats.StreakService;
import com.tennisstats.backend.stats.TieBreakService;
import com.tennisstats.backend.stats.WinLossService;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerStatsController {

    private final PlayerRepository playerRepository;
    private final WinLossService winLossService;
    private final TieBreakService tieBreakService;
    private final StreakService streakService;
    private final HeadToHeadService headToHeadService;
    private final DecidingSetService decidingSetService;
    private final FirstSetService firstSetService;
    private final DoublesPartnerService doublesPartnerService;
    private final HomeAwayService homeAwayService;
    private final PlayerClubService playerClubService;

    public PlayerStatsController(
            PlayerRepository playerRepository,
            WinLossService winLossService,
            TieBreakService tieBreakService,
            StreakService streakService,
            HeadToHeadService headToHeadService,
            DecidingSetService decidingSetService,
            FirstSetService firstSetService,
            DoublesPartnerService doublesPartnerService,
            HomeAwayService homeAwayService,
            PlayerClubService playerClubService) {
        this.playerRepository = playerRepository;
        this.winLossService = winLossService;
        this.tieBreakService = tieBreakService;
        this.streakService = streakService;
        this.headToHeadService = headToHeadService;
        this.decidingSetService = decidingSetService;
        this.firstSetService = firstSetService;
        this.doublesPartnerService = doublesPartnerService;
        this.homeAwayService = homeAwayService;
        this.playerClubService = playerClubService;
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getPlayerStats(@PathVariable String id) {
        Long playerId = existingPlayerId(id);
        if (playerId == null) {
            return notFound(id);
        }
        return ResponseEntity.ok(PlayerStats.from(
                playerId,
                summaryOf(playerId),
                winLossService.getWinLossRecord(playerId),
                tieBreakService.getTieBreakStats(playerId),
                streakService.getStreakStats(playerId),
                decidingSetService.getDecidingSetStats(playerId),
                firstSetService.getFirstSetStats(playerId),
                homeAwayService.getHomeAwayStats(playerId)));
    }

    @GetMapping("/{id}/head-to-head/{opponentId}")
    public ResponseEntity<?> getHeadToHead(@PathVariable String id, @PathVariable String opponentId) {
        Long playerId = existingPlayerId(id);
        if (playerId == null) {
            return notFound(id);
        }
        Long opponent = existingPlayerId(opponentId);
        if (opponent == null) {
            return notFound(opponentId);
        }
        HeadToHeadRecord head = headToHeadService.getHeadToHead(playerId, opponent);
        List<Long> partnerIds = head.doubles().meetings().stream()
                .flatMap(meeting -> Stream.concat(meeting.opponents().stream(), Stream.ofNullable(meeting.partner())))
                .map(Player::getId)
                .distinct()
                .toList();
        return ResponseEntity.ok(HeadToHead.from(
                head, summaryOf(playerId), summaryOf(opponent), playerClubService.getClubs(partnerIds)));
    }

    @GetMapping("/{id}/doubles-partners")
    public ResponseEntity<?> getDoublesPartners(@PathVariable String id) {
        Long playerId = existingPlayerId(id);
        if (playerId == null) {
            return notFound(id);
        }
        List<DoublesPartnerRecord> partners = doublesPartnerService.getDoublesPartners(playerId);
        List<Long> partnerIds = partners.stream().map(partner -> partner.partner().getId()).toList();
        return ResponseEntity.ok(DoublesPartners.from(
                playerId, summaryOf(playerId), partners, playerClubService.getClubs(partnerIds)));
    }

    // The spec defines no 400 on these paths, so an id that can't be a stored id is just an unknown player.
    private Long existingPlayerId(String rawId) {
        Long id;
        try {
            id = Long.valueOf(rawId);
        } catch (NumberFormatException e) {
            return null;
        }
        return playerRepository.existsById(id) ? id : null;
    }

    private PlayerSummary summaryOf(Long playerId) {
        return PlayerSummary.from(
                playerRepository.findById(playerId).orElseThrow(),
                playerClubService.getClubs(List.of(playerId)).get(playerId));
    }

    private static ResponseEntity<ErrorResponse> notFound(String rawId) {
        return ResponseEntity.status(404).body(new ErrorResponse("No player with id '" + rawId + "'"));
    }
}
