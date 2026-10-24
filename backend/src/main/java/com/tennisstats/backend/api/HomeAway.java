package com.tennisstats.backend.api;

import com.tennisstats.backend.stats.HomeAwayStats;

/** Home and away win/loss records, per the {@code HomeAwayStats} schema in api/openapi.yaml. */
public record HomeAway(WinLoss home, WinLoss away) {

    public static HomeAway from(HomeAwayStats stats) {
        return new HomeAway(
                WinLoss.of(stats.homeWins(), stats.homeLosses()),
                WinLoss.of(stats.awayWins(), stats.awayLosses()));
    }
}
