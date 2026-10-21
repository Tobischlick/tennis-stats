package com.tennisstats.backend.stats;

import java.time.LocalDate;

/**
 * Database-wide totals and fun stats for the landing page; see the {@code Overview} schema in
 * api/openapi.yaml for the exact definition of each number. Nothing here identifies a player.
 *
 * <p>On an empty database every count is {@code 0} and {@code firstPlayedOn}, {@code lastPlayedOn}
 * and {@code mostCommonScoreLine} are {@code null}.
 */
public record DatabaseOverview(
        long players,
        long clubs,
        long teams,
        long meetings,
        long singlesMatches,
        long doublesMatches,
        long sets,
        LocalDate firstPlayedOn,
        LocalDate lastPlayedOn,
        long tieBreaks,
        long matchTieBreaks,
        long completedMatches,
        long decidingSetMatches,
        ScoreLineCount mostCommonScoreLine,
        int longestWinStreak) {}
