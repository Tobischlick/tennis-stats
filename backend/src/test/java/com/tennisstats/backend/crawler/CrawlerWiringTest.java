package com.tennisstats.backend.crawler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

class CrawlerWiringTest {

    @Nested
    @SpringBootTest
    class WhenTheCrawlerIsDisabled {

        @Autowired
        private ApplicationContext context;

        @Test
        void noCrawlerBeansExist() {
            assertThat(context.getBeanNamesForType(CrawlService.class)).isEmpty();
            assertThat(context.getBeanNamesForType(PageFetcher.class)).isEmpty();
            assertThat(context.getBeanNamesForType(CrawlRunner.class)).isEmpty();
        }
    }

    @Nested
    @SpringBootTest(properties = "tennisstats.crawler.enabled=true")
    class WhenTheCrawlerIsEnabledWithoutTheCrawlerProfile {

        @Autowired
        private ApplicationContext context;

        @Test
        void theCrawlServiceExistsButNothingRunsIt() {
            assertThat(context.getBeanNamesForType(CrawlService.class)).hasSize(1);
            assertThat(context.getBeanNamesForType(CrawlRunner.class)).isEmpty();
        }

        @Test
        void propertiesFallBackToTheirDefaultsAndNoGroupsAreConfigured() {
            CrawlerProperties properties = context.getBean(CrawlerProperties.class);

            assertThat(properties.enabled()).isTrue();
            assertThat(properties.requestDelay()).isEqualTo(Duration.ofSeconds(2));
            assertThat(properties.federation()).isEqualTo("BAD");
            assertThat(properties.baseUrl()).startsWith("https://baden.liga.nu/");
            assertThat(properties.groups()).isEmpty();
        }
    }
}
