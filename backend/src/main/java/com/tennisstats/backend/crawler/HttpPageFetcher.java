package com.tennisstats.backend.crawler;

import java.net.URI;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

class HttpPageFetcher implements PageFetcher {

    private static final Logger log = LoggerFactory.getLogger(HttpPageFetcher.class);

    private final RestClient restClient;
    private final CrawlerProperties properties;
    private final Sleeper sleeper;

    HttpPageFetcher(RestClient.Builder builder, CrawlerProperties properties, Sleeper sleeper) {
        // no request factory here: the caller (or a test's mock server) owns it
        this.restClient = builder.build();
        this.properties = properties;
        this.sleeper = sleeper;
    }

    @Override
    public String fetch(String url) {
        // before every request, group pages included, so the politeness delay covers all traffic
        sleeper.sleep(properties.requestDelay());
        // started after the delay, so the logged duration is nuLiga's response time only
        long start = System.nanoTime();
        try {
            // URI.create keeps the already-encoded query from being encoded a second time
            ResponseEntity<String> response = restClient
                    .get()
                    .uri(URI.create(url))
                    .retrieve()
                    // nuLiga asking us to back off or refusing access ends the run: never retry or work around it
                    .onStatus(
                            status -> status.isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)
                                    || status.isSameCodeAs(HttpStatus.FORBIDDEN),
                            (request, errorResponse) -> {
                                int status = errorResponse.getStatusCode().value();
                                log.error("GET {} -> {} in {} ms, stopping the crawl", url, status, elapsedMillis(start));
                                throw new CrawlFailedException(
                                        "nuLiga answered %d for %s - stopping the crawl instead of retrying"
                                                .formatted(status, url));
                            })
                    .toEntity(String.class);
            log.debug("GET {} -> {} in {} ms", url, response.getStatusCode().value(), elapsedMillis(start));
            return response.getBody();
        } catch (RestClientResponseException e) {
            log.debug("GET {} -> {} in {} ms", url, e.getStatusCode().value(), elapsedMillis(start));
            throw e;
        }
    }

    private static long elapsedMillis(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
    }
}
