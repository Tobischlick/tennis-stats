package com.tennisstats.backend.crawler;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class LeaguePageParserTest {

    private final LeaguePageParser parser = new LeaguePageParser();
    private final LogCapture logs = LogCapture.of(LeaguePageParser.class);

    @AfterEach
    void stopCapturingLogs() {
        logs.close();
    }

    private String loadFixture() throws IOException {
        return Files.readString(Path.of("src/test/resources/fixtures/league-page.html"));
    }

    @Test
    void parsesEveryGroupOfTheChampionshipInPageOrder() throws IOException {
        List<CrawlerProperties.Group> groups = parser.parse(loadFixture(), "B1 S 2026");

        List<CrawlerProperties.Group> expected = IntStream.rangeClosed(1, 47)
                .mapToObj(n -> new CrawlerProperties.Group("B1 S 2026", String.valueOf(n)))
                .toList();
        assertThat(groups).isEqualTo(expected);
    }

    @Test
    void logsHowManyGroupsItFoundAtDebug() throws IOException {
        parser.parse(loadFixture(), "B1 S 2026");

        assertThat(logs.messages(Level.DEBUG)).containsExactly("League page B1 S 2026: 47 groups found");
    }

    @Test
    void ignoresGroupsOfOtherChampionships() throws IOException {
        assertThat(parser.parse(loadFixture(), "B1 M 2026")).isEmpty();
    }

    @Test
    void filtersForeignChampionshipsAndDeduplicatesLinks() {
        String html = """
                <html><body>
                <a href="/wa/groupPage?championship=B1+S+2026&amp;group=3">Gruppe 3</a>
                <a href="/wa/groupPage?championship=B1+W+26%2F27&amp;group=1">other</a>
                <a href="/wa/groupPage?championship=B1+S+2026&amp;group=3">Gruppe 3 again</a>
                <a href="/wa/leaguePage?championship=B1+S+2026">overview</a>
                <a href="/wa/groupPage?championship=B1+S+2026&amp;group=4">Gruppe 4</a>
                </body></html>
                """;

        assertThat(parser.parse(html, "B1 S 2026"))
                .containsExactly(
                        new CrawlerProperties.Group("B1 S 2026", "3"),
                        new CrawlerProperties.Group("B1 S 2026", "4"));
        assertThat(parser.parse(html, "B1 W 26/27"))
                .containsExactly(new CrawlerProperties.Group("B1 W 26/27", "1"));
    }
}
