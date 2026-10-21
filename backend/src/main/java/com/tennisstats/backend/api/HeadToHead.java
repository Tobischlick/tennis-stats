package com.tennisstats.backend.api;

import com.tennisstats.backend.stats.HeadToHeadRecord;
import com.tennisstats.backend.stats.MatchResult;
import java.util.List;

/** Matches the {@code HeadToHead} schema in api/openapi.yaml. */
public record HeadToHead(
        String playerId,
        PlayerSummary player,
        String opponentId,
        PlayerSummary opponent,
        int matchesPlayed,
        boolean hasEnoughData,
        WinLoss record,
        List<MatchResult> recentResults) {

    public static HeadToHead from(HeadToHeadRecord head, PlayerSummary player, PlayerSummary opponent) {
        String playerId = String.valueOf(head.playerAId());
        String opponentId = String.valueOf(head.playerBId());
        int matchesPlayed = head.playerAWins() + head.playerBWins();
        if (matchesPlayed == 0) {
            return new HeadToHead(playerId, player, opponentId, opponent, 0, false, null, List.of());
        }
        return new HeadToHead(
                playerId,
                player,
                opponentId,
                opponent,
                matchesPlayed,
                true,
                WinLoss.of(head.playerAWins(), head.playerBWins()),
                head.recentResults());
    }
}
