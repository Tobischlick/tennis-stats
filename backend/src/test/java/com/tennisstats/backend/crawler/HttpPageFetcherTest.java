package com.tennisstats.backend.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

class HttpPageFetcherTest {

    private static final String URL = "https://example.test/wa/groupPage?championship=B1+S+2026&group=1";

    private final List<String> events = new ArrayList<>();
    private final List<Duration> sleeps = new ArrayList<>();

    private static CrawlerProperties properties(Duration requestDelay) {
        return new CrawlerProperties(true, requestDelay, "https://example.test/wa", "BAD", List.of(), List.of());
    }

    private HttpPageFetcher fetcher(RestClient.Builder builder, Duration requestDelay) {
        return new HttpPageFetcher(
                builder,
                properties(requestDelay),
                duration -> {
                    sleeps.add(duration);
                    events.add("sleep");
                });
    }

    @Test
    void returnsTheResponseBody() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpPageFetcher fetcher = fetcher(builder, Duration.ZERO);
        server.expect(requestTo(URL)).andRespond(withSuccess("<html>Spielplan</html>", MediaType.TEXT_HTML));

        String body = fetcher.fetch(URL);

        assertThat(body).isEqualTo("<html>Spielplan</html>");
    }

    @Test
    void waitsTheConfiguredDelayBeforeEachRequest() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpPageFetcher fetcher = fetcher(builder, Duration.ofSeconds(2));
        server.expect(requestTo(URL))
                .andExpect(request -> events.add("request"))
                .andRespond(withSuccess("first", MediaType.TEXT_HTML));
        server.expect(requestTo(URL))
                .andExpect(request -> events.add("request"))
                .andRespond(withSuccess("second", MediaType.TEXT_HTML));

        fetcher.fetch(URL);
        fetcher.fetch(URL);

        assertThat(sleeps).containsExactly(Duration.ofSeconds(2), Duration.ofSeconds(2));
        assertThat(events).containsExactly("sleep", "request", "sleep", "request");
    }

    @Test
    void propagatesAnHttpErrorAsAnException() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpPageFetcher fetcher = fetcher(builder, Duration.ZERO);
        server.expect(requestTo(URL)).andRespond(withServerError());

        assertThatThrownBy(() -> fetcher.fetch(URL)).isInstanceOf(RestClientException.class);
    }

    @Test
    void stopsWithACrawlFailureOnTooManyRequests() {
        assertStopsTheCrawlOn(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void stopsWithACrawlFailureOnForbidden() {
        assertStopsTheCrawlOn(HttpStatus.FORBIDDEN);
    }

    private void assertStopsTheCrawlOn(HttpStatus status) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpPageFetcher fetcher = fetcher(builder, Duration.ZERO);
        server.expect(requestTo(URL)).andRespond(withStatus(status));

        assertThatThrownBy(() -> fetcher.fetch(URL))
                .isInstanceOfSatisfying(CrawlFailedException.class, e -> {
                    assertThat(e.getExitCode()).isEqualTo(1);
                    assertThat(e).hasMessageContaining(String.valueOf(status.value())).hasMessageContaining(URL);
                });
        server.verify();
    }
}
