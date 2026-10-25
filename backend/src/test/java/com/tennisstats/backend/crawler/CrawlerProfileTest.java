package com.tennisstats.backend.crawler;

import static org.assertj.core.api.Assertions.assertThat;

import com.tennisstats.backend.PostgresTestcontainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

// NONE matches the web-application-type=none the profile sets. @SpringBootTest does run
// ApplicationRunners, and the profile file configures the real championship, so the empty lists (which
// replace the whole list) keep the run a zero-request no-op.
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {"tennisstats.crawler.groups=", "tennisstats.crawler.championships="})
@ActiveProfiles("crawler")
class CrawlerProfileTest implements PostgresTestcontainer {

    @Autowired
    private ApplicationContext context;

    @Test
    void theCrawlerProfileRegistersExactlyOneRunner() {
        assertThat(context.getBeansOfType(CrawlRunner.class)).hasSize(1);
    }

    @Test
    void theCrawlerProfileLogsTheCrawlerAtInfoUnlessCrawlerLogLevelIsSet() {
        assertThat(context.getEnvironment().getProperty("logging.level.com.tennisstats.backend.crawler"))
                .isEqualTo("INFO");
    }
}
