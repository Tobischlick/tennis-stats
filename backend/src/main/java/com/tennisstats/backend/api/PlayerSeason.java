package com.tennisstats.backend.api;

import com.tennisstats.backend.stats.Season;
import com.tennisstats.backend.stats.SeasonStats;
import java.time.LocalDate;

/** One season of a player's record, per the {@code PlayerSeason} schema in api/openapi.yaml. */
public record PlayerSeason(
        String season,
        Season.Type type,
        LocalDate startsOn,
        LocalDate endsOn,
        int matchesPlayed,
        WinLoss record,
        TieBreak tieBreak) {

    public static PlayerSeason from(SeasonStats stats) {
        Season season = stats.season();
        return new PlayerSeason(
                season.label(),
                season.type(),
                season.startsOn(),
                season.endsOn(),
                stats.record().wins() + stats.record().losses(),
                WinLoss.from(stats.record()),
                TieBreak.from(stats.tieBreak()));
    }
}
