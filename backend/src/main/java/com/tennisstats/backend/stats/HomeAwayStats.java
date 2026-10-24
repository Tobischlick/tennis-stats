package com.tennisstats.backend.stats;

/**
 * A player's win/loss record split by whether their team was the home or the away team of the
 * meeting. Counts the same rubbers as {@link WinLossRecord}, so home and away add up to it.
 */
public record HomeAwayStats(int homeWins, int homeLosses, int awayWins, int awayLosses) {

    public static HomeAwayStats noData() {
        return new HomeAwayStats(0, 0, 0, 0);
    }

    public boolean hasData() {
        return homeWins + homeLosses + awayWins + awayLosses > 0;
    }
}
