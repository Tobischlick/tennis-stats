package com.tennisstats.backend.api;

import com.tennisstats.backend.stats.WinLossRecord;

/** Win/loss record with a percentage, per the {@code WinLossRecord} schema in api/openapi.yaml. */
public record WinLoss(int wins, int losses, double winPercentage) {

    public static WinLoss from(WinLossRecord record) {
        return of(record.wins(), record.losses());
    }

    public static WinLoss of(int wins, int losses) {
        int decided = wins + losses;
        double winPercentage = decided == 0 ? 0.0 : roundToOneDecimal(100.0 * wins / decided);
        return new WinLoss(wins, losses, winPercentage);
    }

    static double roundToOneDecimal(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
