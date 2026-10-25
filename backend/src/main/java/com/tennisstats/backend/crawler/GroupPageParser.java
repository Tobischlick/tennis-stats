package com.tennisstats.backend.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a nuLiga group/schedule page ("Spielplan") into DTOs. Pure function: no DB, no HTTP.
 */
public class GroupPageParser {

    private static final Logger log = LoggerFactory.getLogger(GroupPageParser.class);

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final Pattern DATE_TIME_PATTERN = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}\\s+\\d{2}:\\d{2}");
    private static final Pattern MEETING_ID_PATTERN = Pattern.compile("meeting=(\\d+)");

    public List<ScheduledMeetingDto> parse(String html) {
        return parse(Jsoup.parse(html));
    }

    public List<ScheduledMeetingDto> parse(Document document) {
        List<ScheduledMeetingDto> meetings = new ArrayList<>();

        Element scheduleTable = document.select("h2:contains(Spielplan) ~ table").first();
        if (scheduleTable == null) {
            log.debug("Group page: no schedule table found, 0 meetings");
            return meetings;
        }

        LocalDateTime currentDate = null;
        for (Element row : scheduleTable.select("tr")) {
            Elements cells = row.select("> td");
            if (cells.isEmpty()) {
                continue; // header row
            }

            LocalDateTime parsedDate = parseDate(cells);
            if (parsedDate != null) {
                currentDate = parsedDate;
            }

            String homeTeam = cellText(cells, 3);
            String awayTeam = cellText(cells, 4);
            Long meetingId = extractMeetingId(row);

            if (homeTeam.isEmpty() || awayTeam.isEmpty() || meetingId == null) {
                continue; // bye or missing report link
            }

            meetings.add(new ScheduledMeetingDto(homeTeam, awayTeam, currentDate, meetingId));
        }

        log.debug("Group page: {} meetings found", meetings.size());
        return meetings;
    }

    private LocalDateTime parseDate(Elements cells) {
        if (cells.size() < 2) {
            return null;
        }
        String raw = cells.get(1).text().replace('\u00A0', ' ').trim();
        if (!DATE_TIME_PATTERN.matcher(raw).matches()) {
            return null;
        }
        return LocalDateTime.parse(raw, DATE_TIME_FORMAT);
    }

    private String cellText(Elements cells, int index) {
        if (index >= cells.size()) {
            return "";
        }
        return cells.get(index).text().replace('\u00A0', ' ').trim();
    }

    private Long extractMeetingId(Element row) {
        for (Element link : row.select("a[href*=meetingReport]")) {
            Matcher matcher = MEETING_ID_PATTERN.matcher(link.attr("href"));
            if (matcher.find()) {
                return Long.parseLong(matcher.group(1));
            }
        }
        return null;
    }
}
