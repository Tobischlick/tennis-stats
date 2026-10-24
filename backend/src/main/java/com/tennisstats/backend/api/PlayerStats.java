package com.tennisstats.backend.api;

import com.tennisstats.backend.stats.DecidingSetStats;
import com.tennisstats.backend.stats.FirstSetStats;
import com.tennisstats.backend.stats.StreakStats;
import com.tennisstats.backend.stats.TieBreakStats;
import com.tennisstats.backend.stats.WinLossRecord;

/** Matches the {@code PlayerStats} schema in api/openapi.yaml. */
public record PlayerStats(
        String playerId,
        PlayerSummary player,
        int matchesPlayed,
        boolean hasEnoughData,
        WinLoss record,
        TieBreak tieBreak,
        Streak streak,
        DecidingSet decidingSet,
        FirstSet firstSet) {

    public static PlayerStats from(
            Long playerId,
            PlayerSummary player,
            WinLossRecord winLoss,
            TieBreakStats tieBreak,
            StreakStats streak,
            DecidingSetStats decidingSet,
            FirstSetStats firstSet) {
        int matchesPlayed = winLoss.wins() + winLoss.losses();
        String id = String.valueOf(playerId);
        if (matchesPlayed == 0) {
            return new PlayerStats(id, player, 0, false, null, null, null, null, null);
        }
        return new PlayerStats(
                id,
                player,
                matchesPlayed,
                true,
                WinLoss.from(winLoss),
                TieBreak.from(tieBreak),
                Streak.from(streak),
                DecidingSet.from(decidingSet),
                FirstSet.from(firstSet));
    }
}
