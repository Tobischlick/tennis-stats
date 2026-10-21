package com.tennisstats.backend.api;

import com.tennisstats.backend.stats.DatabaseOverview;
import com.tennisstats.backend.stats.ScoreLineCount;
import java.time.LocalDate;

/** Matches the {@code Overview} schema (and its nested {@code Overview*} schemas) in api/openapi.yaml. */
public record Overview(boolean hasData, Totals totals, FunStats funStats) {

    public record Totals(
            long players,
            long clubs,
            long teams,
            long meetings,
            MatchCounts matches,
            long sets,
            LocalDate firstPlayedOn,
            LocalDate lastPlayedOn) {}

    public record MatchCounts(long total, long singles, long doubles) {}

    public record FunStats(
            long tieBreaks,
            long matchTieBreaks,
            long completedMatches,
            long decidingSetMatches,
            Double decidingSetPercentage,
            ScoreLine mostCommonScoreLine,
            int longestWinStreak) {}

    public record ScoreLine(String scoreLine, long count) {

        static ScoreLine from(ScoreLineCount scoreLine) {
            return scoreLine == null ? null : new ScoreLine(scoreLine.scoreLine(), scoreLine.count());
        }
    }

    public static Overview from(DatabaseOverview overview) {
        long matches = overview.singlesMatches() + overview.doublesMatches();
        Double decidingSetPercentage = overview.completedMatches() == 0
                ? null
                : WinLoss.roundToOneDecimal(100.0 * overview.decidingSetMatches() / overview.completedMatches());
        return new Overview(
                matches > 0,
                new Totals(
                        overview.players(),
                        overview.clubs(),
                        overview.teams(),
                        overview.meetings(),
                        new MatchCounts(matches, overview.singlesMatches(), overview.doublesMatches()),
                        overview.sets(),
                        overview.firstPlayedOn(),
                        overview.lastPlayedOn()),
                new FunStats(
                        overview.tieBreaks(),
                        overview.matchTieBreaks(),
                        overview.completedMatches(),
                        overview.decidingSetMatches(),
                        decidingSetPercentage,
                        ScoreLine.from(overview.mostCommonScoreLine()),
                        overview.longestWinStreak()));
    }
}
