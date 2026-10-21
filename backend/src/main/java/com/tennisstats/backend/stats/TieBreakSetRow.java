package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.SetType;
import com.tennisstats.backend.domain.Side;

/**
 * A projection of one tie-break {@link com.tennisstats.backend.domain.RubberSet} a player took part
 * in, together with which {@link Side} the player played on.
 */
record TieBreakSetRow(
        SetType type, Integer homeScore, Integer awayScore, Integer homeTiebreakScore, Integer awayTiebreakScore, Side side) {}
