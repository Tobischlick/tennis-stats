package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.RubberSet;
import com.tennisstats.backend.domain.SetType;
import com.tennisstats.backend.domain.Side;

/**
 * One set of a rubber from one player's perspective. For a match tie-break, {@code own} and {@code
 * opponent} are points rather than games; the tie-break fields are only set for a regular set that
 * went to a breaker.
 */
public record SetScore(int own, int opponent, Integer ownTieBreak, Integer opponentTieBreak, boolean matchTieBreak) {

    static SetScore of(RubberSet set, Side side) {
        boolean home = side == Side.HOME;
        return new SetScore(
                home ? set.getHomeScore() : set.getAwayScore(),
                home ? set.getAwayScore() : set.getHomeScore(),
                home ? set.getHomeTiebreakScore() : set.getAwayTiebreakScore(),
                home ? set.getAwayTiebreakScore() : set.getHomeTiebreakScore(),
                set.getType() == SetType.MATCH_TIE_BREAK);
    }
}
