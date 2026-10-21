package com.tennisstats.backend.crawler;

import com.tennisstats.backend.domain.repository.MeetingRepository;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(CrawlerProperties.class)
@ConditionalOnProperty(prefix = "tennisstats.crawler", name = "enabled", havingValue = "true")
class CrawlerConfiguration {

    @Bean
    PageFetcher pageFetcher(CrawlerProperties properties) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(30));
        return new HttpPageFetcher(RestClient.builder().requestFactory(requestFactory), properties, Sleeper.threadSleep());
    }

    @Bean
    CrawlService crawlService(
            PageFetcher pageFetcher,
            MeetingPersistenceService persistenceService,
            MeetingRepository meetingRepository,
            CrawlerProperties properties) {
        return new CrawlService(pageFetcher, persistenceService, meetingRepository, properties);
    }

    @Bean
    @Profile("crawler")
    CrawlRunner crawlRunner(CrawlService crawlService) {
        return new CrawlRunner(crawlService);
    }
}
