package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.Side;

/** A projection of which {@link Side} of a rubber a player played on. */
record ParticipationRow(Long playerId, Long rubberId, Side side) {}
