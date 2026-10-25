package com.tennisstats.backend.crawler;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.tennisstats.backend.domain.Side;

class MatchReportParserTest {

	private static MatchReport report;

	@BeforeAll
	static void parseFixture() throws IOException {
		Path fixture = Path.of("src/test/resources/fixtures/meeting-report.html");
		String html = Files.readString(fixture);
		report = MatchReportParser.parse(html);
	}

	@Test
	void parsesTeamsAndOverallResult() {
		assertThat(report.homeTeam()).isEqualTo("TK GW Mannheim 3");
		assertThat(report.awayTeam()).isEqualTo("TC Weinheim 1902 2");
		assertThat(report.overallResult()).isEqualTo(new Score(2, 7));
	}

	@Test
	void logsHowManyRubbersItFoundAtDebug() throws IOException {
		String html = Files.readString(Path.of("src/test/resources/fixtures/meeting-report.html"));
		try (LogCapture logs = LogCapture.of(MatchReportParser.class)) {
			MatchReportParser.parse(html);

			assertThat(logs.messages(Level.DEBUG))
					.containsExactly("Match report TK GW Mannheim 3 vs TC Weinheim 1902 2: 6 singles, 3 doubles found");
		}
	}

	@Test
	void parsesSinglesAndDoublesRubberCounts() {
		assertThat(report.singles()).hasSize(6);
		assertThat(report.doubles()).hasSize(3);
	}

	@Test
	void parsesANormalStraightSetsRubber() {
		Rubber rubberOne = report.singles().get(0);

		assertThat(rubberOne.number()).isEqualTo(1);
		assertThat(rubberOne.homePlayers()).containsExactly("Pohlmann, Ole");
		assertThat(rubberOne.awayPlayers()).containsExactly("Oberle, Leon");
		assertThat(rubberOne.sets()).containsExactly(new Score(0, 6), new Score(0, 6));
		assertThat(rubberOne.matchesResult()).isEqualTo(new Score(0, 1));
		assertThat(rubberOne.setsResult()).isEqualTo(new Score(0, 2));
		assertThat(rubberOne.gamesResult()).isEqualTo(new Score(0, 12));
	}

	@Test
	void capturesDecidingSetMatchTieBreakOnRubberSix() {
		Rubber rubberSix = report.singles().get(5);

		assertThat(rubberSix.number()).isEqualTo(6);
		assertThat(rubberSix.homePlayers()).containsExactly("Fahrbach, Ansgar");
		assertThat(rubberSix.awayPlayers()).containsExactly("Hasselbach, Leon");
		assertThat(rubberSix.sets()).containsExactly(new Score(1, 6), new Score(7, 5), new Score(10, 8));

		Score thirdSet = rubberSix.sets().get(2);
		assertThat(thirdSet.isMatchTieBreak()).isTrue();
		assertThat(rubberSix.matchesResult()).isEqualTo(new Score(1, 0));
	}

	@Test
	void parsesADoublesRubberWithBothPlayersPerSide() {
		Rubber doubleOne = report.doubles().get(0);

		assertThat(doubleOne.homePlayers()).containsExactly("Pohlmann, Ole", "Zirngibl, Jakob");
		assertThat(doubleOne.awayPlayers()).containsExactly("Oberle, Leon", "Jurisic, Bastian");
		assertThat(doubleOne.sets()).containsExactly(new Score(1, 6), new Score(2, 6));
		assertThat(doubleOne.matchesResult()).isEqualTo(new Score(0, 1));
	}

	@Test
	void keepsTheAbsentPlayerPlaceholderOfAnUnfieldedDoublesPair() throws IOException {
		String html = Files.readString(Path.of("src/test/resources/fixtures/meeting-report-12710538.html"));
		Rubber doubleThree = MatchReportParser.parse(html).doubles().get(2);

		assertThat(doubleThree.homePlayers()).containsExactly("Gieselmann, Fabian", "Hornbacher, Moritz");
		assertThat(doubleThree.awayPlayers())
				.containsExactly("Spieler/in nicht anwesend", "Spieler/in nicht anwesend");
		assertThat(doubleThree.sets()).isEmpty();
	}

	@Test
	void marksTheSideThatRetiredMidMatch() throws IOException {
		String html = Files.readString(Path.of("src/test/resources/fixtures/meeting-report-12710515.html"));
		MatchReport retirements = MatchReportParser.parse(html);

		Rubber homeRetired = retirements.singles().get(1);
		assertThat(homeRetired.homePlayers()).containsExactly("Czerwinski, Nils");
		assertThat(homeRetired.retiredSide()).isEqualTo(Side.HOME);
		assertThat(homeRetired.sets()).containsExactly(new Score(0, 3));
		assertThat(homeRetired.matchesResult()).isEqualTo(new Score(0, 1));
		assertThat(homeRetired.setsResult()).isEqualTo(new Score(0, 2));
		assertThat(homeRetired.gamesResult()).isEqualTo(new Score(0, 12));

		Rubber awayRetired = retirements.singles().get(4);
		assertThat(awayRetired.awayPlayers()).containsExactly("Hornbacher, Moritz");
		assertThat(awayRetired.retiredSide()).isEqualTo(Side.AWAY);
		assertThat(awayRetired.sets()).containsExactly(new Score(6, 0), new Score(1, 0));
		assertThat(awayRetired.matchesResult()).isEqualTo(new Score(1, 0));

		Rubber walkover = retirements.doubles().get(0);
		assertThat(walkover.retiredSide()).isEqualTo(Side.HOME);
		assertThat(walkover.sets()).isEmpty();
		assertThat(walkover.matchesResult()).isEqualTo(new Score(0, 1));

		assertThat(retirements.singles().get(0).retiredSide()).isNull();
	}

	@Test
	void tellsFinishedSetsFromOnesCutShort() {
		assertThat(new Score(6, 4).isFinishedSet()).isTrue();
		assertThat(new Score(5, 7).isFinishedSet()).isTrue();
		assertThat(new Score(7, 6).isFinishedSet()).isTrue();
		assertThat(new Score(8, 10).isFinishedSet()).isTrue();
		assertThat(new Score(0, 3).isFinishedSet()).isFalse();
		assertThat(new Score(1, 0).isFinishedSet()).isFalse();
		assertThat(new Score(6, 5).isFinishedSet()).isFalse();
		assertThat(new Score(10, 9).isFinishedSet()).isFalse();
	}

	@Test
	void stripsGuestMarkersFromARealReport() throws IOException {
		String html = Files.readString(Path.of("src/test/resources/fixtures/meeting-report-12710538.html"));
		MatchReport report = MatchReportParser.parse(html);

		assertThat(report.singles().get(0).homePlayers()).containsExactly("Dvorsky, Ondrej");
		assertThat(report.singles().get(1).homePlayers()).containsExactly("Pichler, Emil");
		assertThat(report.singles().get(1).awayPlayers()).containsExactly("Mladenov, Krasimir");
		assertThat(report.doubles().get(0).homePlayers()).containsExactly("Dvorsky, Ondrej", "Pichler, Emil");
	}

	@Test
	void stripsAsteriskMarkerFromARealReport() throws IOException {
		String html = Files.readString(Path.of("src/test/resources/fixtures/meeting-report-12895864.html"));
		MatchReport report = MatchReportParser.parse(html);

		assertThat(report.singles().get(0).homePlayers()).containsExactly("Dragnev, Dimitar");
		assertThat(report.singles().get(3).awayPlayers()).containsExactly("Voronyak, Bohdan");
		assertThat(report.doubles().get(1).awayPlayers()).containsExactly("Sauerbrey, Quirin", "Voronyak, Bohdan");
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', textBlock = """
			Fekete, Janos SVK gA (3, LK5,1) | Fekete, Janos
			Pichler, Emil AUT gA (2) (w.o.) | Pichler, Emil
			Garcia, Alvaro MEX* (4)         | Garcia, Alvaro
			Voronyak, Bohdan UKR*           | Voronyak, Bohdan
			Doe, John USA* gA (1)           | Doe, John
			Spieler/in nicht anwesend k.A.* | Spieler/in nicht anwesend
			""")
	void stripsNationalityMarkers(String raw, String expected) {
		assertThat(MatchReportParser.cleanPlayerName(raw)).isEqualTo(expected);
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', textBlock = """
			Ng, Li (5)                   | Ng, Li
			Ali, MO (5)                  | Ali, MO
			Kim, Jae JUN (5)             | Kim, Jae JUN
			Meier, Anna-Lena (5, LK10,0) | Meier, Anna-Lena
			Schmidt, Ga (5)              | Schmidt, Ga
			OKAFOR, Chidi (5)            | OKAFOR, Chidi
			Lopez, Ana GA (5)            | Lopez, Ana GA
			""")
	void keepsLegitimateNameParts(String raw, String expected) {
		assertThat(MatchReportParser.cleanPlayerName(raw)).isEqualTo(expected);
	}
}
