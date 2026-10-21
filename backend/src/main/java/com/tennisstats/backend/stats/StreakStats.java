package com.tennisstats.backend.stats;

import java.util.List;

/**
 * A player's current streak and recent-form results, ordered most recent first.
 *
 * <p>{@code hasData} is {@code false} when the player has no settled rubbers yet; in that case
 * {@code streakResult} is {@code null}, {@code streakLength} is {@code 0} and {@code
 * recentResults} is empty, and should not be presented as a real streak.
 */
public record StreakStats(
        Long playerId, boolean hasData, MatchResult streakResult, int streakLength, List<MatchResult> recentResults) {

    public static StreakStats noData(Long playerId) {
        return new StreakStats(playerId, false, null, 0, List.of());
    }

    public static StreakStats of(
            Long playerId, MatchResult streakResult, int streakLength, List<MatchResult> recentResults) {
        return new StreakStats(playerId, true, streakResult, streakLength, recentResults);
    }
}
