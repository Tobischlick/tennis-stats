package com.tennisstats.backend.crawler;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("tennisstats.crawler")
record CrawlerProperties(
        boolean enabled,
        @DefaultValue("2s") Duration requestDelay,
        @DefaultValue("https://baden.liga.nu/cgi-bin/WebObjects/nuLigaTENDE.woa/wa") String baseUrl,
        @DefaultValue("BAD") String federation,
        @DefaultValue List<Group> groups,
        @DefaultValue List<String> championships) {

    record Group(String championship, String group) {
    }
}
