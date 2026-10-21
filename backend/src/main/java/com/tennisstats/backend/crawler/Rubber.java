package com.tennisstats.backend.crawler;

import com.tennisstats.backend.domain.Side;
import java.util.List;

/**
 * A single rubber (one row of "Einzelspiele" or "Doppelspiele") from a match report:
 * one player per side for singles, two per side for doubles. {@code retiredSide} is the side
 * nuLiga marks "(w.o.)" (retired, walkover or absent), {@code null} when none is.
 */
public record Rubber(
		int number,
		List<String> homePlayers,
		List<String> awayPlayers,
		List<Score> sets,
		Score matchesResult,
		Score setsResult,
		Score gamesResult,
		Side retiredSide) {
}
