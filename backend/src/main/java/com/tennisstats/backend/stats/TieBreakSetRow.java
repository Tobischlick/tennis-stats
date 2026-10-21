package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.Side;

/**
 * A projection of one tie-break {@link com.tennisstats.backend.domain.RubberSet} a player took part
 * in — its games (or points, for a match tie-break), which decide the winner — together with which
 * {@link Side} the player played on.
 */
record TieBreakSetRow(Integer homeScore, Integer awayScore, Side side) {}
