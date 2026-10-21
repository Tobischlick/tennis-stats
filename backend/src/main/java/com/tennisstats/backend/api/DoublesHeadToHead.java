package com.tennisstats.backend.api;

import com.tennisstats.backend.stats.HeadToHeadRecord;
import java.util.List;
import java.util.Map;

/** Matches the {@code DoublesHeadToHead} schema in api/openapi.yaml. */
public record DoublesHeadToHead(
        int matchesPlayed, boolean hasEnoughData, WinLoss record, List<DoublesMeeting> meetings) {

    static DoublesHeadToHead from(HeadToHeadRecord.Doubles doubles, Long opponentId, Map<Long, String> clubs) {
        int matchesPlayed = doubles.playerAWins() + doubles.playerBWins();
        if (matchesPlayed == 0) {
            return new DoublesHeadToHead(0, false, null, List.of());
        }
        return new DoublesHeadToHead(
                matchesPlayed,
                true,
                WinLoss.of(doubles.playerAWins(), doubles.playerBWins()),
                doubles.meetings().stream()
                        .map(meeting -> DoublesMeeting.from(meeting, opponentId, clubs))
                        .toList());
    }
}
