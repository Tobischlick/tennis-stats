package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.Side;
import java.time.LocalDate;

/**
 * A projection of one tie-break {@link com.tennisstats.backend.domain.RubberSet} a player took part
 * in — its games (or points, for a match tie-break), which decide the winner — together with which
 * {@link Side} the player played on and when its meeting was played.
 */
record TieBreakSetRow(Integer homeScore, Integer awayScore, Side side, LocalDate playedOn) {

    boolean wonByPlayer() {
        // Games for a regular set, points for a match tie-break: either way the higher score won.
        boolean homeWon = homeScore > awayScore;
        return side == Side.HOME ? homeWon : !homeWon;
    }
}
