package com.tennisstats.backend.api;

import com.tennisstats.backend.stats.DecidingSetStats;

/** Deciding-set record, per the {@code DecidingSetStats} schema in api/openapi.yaml. */
public record DecidingSet(long played, long won, double winPercentage) {

    public static DecidingSet from(DecidingSetStats stats) {
        double percentage = stats.winRate() == null ? 0.0 : WinLoss.roundToOneDecimal(stats.winRate() * 100);
        return new DecidingSet(stats.played(), stats.won(), percentage);
    }
}
