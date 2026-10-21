package com.tennisstats.backend.stats;

/**
 * A player's deciding-set record: how many of their rubbers went to a third set (a {@code
 * REGULAR} set or a {@code MATCH_TIE_BREAK}) and how many of those they won.
 *
 * <p>{@code winRate} is {@code null} when {@code played} is zero, like {@link TieBreakStats}.
 */
public record DecidingSetStats(long played, long won, Double winRate) {

    public static DecidingSetStats noData() {
        return new DecidingSetStats(0, 0, null);
    }

    public boolean hasData() {
        return played > 0;
    }
}
