package com.tennisstats.backend.stats;

import java.util.List;

/**
 * The head-to-head record between two players: the singles record in the top-level fields, and
 * doubles encounters on opposite sides as a separate {@link Doubles} record (see {@link
 * HeadToHeadService} for why the two are never mixed).
 *
 * <p>{@code hasMatches} is {@code false} when the two players have never played each other in
 * singles; in that case {@code matchesPlayed}, {@code playerAWins} and {@code playerBWins} are all
 * {@code 0} and should be presented as "no singles yet" rather than a real 0-0 record. {@code
 * doubles} is always present and may have meetings even then.
 *
 * <p>{@code recentResults} lists the settled singles encounters newest first, from player A's
 * perspective. Unsettled rubbers count toward {@code matchesPlayed} but add no entry.
 */
public record HeadToHeadRecord(
        Long playerAId,
        Long playerBId,
        boolean hasMatches,
        int matchesPlayed,
        int playerAWins,
        int playerBWins,
        List<MatchResult> recentResults,
        Doubles doubles) {

    public static HeadToHeadRecord noMatches(Long playerAId, Long playerBId) {
        return noSinglesMatches(playerAId, playerBId, Doubles.NONE);
    }

    public static HeadToHeadRecord noSinglesMatches(Long playerAId, Long playerBId, Doubles doubles) {
        return new HeadToHeadRecord(playerAId, playerBId, false, 0, 0, 0, List.of(), doubles);
    }

    public static HeadToHeadRecord of(
            Long playerAId,
            Long playerBId,
            int matchesPlayed,
            int playerAWins,
            int playerBWins,
            List<MatchResult> recentResults,
            Doubles doubles) {
        return new HeadToHeadRecord(
                playerAId, playerBId, true, matchesPlayed, playerAWins, playerBWins, recentResults, doubles);
    }

    /**
     * Doubles rubbers in which the two players stood on opposite sides, each with their own partner.
     * Rubbers where they played together as partners are not part of this. Like the singles fields,
     * {@code matchesPlayed} includes unsettled rubbers while {@code meetings} lists only the settled
     * ones, newest first, from player A's perspective (so {@code partner} is player A's partner and
     * {@code opponents} holds player B and player B's partner).
     */
    public record Doubles(int matchesPlayed, int playerAWins, int playerBWins, List<MatchDetail> meetings) {

        public static final Doubles NONE = new Doubles(0, 0, 0, List.of());
    }
}
