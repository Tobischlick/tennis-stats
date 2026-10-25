package com.tennisstats.backend.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Level;
import com.tennisstats.backend.PostgresTestcontainer;
import com.tennisstats.backend.domain.Meeting;
import com.tennisstats.backend.domain.RubberType;
import com.tennisstats.backend.domain.Side;
import com.tennisstats.backend.domain.Team;
import com.tennisstats.backend.domain.repository.MeetingRepository;
import com.tennisstats.backend.domain.repository.RubberRepository;
import com.tennisstats.backend.domain.repository.TeamRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClientException;

@DataJpaTest
@Import(MeetingPersistenceService.class)
class CrawlServiceTest implements PostgresTestcontainer {

    private static final String BASE_URL = "https://example.test/wa";
    private static final String GROUP_URL = BASE_URL + "/groupPage?championship=B1+S+2026&group=1";
    private static final String OTHER_GROUP_URL = BASE_URL + "/groupPage?championship=B1+S+2026&group=2";
    private static final CrawlerProperties.Group GROUP = new CrawlerProperties.Group("B1 S 2026", "1");
    private static final CrawlerProperties.Group OTHER_GROUP = new CrawlerProperties.Group("B1 S 2026", "2");
    private static final String CHAMPIONSHIP = "B1 S 2026";
    private static final String LEAGUE_URL = BASE_URL + "/leaguePage?championship=B1+S+2026";
    private static final String OTHER_LEAGUE_URL = BASE_URL + "/leaguePage?championship=B2+S+2026";

    private final LogCapture logs = LogCapture.of(CrawlService.class);

    @AfterEach
    void stopCapturingLogs() {
        logs.close();
    }

    @Autowired
    private MeetingPersistenceService persistenceService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private RubberRepository rubberRepository;

    @Autowired
    private TeamRepository teamRepository;

    private static String fixture(String name) throws IOException {
        return Files.readString(Path.of("src/test/resources/fixtures/" + name));
    }

    private static String reportUrl(long meetingId) {
        return BASE_URL + "/meetingReport?meeting=" + meetingId + "&federation=BAD&championship=B1+S+2026";
    }

    private CrawlService crawlService(PageFetcher fetcher, CrawlerProperties.Group... groups) {
        return crawlService(fetcher, List.of(), groups);
    }

    private CrawlService crawlService(
            PageFetcher fetcher, List<String> championships, CrawlerProperties.Group... groups) {
        CrawlerProperties properties =
                new CrawlerProperties(true, Duration.ZERO, BASE_URL, "BAD", List.of(groups), championships);
        return new CrawlService(fetcher, persistenceService, meetingRepository, properties);
    }

    private static String leaguePage(String championship, String... groups) {
        StringBuilder links = new StringBuilder();
        for (String group : groups) {
            links.append(
                    "<a href=\"/cgi-bin/WebObjects/nuLigaTENDE.woa/wa/groupPage?championship=%s&amp;group=%s\">Gruppe</a>\n"
                            .formatted(championship.replace(' ', '+'), group));
        }
        return "<html><body>" + links + "</body></html>";
    }

    private static String groupPage(String... rows) {
        return """
                <html><body>
                <h2>Spielplan</h2>
                <table cellspacing="0" class="result-set" border="0" cellpadding="0">
                  <tr>
                    <th colspan="3">Datum</th>
                    <th>Heimmannschaft</th>
                    <th>Gastmannschaft</th>
                    <th class="center">Spielbericht</th>
                  </tr>
                """
                + String.join("\n", rows)
                + """
                </table>
                </body></html>
                """;
    }

    private static String row(String dateCell, String home, String away, long meetingId) {
        return """
                <tr>
                  <td>So</td>
                  <td nowrap="nowrap">%s</td>
                  <td nowrap="nowrap">&nbsp;</td>
                  <td nowrap="nowrap">%s</td>
                  <td nowrap="nowrap">%s</td>
                  <td class="center" nowrap="nowrap">
                    <a href="/cgi-bin/WebObjects/nuLigaTENDE.woa/wa/meetingReport?meeting=%d&amp;federation=BAD&amp;championship=B1+S+2026">anzeigen</a>
                  </td>
                </tr>
                """
                .formatted(dateCell, home, away, meetingId);
    }

    private static class FakePageFetcher implements PageFetcher {

        private final String groupPageHtml;
        private final String reportHtml;
        final List<String> fetchedUrls = new ArrayList<>();
        final Set<String> failingUrls = new HashSet<>();
        final Set<String> blockedUrls = new HashSet<>();
        final List<Thread> fetchThreads = new ArrayList<>();
        final Map<String, String> overrides = new HashMap<>();

        FakePageFetcher(String groupPageHtml, String reportHtml) {
            this.groupPageHtml = groupPageHtml;
            this.reportHtml = reportHtml;
        }

        @Override
        public String fetch(String url) {
            fetchedUrls.add(url);
            fetchThreads.add(Thread.currentThread());
            if (blockedUrls.contains(url)) {
                throw new CrawlFailedException("simulated 429 for " + url);
            }
            if (failingUrls.contains(url)) {
                throw new RestClientException("simulated failure for " + url);
            }
            if (overrides.containsKey(url)) {
                return overrides.get(url);
            }
            if (url.contains("/leaguePage?")) {
                return leaguePage(CHAMPIONSHIP, "1", "2");
            }
            if (url.contains("/groupPage?")) {
                return groupPageHtml;
            }
            if (url.contains("/meetingReport?")) {
                return reportHtml;
            }
            // an Error, not a RuntimeException, so CrawlService's catch can't hide an unexpected URL
            throw new AssertionError("unexpected URL " + url);
        }
    }

    private FakePageFetcher fixtureFetcher() throws IOException {
        return new FakePageFetcher(fixture("group-page.html"), fixture("meeting-report.html"));
    }

    @Test
    void crawlsEveryPlayedMeetingOfTheGroupOnAFirstRun() throws IOException {
        FakePageFetcher fetcher = fixtureFetcher();

        CrawlSummary summary = crawlService(fetcher, GROUP).crawl();

        assertThat(summary).isEqualTo(new CrawlSummary(1, 0, 28, 0, 0));
        assertThat(fetcher.fetchedUrls).hasSize(29);
        assertThat(meetingRepository.count()).isEqualTo(28);
        assertThat(rubberRepository.count()).isEqualTo(28 * 9);
    }

    @Test
    void buildsNuLigaUrlsFromTheConfiguredBaseUrlAndGroup() throws IOException {
        FakePageFetcher fetcher = fixtureFetcher();

        crawlService(fetcher, GROUP).crawl();

        assertThat(fetcher.fetchedUrls.get(0)).isEqualTo(GROUP_URL);
        assertThat(fetcher.fetchedUrls.get(1)).isEqualTo(reportUrl(12710540L));
    }

    @Test
    void skipsMeetingsAlreadyStoredWithAResult() throws IOException {
        crawlService(fixtureFetcher(), GROUP).crawl();
        FakePageFetcher secondRunFetcher = fixtureFetcher();

        CrawlSummary summary = crawlService(secondRunFetcher, GROUP).crawl();

        assertThat(secondRunFetcher.fetchedUrls).containsExactly(GROUP_URL);
        assertThat(summary).isEqualTo(new CrawlSummary(1, 0, 0, 28, 0));
    }

    @Test
    void runningTheCrawlTwiceDoesNotDuplicateData() throws IOException {
        crawlService(fixtureFetcher(), GROUP).crawl();
        crawlService(fixtureFetcher(), GROUP).crawl();

        assertThat(meetingRepository.count()).isEqualTo(28);
        assertThat(rubberRepository.count()).isEqualTo(28 * 9);
    }

    @Test
    void refetchesAMeetingStoredWithoutAnyResultYet() throws IOException {
        Team home = teamRepository.save(new Team("TC SW Neckarau 1"));
        Team away = teamRepository.save(new Team("TC 70 Sandhausen 1"));
        Meeting unplayed = new Meeting(home, away, LocalDate.of(2026, 5, 10));
        unplayed.setExternalId("12710540");
        unplayed.addRubber(RubberType.SINGLES, 1);
        meetingRepository.saveAndFlush(unplayed);
        FakePageFetcher fetcher = fixtureFetcher();

        CrawlSummary summary = crawlService(fetcher, GROUP).crawl();

        assertThat(fetcher.fetchedUrls).contains(reportUrl(12710540L));
        assertThat(summary.meetingsCrawled()).isEqualTo(28);
        Meeting refreshed = meetingRepository.findByExternalId("12710540").orElseThrow();
        assertThat(refreshed.getRubbers()).hasSize(9);
    }

    @Test
    void skipsAMeetingStoredAsAWalkoverWithoutAnySets() throws IOException {
        Team home = teamRepository.save(new Team("TC SW Neckarau 1"));
        Team away = teamRepository.save(new Team("TC 70 Sandhausen 1"));
        Meeting walkover = new Meeting(home, away, LocalDate.of(2026, 5, 10));
        walkover.setExternalId("12710540");
        walkover.addRubber(RubberType.SINGLES, 1).setRetiredSide(Side.AWAY);
        meetingRepository.saveAndFlush(walkover);
        FakePageFetcher fetcher = fixtureFetcher();

        CrawlSummary summary = crawlService(fetcher, GROUP).crawl();

        assertThat(fetcher.fetchedUrls).doesNotContain(reportUrl(12710540L));
        assertThat(summary.meetingsSkipped()).isEqualTo(1);
        assertThat(summary.meetingsCrawled()).isEqualTo(27);
    }

    @Test
    void oneFailingMeetingDoesNotAbortTheRun() throws IOException {
        FakePageFetcher fetcher = fixtureFetcher();
        fetcher.failingUrls.add(reportUrl(12710540L));
        fetcher.overrides.put(reportUrl(12710512L), "<html><body>no report here</body></html>");

        CrawlSummary summary = crawlService(fetcher, GROUP).crawl();

        assertThat(summary).isEqualTo(new CrawlSummary(1, 0, 26, 0, 2));
        assertThat(meetingRepository.count()).isEqualTo(26);
        assertThat(fetcher.fetchedUrls).hasSize(29);
        assertThat(meetingRepository.findByExternalId("12710540")).isEmpty();
        assertThat(meetingRepository.findByExternalId("12710512")).isEmpty();
    }

    @Test
    void skipsMeetingsWithoutAScheduledDate() throws IOException {
        FakePageFetcher fetcher = new FakePageFetcher(
                groupPage(
                        row("&nbsp;", "TC Musterstadt 1", "TC Beispielhausen 1", 501L),
                        row("10.05.2026 09:30", "TC Musterstadt 2", "TC Beispielhausen 2", 502L)),
                fixture("meeting-report.html"));

        CrawlSummary summary = crawlService(fetcher, GROUP).crawl();

        assertThat(fetcher.fetchedUrls).doesNotContain(reportUrl(501L)).contains(reportUrl(502L));
        assertThat(summary).isEqualTo(new CrawlSummary(1, 0, 1, 1, 0));
    }

    @Test
    void skipsMeetingsScheduledInTheFuture() throws IOException {
        FakePageFetcher fetcher = new FakePageFetcher(
                groupPage(
                        row("10.05.2026 09:30", "TC Musterstadt 1", "TC Beispielhausen 1", 601L),
                        row("01.01.2999 09:30", "TC Musterstadt 2", "TC Beispielhausen 2", 602L)),
                fixture("meeting-report.html"));

        CrawlSummary summary = crawlService(fetcher, GROUP).crawl();

        assertThat(fetcher.fetchedUrls).doesNotContain(reportUrl(602L)).contains(reportUrl(601L));
        assertThat(summary).isEqualTo(new CrawlSummary(1, 0, 1, 1, 0));
    }

    @Test
    void logsEverySkippedAndCrawledMeetingAtDebug() throws IOException {
        FakePageFetcher fetcher = new FakePageFetcher(
                groupPage(
                        row("&nbsp;", "TC Musterstadt 1", "TC Beispielhausen 1", 701L),
                        row("01.01.2999 09:30", "TC Musterstadt 2", "TC Beispielhausen 2", 702L),
                        row("10.05.2026 09:30", "TC Musterstadt 3", "TC Beispielhausen 3", 703L)),
                fixture("meeting-report.html"));

        crawlService(fetcher, GROUP).crawl();

        assertThat(logs.messages(Level.DEBUG))
                .containsExactly(
                        "Meeting 701 skipped: no date",
                        "Meeting 702 skipped: in the future",
                        "Meeting 703 crawled: 9 rubbers saved");
    }

    @Test
    void logsMeetingsAlreadyStoredWithAResultAsSkipped() throws IOException {
        crawlService(fixtureFetcher(), GROUP).crawl();
        crawlService(fixtureFetcher(), GROUP).crawl();

        assertThat(logs.messages(Level.DEBUG))
                .filteredOn(message -> message.endsWith(" skipped: already has results"))
                .hasSize(28)
                .contains("Meeting 12710540 skipped: already has results");
    }

    @Test
    void continuesWithTheNextGroupWhenAGroupPageFetchFails() throws IOException {
        FakePageFetcher fetcher = fixtureFetcher();
        fetcher.failingUrls.add(GROUP_URL);

        CrawlSummary summary = crawlService(fetcher, GROUP, OTHER_GROUP).crawl();

        assertThat(fetcher.fetchedUrls).contains(OTHER_GROUP_URL);
        assertThat(summary).isEqualTo(new CrawlSummary(2, 1, 28, 0, 0));
        assertThat(summary.nothingCouldBeCrawled()).isFalse();
    }

    @Test
    void reportsThatNothingCouldBeCrawledWhenEveryGroupPageFails() throws IOException {
        FakePageFetcher fetcher = fixtureFetcher();
        fetcher.failingUrls.add(GROUP_URL);
        fetcher.failingUrls.add(OTHER_GROUP_URL);

        CrawlSummary summary = crawlService(fetcher, GROUP, OTHER_GROUP).crawl();

        assertThat(summary).isEqualTo(new CrawlSummary(2, 2, 0, 0, 0));
        assertThat(summary.nothingCouldBeCrawled()).isTrue();
        assertThat(meetingRepository.count()).isZero();
    }

    @Test
    void stopsTheRunWhenAMeetingReportIsBlocked() throws IOException {
        FakePageFetcher fetcher = fixtureFetcher();
        fetcher.blockedUrls.add(reportUrl(12710540L));

        assertThatThrownBy(() -> crawlService(fetcher, GROUP).crawl()).isInstanceOf(CrawlFailedException.class);

        assertThat(fetcher.fetchedUrls).last().isEqualTo(reportUrl(12710540L));
        int reportsFetchedBeforeTheBlock = fetcher.fetchedUrls.size() - 2;
        assertThat(meetingRepository.count()).isEqualTo(reportsFetchedBeforeTheBlock);
        assertThat(meetingRepository.findByExternalId("12710540")).isEmpty();
    }

    @Test
    void stopsTheRunWhenAGroupPageIsBlocked() throws IOException {
        FakePageFetcher fetcher = fixtureFetcher();
        fetcher.blockedUrls.add(GROUP_URL);

        assertThatThrownBy(() -> crawlService(fetcher, GROUP, OTHER_GROUP).crawl())
                .isInstanceOf(CrawlFailedException.class);

        assertThat(fetcher.fetchedUrls).containsExactly(GROUP_URL);
        assertThat(meetingRepository.count()).isZero();
    }

    @Test
    void fetchesSequentiallyOnTheCallingThread() throws IOException {
        FakePageFetcher fetcher = fixtureFetcher();

        crawlService(fetcher, GROUP).crawl();

        assertThat(fetcher.fetchThreads).hasSize(29).containsOnly(Thread.currentThread());
    }

    @Test
    void crawlsEveryGroupLinkedFromAConfiguredChampionship() throws IOException {
        FakePageFetcher fetcher = fixtureFetcher();

        CrawlSummary summary = crawlService(fetcher, List.of(CHAMPIONSHIP)).crawl();

        assertThat(fetcher.fetchedUrls.get(0)).isEqualTo(LEAGUE_URL);
        assertThat(fetcher.fetchedUrls).contains(GROUP_URL, OTHER_GROUP_URL);
        assertThat(summary.groupsAttempted()).isEqualTo(2);
        assertThat(summary.groupsFailed()).isZero();
    }

    @Test
    void crawlsAGroupConfiguredExplicitlyAndViaItsChampionshipOnlyOnce() throws IOException {
        FakePageFetcher fetcher = fixtureFetcher();

        CrawlSummary summary = crawlService(fetcher, List.of(CHAMPIONSHIP), GROUP).crawl();

        assertThat(fetcher.fetchedUrls.stream().filter(GROUP_URL::equals)).hasSize(1);
        assertThat(fetcher.fetchedUrls.stream().filter(OTHER_GROUP_URL::equals)).hasSize(1);
        assertThat(summary.groupsAttempted()).isEqualTo(2);
    }

    @Test
    void countsAFailingOverviewPageAsAFailedGroupAndKeepsCrawling() throws IOException {
        FakePageFetcher fetcher = fixtureFetcher();
        fetcher.failingUrls.add(OTHER_LEAGUE_URL);

        CrawlSummary summary = crawlService(fetcher, List.of("B2 S 2026"), GROUP).crawl();

        assertThat(fetcher.fetchedUrls).contains(GROUP_URL);
        assertThat(summary).isEqualTo(new CrawlSummary(2, 1, 28, 0, 0));
        assertThat(summary.nothingCouldBeCrawled()).isFalse();
    }

    @Test
    void reportsThatNothingCouldBeCrawledWhenTheOnlyOverviewPageFails() throws IOException {
        FakePageFetcher fetcher = fixtureFetcher();
        fetcher.failingUrls.add(LEAGUE_URL);

        CrawlSummary summary = crawlService(fetcher, List.of(CHAMPIONSHIP)).crawl();

        assertThat(fetcher.fetchedUrls).containsExactly(LEAGUE_URL);
        assertThat(summary).isEqualTo(new CrawlSummary(1, 1, 0, 0, 0));
        assertThat(summary.nothingCouldBeCrawled()).isTrue();
    }

    @Test
    void countsAnOverviewPageWithoutGroupsOfItsChampionshipAsFailed() throws IOException {
        FakePageFetcher fetcher = fixtureFetcher();
        fetcher.overrides.put(OTHER_LEAGUE_URL, leaguePage(CHAMPIONSHIP, "1"));

        CrawlSummary summary = crawlService(fetcher, List.of("B2 S 2026")).crawl();

        assertThat(fetcher.fetchedUrls).containsExactly(OTHER_LEAGUE_URL);
        assertThat(summary).isEqualTo(new CrawlSummary(1, 1, 0, 0, 0));
    }

    @Test
    void stopsTheRunWhenAnOverviewPageIsBlocked() throws IOException {
        FakePageFetcher fetcher = fixtureFetcher();
        fetcher.blockedUrls.add(LEAGUE_URL);

        assertThatThrownBy(() -> crawlService(fetcher, List.of(CHAMPIONSHIP), GROUP).crawl())
                .isInstanceOf(CrawlFailedException.class);

        assertThat(fetcher.fetchedUrls).containsExactly(LEAGUE_URL);
        assertThat(meetingRepository.count()).isZero();
    }
}
