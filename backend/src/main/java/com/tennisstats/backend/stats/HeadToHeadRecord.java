package com.tennisstats.backend.stats;

import java.util.List;

/**
 * The head-to-head record between two players, scoped to singles rubbers where they played each
 * other directly (see {@link HeadToHeadService} for why doubles encounters aren't counted).
 *
 * <p>{@code hasMatches} is {@code false} when the two players have never played each other in
 * scope; in that case {@code matchesPlayed}, {@code playerAWins} and {@code playerBWins} are all
 * {@code 0} and should be presented as "no matches yet" rather than a real 0-0 record.
 *
 * <p>{@code recentResults} lists the settled encounters newest first, from player A's
 * perspective. Unsettled rubbers count toward {@code matchesPlayed} but add no entry.
 */
public record HeadToHeadRecord(
        Long playerAId,
        Long playerBId,
        boolean hasMatches,
        int matchesPlayed,
        int playerAWins,
        int playerBWins,
        List<MatchResult> recentResults) {

    public static HeadToHeadRecord noMatches(Long playerAId, Long playerBId) {
        return new HeadToHeadRecord(playerAId, playerBId, false, 0, 0, 0, List.of());
    }

    public static HeadToHeadRecord of(
            Long playerAId,
            Long playerBId,
            int matchesPlayed,
            int playerAWins,
            int playerBWins,
            List<MatchResult> recentResults) {
        return new HeadToHeadRecord(playerAId, playerBId, true, matchesPlayed, playerAWins, playerBWins, recentResults);
    }
}
