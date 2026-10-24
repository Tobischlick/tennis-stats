package com.tennisstats.backend.api;

import com.tennisstats.backend.stats.FirstSetStats;

/** Record after the first set, per the {@code FirstSetStats} schema in api/openapi.yaml. */
public record FirstSet(
        long lostFirstSet,
        long comebacks,
        double comebackPercentage,
        long wonFirstSet,
        long blownLeads,
        double blownLeadPercentage) {

    public static FirstSet from(FirstSetStats stats) {
        return new FirstSet(
                stats.lostFirstSet(),
                stats.comebacks(),
                percentage(stats.comebackRate()),
                stats.wonFirstSet(),
                stats.blownLeads(),
                percentage(stats.blownLeadRate()));
    }

    private static double percentage(Double rate) {
        return rate == null ? 0.0 : WinLoss.roundToOneDecimal(rate * 100);
    }
}
