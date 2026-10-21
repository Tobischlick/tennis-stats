package com.tennisstats.backend.stats;

/**
 * A player's tie-break/deciding-set record: how often their sets went to a breaker (a
 * {@code MATCH_TIE_BREAK} "set", or a {@code REGULAR} set that ended 7:6 or 6:7) and how
 * often they won it.
 *
 * <p>{@code winRate} is {@code null} when {@code tieBreaksPlayed} is zero — a player who has never
 * played a tie-break has no rate to report, rather than a misleading {@code 0.0}.
 */
public record TieBreakStats(long tieBreaksPlayed, long tieBreaksWon, Double winRate) {

    public static TieBreakStats noData() {
        return new TieBreakStats(0, 0, null);
    }

    public boolean hasData() {
        return tieBreaksPlayed > 0;
    }
}
