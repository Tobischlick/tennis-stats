package com.tennisstats.backend.crawler;

import org.springframework.boot.ExitCodeGenerator;

/**
 * Ends the crawl run so Spring Boot exits non-zero: thrown out of {@link CrawlRunner} when every
 * group page failed, and by {@link HttpPageFetcher} as soon as nuLiga answers 429 or 403. Individual
 * meeting failures deliberately do not use it: a crawl that stored some data is a successful run.
 */
class CrawlFailedException extends RuntimeException implements ExitCodeGenerator {

    CrawlFailedException(String message) {
        super(message);
    }

    @Override
    public int getExitCode() {
        return 1;
    }
}
