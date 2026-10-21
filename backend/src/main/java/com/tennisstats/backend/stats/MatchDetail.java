package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.Player;
import com.tennisstats.backend.domain.RubberType;
import java.time.LocalDate;
import java.util.List;

/**
 * One settled rubber from one player's perspective: who they played with and against, for which
 * teams, and the set scores. {@code partner} is {@code null} for singles.
 */
public record MatchDetail(
        LocalDate playedOn,
        RubberType type,
        MatchResult result,
        List<Player> opponents,
        Player partner,
        String ownTeam,
        String opponentTeam,
        List<SetScore> sets) {}
