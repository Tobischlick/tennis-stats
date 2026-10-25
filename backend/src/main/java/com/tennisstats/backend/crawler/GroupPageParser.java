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
    // positions in the layout without the optional "Spielort"/"Platz" columns, used when the table has no header row
    private static final int DEFAULT_HOME_TEAM_COLUMN = 3;
    private static final int DEFAULT_AWAY_TEAM_COLUMN = 4;

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

        int homeTeamColumn = columnIndex(scheduleTable, "Heimmannschaft", DEFAULT_HOME_TEAM_COLUMN);
        int awayTeamColumn = columnIndex(scheduleTable, "Gastmannschaft", DEFAULT_AWAY_TEAM_COLUMN);

        LocalDateTime currentDate = null;
        int unparsedReportRows = 0;
        for (Element row : scheduleTable.select("tr")) {
            Elements cells = row.select("> td");
            if (cells.isEmpty()) {
                continue; // header row
            }

            LocalDateTime parsedDate = parseDate(cells);
            if (parsedDate != null) {
                currentDate = parsedDate;
            }

            String homeTeam = cellText(cells, homeTeamColumn);
            String awayTeam = cellText(cells, awayTeamColumn);
            Long meetingId = extractMeetingId(row);

            if (meetingId == null) {
                continue; // bye or missing report link
            }
            if (homeTeam.isEmpty() || awayTeam.isEmpty()) {
                unparsedReportRows++;
                continue;
            }

            meetings.add(new ScheduledMeetingDto(homeTeam, awayTeam, currentDate, meetingId));
        }

        if (meetings.isEmpty() && unparsedReportRows > 0) {
            log.warn("Group page: {} rows link a match report but none has both teams, 0 meetings", unparsedReportRows);
        }
        log.debug("Group page: {} meetings found", meetings.size());
        return meetings;
    }

    // header cells can span several columns ("Datum" spans three), so count colspans to find the body cell index
    private int columnIndex(Element scheduleTable, String header, int fallback) {
        int column = 0;
        for (Element headerCell : scheduleTable.select("tr > th")) {
            if (headerCell.text().trim().equals(header)) {
                return column;
            }
            column += colspan(headerCell);
        }
        return fallback;
    }

    private int colspan(Element cell) {
        try {
            return Math.max(1, Integer.parseInt(cell.attr("colspan").trim()));
        } catch (NumberFormatException e) {
            return 1;
        }
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
