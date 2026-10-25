package com.tennisstats.backend.crawler;

import java.util.List;
import java.util.stream.Stream;

/**
 * A parsed nuLiga match-report ("Spielbericht") page: the two teams, the overall
 * match result, and every singles/doubles rubber.
 */
public record MatchReport(
		String homeTeam,
		String awayTeam,
		Score overallResult,
		List<Rubber> singles,
		List<Rubber> doubles) {

	/** Whether any rubber has a score yet - a walkover counts, it is scored 1:0 without sets. */
	public boolean hasResult() {
		return Stream.concat(singles.stream(), doubles.stream())
				.anyMatch(rubber -> !rubber.sets().isEmpty() || rubber.matchesResult() != null);
	}
}
