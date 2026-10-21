package com.tennisstats.backend.api;

import com.tennisstats.backend.stats.TieBreakStats;

/** Tie-break record, per the {@code TieBreakStats} schema in api/openapi.yaml. */
public record TieBreak(long tieBreaksPlayed, long tieBreaksWon, double tieBreakWinPercentage) {

    public static TieBreak from(TieBreakStats stats) {
        double percentage = stats.winRate() == null ? 0.0 : WinLoss.roundToOneDecimal(stats.winRate() * 100);
        return new TieBreak(stats.tieBreaksPlayed(), stats.tieBreaksWon(), percentage);
    }
}
