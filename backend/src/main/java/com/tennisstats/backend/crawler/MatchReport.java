package com.tennisstats.backend.crawler;

import java.util.List;

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
}
