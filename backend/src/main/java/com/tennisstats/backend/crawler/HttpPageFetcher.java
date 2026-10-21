package com.tennisstats.backend.crawler;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

class HttpPageFetcher implements PageFetcher {

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
        // URI.create keeps the already-encoded query from being encoded a second time
        return restClient
                .get()
                .uri(URI.create(url))
                .retrieve()
                // nuLiga asking us to back off or refusing access ends the run: never retry or work around it
                .onStatus(
                        status -> status.isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)
                                || status.isSameCodeAs(HttpStatus.FORBIDDEN),
                        (request, response) -> {
                            throw new CrawlFailedException(
                                    "nuLiga answered %d for %s - stopping the crawl instead of retrying"
                                            .formatted(response.getStatusCode().value(), url));
                        })
                .body(String.class);
    }
}
