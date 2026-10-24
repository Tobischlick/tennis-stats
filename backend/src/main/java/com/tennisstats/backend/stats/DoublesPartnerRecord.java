package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.Player;

/**
 * A player's record together with one doubles partner. Like {@link WinLossRecord}, only rubbers
 * with a known winner count, so {@code matchesPlayed} is {@code wins + losses}.
 */
public record DoublesPartnerRecord(Player partner, int wins, int losses) {

    public int matchesPlayed() {
        return wins + losses;
    }

    /** wins / matchesPlayed * 100, unrounded; 0 when there is no settled rubber. */
    public double winPercentage() {
        return matchesPlayed() == 0 ? 0.0 : 100.0 * wins / matchesPlayed();
    }
}
