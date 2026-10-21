package com.tennisstats.backend.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class CrawlRunnerTest {

    private static class StubCrawlService extends CrawlService {

        private final CrawlSummary summary;

        StubCrawlService(CrawlSummary summary) {
            super(null, null, null, null);
            this.summary = summary;
        }

        @Override
        CrawlSummary crawl() {
            return summary;
        }
    }

    private static CrawlRunner runnerReporting(CrawlSummary summary) {
        return new CrawlRunner(new StubCrawlService(summary));
    }

    @Test
    void completesWhenAtLeastOneGroupWasCrawled() {
        CrawlRunner runner = runnerReporting(new CrawlSummary(2, 1, 5, 3, 0));

        assertThatCode(() -> runner.run(new DefaultApplicationArguments())).doesNotThrowAnyException();
    }

    @Test
    void completesWhenOnlyIndividualMeetingsFailed() {
        CrawlRunner runner = runnerReporting(new CrawlSummary(1, 0, 0, 0, 4));

        assertThatCode(() -> runner.run(new DefaultApplicationArguments())).doesNotThrowAnyException();
    }

    @Test
    void failsWithExitCodeOneWhenEveryGroupPageFailed() {
        CrawlRunner runner = runnerReporting(new CrawlSummary(2, 2, 0, 0, 0));

        assertThatThrownBy(() -> runner.run(new DefaultApplicationArguments()))
                .isInstanceOfSatisfying(CrawlFailedException.class, e -> {
                    assertThat(e.getExitCode()).isEqualTo(1);
                    assertThat(e).hasMessageContaining("All 2 group and championship overview pages failed");
                });
    }

    @Test
    void completesWhenNoGroupsAreConfigured() {
        CrawlRunner runner = runnerReporting(new CrawlSummary(0, 0, 0, 0, 0));

        assertThatCode(() -> runner.run(new DefaultApplicationArguments())).doesNotThrowAnyException();
    }
}
