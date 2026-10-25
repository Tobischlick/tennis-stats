package com.tennisstats.backend.api;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();
    private final Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    @BeforeEach
    void captureLogs() {
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void stopCapturingLogs() {
        logger.detachAppender(appender);
    }

    private List<String> messages() {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }

    private void handle(MockHttpServletRequest request, int status) throws ServletException, IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(status);
        filter.doFilter(request, response, new MockFilterChain());
    }

    @Test
    void logsAnApiRequestWhenItComesInAndWhenItIsAnswered() throws Exception {
        handle(new MockHttpServletRequest("GET", "/api/v1/overview"), 200);

        assertThat(messages()).hasSize(2);
        assertThat(messages().get(0)).isEqualTo("Handling GET /api/v1/overview");
        assertThat(messages().get(1)).matches("Handled GET /api/v1/overview -> 200 in \\d+ ms");
    }

    @Test
    void leavesTheQueryStringOut() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/players");
        request.setQueryString("query=Tobias+Schlick");
        request.setParameter("query", "Tobias Schlick");

        handle(request, 200);

        assertThat(messages()).first().isEqualTo("Handling GET /api/v1/players");
        assertThat(messages()).noneMatch(message -> message.contains("Schlick"));
    }

    @Test
    void logsTheStatusOfAnErrorResponse() throws Exception {
        handle(new MockHttpServletRequest("GET", "/api/v1/players/abc/stats"), 404);

        assertThat(messages().get(1)).matches("Handled GET /api/v1/players/abc/stats -> 404 in \\d+ ms");
    }

    @Test
    void ignoresRequestsOutsideTheApi() throws Exception {
        handle(new MockHttpServletRequest("GET", "/favicon.ico"), 404);

        assertThat(messages()).isEmpty();
    }
}
