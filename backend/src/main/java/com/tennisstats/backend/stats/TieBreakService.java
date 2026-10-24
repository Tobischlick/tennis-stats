package com.tennisstats.backend.stats;

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
        return statsOf(tieBreakSetRepository.findTieBreakSetRowsForPlayer(playerId));
    }

    /** The record over the given tie-break sets; shared with {@link SeasonStatsService} so both count alike. */
    static TieBreakStats statsOf(List<TieBreakSetRow> rows) {
        if (rows.isEmpty()) {
            return TieBreakStats.noData();
        }

        long won = rows.stream().filter(TieBreakSetRow::wonByPlayer).count();
        return new TieBreakStats(rows.size(), won, (double) won / rows.size());
    }
}
