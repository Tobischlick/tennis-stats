package com.tennisstats.backend.crawler;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a nuLiga championship overview page ("leaguePage") into the groups it links. Pure function: no DB,
 * no HTTP. The page also links other championships, so only groups of the given one are returned.
 */
class LeaguePageParser {

    private static final Logger log = LoggerFactory.getLogger(LeaguePageParser.class);

    private static final Pattern CHAMPIONSHIP_PATTERN = Pattern.compile("[?&]championship=([^&]*)");
    private static final Pattern GROUP_PATTERN = Pattern.compile("[?&]group=([^&]*)");

    List<CrawlerProperties.Group> parse(String html, String championship) {
        LinkedHashSet<CrawlerProperties.Group> groups = new LinkedHashSet<>();
        for (Element link : Jsoup.parse(html).select("a[href*=groupPage]")) {
            String href = link.attr("href");
            String linkedChampionship = queryParam(CHAMPIONSHIP_PATTERN, href);
            String group = queryParam(GROUP_PATTERN, href);
            if (championship.equals(linkedChampionship) && group != null && !group.isEmpty()) {
                groups.add(new CrawlerProperties.Group(linkedChampionship, group));
            }
        }
        log.debug("League page {}: {} groups found", championship, groups.size());
        return List.copyOf(groups);
    }

    private static String queryParam(Pattern pattern, String href) {
        Matcher matcher = pattern.matcher(href);
        return matcher.find() ? URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8) : null;
    }
}
