package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.Side;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Computes a player's tie-break/deciding-set win rate: how they perform when a set comes down to
 * a breaker, the pressure stat BTV itself never shows.
 */
@Service
public class TieBreakService {

    private final TieBreakSetRepository tieBreakSetRepository;

    public TieBreakService(TieBreakSetRepository tieBreakSetRepository) {
        this.tieBreakSetRepository = tieBreakSetRepository;
    }

    public TieBreakStats getTieBreakStats(Long playerId) {
        List<TieBreakSetRow> rows = tieBreakSetRepository.findTieBreakSetRowsForPlayer(playerId);
        if (rows.isEmpty()) {
            return TieBreakStats.noData();
        }

        long won = rows.stream().filter(TieBreakService::wonByPlayer).count();
        return new TieBreakStats(rows.size(), won, (double) won / rows.size());
    }

    private static boolean wonByPlayer(TieBreakSetRow row) {
        // Games for a regular set, points for a match tie-break: either way the higher score won.
        boolean homeWon = row.homeScore() > row.awayScore();
        return row.side() == Side.HOME ? homeWon : !homeWon;
    }
}
