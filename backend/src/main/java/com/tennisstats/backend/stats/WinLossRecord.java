package com.tennisstats.backend.stats;

/**
 * A player's aggregate win/loss record across all rubbers (singles and doubles) they've played.
 *
 * <p>{@code hasData} is {@code false} when the player has no recorded rubbers yet; in that case
 * {@code wins}, {@code losses} and {@code winPercentage} are all {@code 0} and should not be
 * presented as a real 0% record.
 */
public record WinLossRecord(Long playerId, boolean hasData, int wins, int losses, double winPercentage) {

    public static WinLossRecord noData(Long playerId) {
        return new WinLossRecord(playerId, false, 0, 0, 0.0);
    }

    public static WinLossRecord of(Long playerId, int wins, int losses) {
        int decided = wins + losses;
        double winPercentage = decided == 0 ? 0.0 : 100.0 * wins / decided;
        return new WinLossRecord(playerId, true, wins, losses, winPercentage);
    }
}
