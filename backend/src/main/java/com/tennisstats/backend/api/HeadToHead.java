package com.tennisstats.backend.api;

import com.tennisstats.backend.stats.HeadToHeadRecord;
import com.tennisstats.backend.stats.MatchResult;
import java.util.List;
import java.util.Map;

/**
 * Matches the {@code HeadToHead} schema in api/openapi.yaml: the singles record at the top level,
 * doubles separately in {@code doubles}.
 */
public record HeadToHead(
        String playerId,
        PlayerSummary player,
        String opponentId,
        PlayerSummary opponent,
        int matchesPlayed,
        boolean hasEnoughData,
        WinLoss record,
        List<MatchResult> recentResults,
        DoublesHeadToHead doubles) {

    public static HeadToHead from(
            HeadToHeadRecord head, PlayerSummary player, PlayerSummary opponent, Map<Long, String> clubs) {
        String playerId = String.valueOf(head.playerAId());
        String opponentId = String.valueOf(head.playerBId());
        DoublesHeadToHead doubles = DoublesHeadToHead.from(head.doubles(), head.playerBId(), clubs);
        int matchesPlayed = head.playerAWins() + head.playerBWins();
        if (matchesPlayed == 0) {
            return new HeadToHead(playerId, player, opponentId, opponent, 0, false, null, List.of(), doubles);
        }
        return new HeadToHead(
                playerId,
                player,
                opponentId,
                opponent,
                matchesPlayed,
                true,
                WinLoss.of(head.playerAWins(), head.playerBWins()),
                head.recentResults(),
                doubles);
    }
}
