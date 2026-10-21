package com.tennisstats.backend.api;

import com.tennisstats.backend.stats.MatchResult;
import com.tennisstats.backend.stats.StreakStats;
import java.util.List;

/** Current streak and recent form, per the {@code Streak} schema in api/openapi.yaml. */
public record Streak(Current current, List<MatchResult> recentResults) {

    public record Current(MatchResult result, int count) {}

    public static Streak from(StreakStats stats) {
        if (!stats.hasData()) {
            return null;
        }
        return new Streak(new Current(stats.streakResult(), stats.streakLength()), stats.recentResults());
    }
}
