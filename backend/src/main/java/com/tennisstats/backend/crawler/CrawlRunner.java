package com.tennisstats.backend.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

class CrawlRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CrawlRunner.class);

    private final CrawlService crawlService;

    CrawlRunner(CrawlService crawlService) {
        this.crawlService = crawlService;
    }

    @Override
    public void run(ApplicationArguments args) {
        CrawlSummary summary = crawlService.crawl();
        log.info("Crawl finished: {}", summary);
        if (summary.nothingCouldBeCrawled()) {
            throw new CrawlFailedException(
                    "All %d group and championship overview pages failed - nothing was crawled".formatted(summary.groupsAttempted()));
        }
    }
}
