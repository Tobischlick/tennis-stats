package com.tennisstats.backend.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tennisstats.backend.domain.Side;

/**
 * Parses a nuLiga match-report ("Spielbericht") page into plain DTOs. Pure function:
 * no DB access, no HTTP calls - the caller is responsible for fetching the HTML.
 */
public final class MatchReportParser {

	private static final Logger log = LoggerFactory.getLogger(MatchReportParser.class);

	private static final Pattern SCORE_PATTERN = Pattern.compile("(\\d+):(\\d+)");
	private static final String RETIRED_MARKER = "(w.o.)";
	private static final Pattern NAME_SUFFIX_PATTERN = Pattern.compile("\\s*\\(.*\\)\\s*$");
	private static final Pattern NATIONALITY_MARKER_PATTERN =
			Pattern.compile("\\s+(?:[A-Z]{3}|k\\.A\\.)(?:\\*(?:\\s+gA)?|\\s+gA)$");

	private MatchReportParser() {
	}

	public static MatchReport parse(String html) {
		return parse(Jsoup.parse(html));
	}

	public static MatchReport parse(Document document) {
		Element table = document.selectFirst("table.result-set");
		if (table == null) {
			throw new IllegalArgumentException("No match-report table (table.result-set) found");
		}

		Elements teamHeaders = table.select("th");
		String homeTeam = teamHeaders.get(0).text().trim();
		String awayTeam = teamHeaders.get(1).text().trim();

		List<Rubber> singles = new ArrayList<>();
		List<Rubber> doubles = new ArrayList<>();
		Score overallResult = null;

		Section section = Section.NONE;
		int singleNumber = 0;
		int doubleNumber = 0;

		for (Element row : table.select("tr")) {
			if (!row.select("h2").isEmpty()) {
				String heading = row.select("h2").text();
				if (heading.contains("Einzelspiele")) {
					section = Section.SINGLES;
				} else if (heading.contains("Doppelspiele")) {
					section = Section.DOUBLES;
				}
				continue;
			}
			if (!row.select("strong").isEmpty() && row.select("strong").text().contains("Gesamt")) {
				overallResult = parseOverallResult(row);
				continue;
			}

			Elements playerLinks = row.select("a[rel^=ibox]");
			if (playerLinks.isEmpty()) {
				continue;
			}

			Rubber rubber = switch (section) {
				case SINGLES -> parseRubber(row, playerLinks, ++singleNumber, 1);
				case DOUBLES -> parseRubber(row, playerLinks, ++doubleNumber, 2);
				case NONE -> null;
			};
			if (rubber == null) {
				continue;
			}
			if (section == Section.SINGLES) {
				singles.add(rubber);
			} else {
				doubles.add(rubber);
			}
		}

		log.debug("Match report {} vs {}: {} singles, {} doubles found", homeTeam, awayTeam, singles.size(),
				doubles.size());
		return new MatchReport(homeTeam, awayTeam, overallResult, singles, doubles);
	}

	private static Rubber parseRubber(Element row, Elements playerLinks, int number, int playersPerSide) {
		List<String> homePlayers = new ArrayList<>();
		List<String> awayPlayers = new ArrayList<>();
		for (int i = 0; i < playerLinks.size(); i++) {
			String name = cleanPlayerName(playerLinks.get(i).text());
			if (i < playersPerSide) {
				homePlayers.add(name);
			} else {
				awayPlayers.add(name);
			}
		}

		List<Element> summaryCells = row.select("td[align=center]");
		summaryCells = summaryCells.subList(Math.max(0, summaryCells.size() - 6), summaryCells.size());

		List<Score> sets = new ArrayList<>();
		for (int i = 0; i < 3 && i < summaryCells.size(); i++) {
			parseScore(summaryCells.get(i).text()).ifPresent(sets::add);
		}
		Score matchesResult = parseScore(summaryCells.get(3).text()).orElse(null);
		Score setsResult = parseScore(summaryCells.get(4).text()).orElse(null);
		Score gamesResult = parseScore(summaryCells.get(5).text()).orElse(null);

		return new Rubber(number, homePlayers, awayPlayers, sets, matchesResult, setsResult, gamesResult,
				retiredSide(playerLinks, playersPerSide));
	}

	/** nuLiga appends "(w.o.)" to a player name of the side that retired, conceded or didn't turn up. */
	private static Side retiredSide(Elements playerLinks, int playersPerSide) {
		for (int i = 0; i < playerLinks.size(); i++) {
			if (playerLinks.get(i).text().contains(RETIRED_MARKER)) {
				return i < playersPerSide ? Side.HOME : Side.AWAY;
			}
		}
		return null;
	}

	private static Score parseOverallResult(Element gesamtRow) {
		List<Element> summaryCells = gesamtRow.select("td[align=center]");
		if (summaryCells.isEmpty()) {
			return null;
		}
		return parseScore(summaryCells.get(0).text()).orElse(null);
	}

	/**
	 * Strips nuLiga's suffixes from a player name: the bracketed ranking/LK and "(w.o.)" parts, then
	 * a trailing nationality marker - a three-letter country code or "k.A." followed by "*" and/or
	 * "gA" (e.g. "SVK gA", "UKR*", "k.A.*"). A bare all-caps token without "*" or "gA" is kept, as
	 * it can't be told apart from a real name part.
	 */
	static String cleanPlayerName(String rawText) {
		String withoutSuffixes = NAME_SUFFIX_PATTERN.matcher(rawText.trim()).replaceAll("").trim();
		return NATIONALITY_MARKER_PATTERN.matcher(withoutSuffixes).replaceAll("");
	}

	private static Optional<Score> parseScore(String text) {
		Matcher matcher = SCORE_PATTERN.matcher(text.trim());
		if (!matcher.find()) {
			return Optional.empty();
		}
		return Optional.of(new Score(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
	}

	private enum Section {
		NONE, SINGLES, DOUBLES
	}
}
