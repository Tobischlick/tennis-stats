package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.Rubber;
import com.tennisstats.backend.domain.RubberSet;
import com.tennisstats.backend.domain.Side;

/** Derives the winner of a {@link Rubber} from its retired side or set scores, since no entity stores that directly. */
final class RubberOutcome {

    private RubberOutcome() {}

    /**
     * The side that didn't retire if one did, otherwise the side that won more sets of this rubber,
     * or {@code null} if the sets don't settle it.
     */
    static Side winnerOf(Rubber rubber) {
        if (rubber.getRetiredSide() != null) {
            return rubber.getRetiredSide() == Side.HOME ? Side.AWAY : Side.HOME;
        }
        int homeSetsWon = 0;
        int awaySetsWon = 0;
        for (RubberSet set : rubber.getSets()) {
            if (set.getHomeScore() > set.getAwayScore()) {
                homeSetsWon++;
            } else if (set.getAwayScore() > set.getHomeScore()) {
                awaySetsWon++;
            }
        }
        if (homeSetsWon > awaySetsWon) {
            return Side.HOME;
        }
        if (awaySetsWon > homeSetsWon) {
            return Side.AWAY;
        }
        return null;
    }
}
