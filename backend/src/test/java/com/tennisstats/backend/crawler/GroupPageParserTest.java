package com.tennisstats.backend.crawler;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupPageParserTest {

    private final GroupPageParser parser = new GroupPageParser();
    private final LogCapture logs = LogCapture.of(GroupPageParser.class);

    @AfterEach
    void stopCapturingLogs() {
        logs.close();
    }

    private String loadFixture() throws IOException {
        return loadFixture("group-page.html");
    }

    private String loadFixture(String name) throws IOException {
        Path path = Path.of("src/test/resources/fixtures", name);
        return Files.readString(path);
    }

    @Test
    void parsesOneDtoPerScheduledMeeting() throws IOException {
        List<ScheduledMeetingDto> meetings = parser.parse(loadFixture());

        assertEquals(28, meetings.size());
    }

    @Test
    void logsHowManyMeetingsItFoundAtDebug() throws IOException {
        parser.parse(loadFixture());

        assertEquals(List.of("Group page: 28 meetings found"), logs.messages(Level.DEBUG));
    }

    @Test
    void parsesFirstMeetingOfTheSchedule() throws IOException {
        List<ScheduledMeetingDto> meetings = parser.parse(loadFixture());

        ScheduledMeetingDto first = meetings.get(0);
        assertEquals("TC SW Neckarau 1", first.homeTeam());
        assertEquals("TC 70 Sandhausen 1", first.awayTeam());
        assertEquals(LocalDateTime.of(2026, 5, 10, 9, 30), first.date());
        assertEquals(12710540L, first.meetingId());
    }

    @Test
    void carriesForwardTheDateForMeetingsSharingTheSameMatchday() throws IOException {
        List<ScheduledMeetingDto> meetings = parser.parse(loadFixture());

        // Second meeting's date cell is a "tabelle-rowspan" placeholder in the source markup,
        // sharing the matchday date of the first meeting.
        ScheduledMeetingDto second = meetings.get(1);
        assertEquals("TK GW Mannheim 3", second.homeTeam());
        assertEquals("TC Weinheim 1902 2", second.awayTeam());
        assertEquals(LocalDateTime.of(2026, 5, 10, 9, 30), second.date());
        assertEquals(12710512L, second.meetingId());
    }

    @Test
    void parsesLastMeetingOfTheSchedule() throws IOException {
        List<ScheduledMeetingDto> meetings = parser.parse(loadFixture());

        ScheduledMeetingDto last = meetings.get(meetings.size() - 1);
        assertEquals("TC 70 Sandhausen 1", last.homeTeam());
        assertEquals("TC Weinheim 1902 2", last.awayTeam());
        assertEquals(LocalDateTime.of(2026, 7, 26, 9, 30), last.date());
        assertEquals(12710475L, last.meetingId());
    }

    @Test
    void parsesEveryMeetingWhenTheScheduleHasSpielortAndPlatzColumns() throws IOException {
        List<ScheduledMeetingDto> meetings = parser.parse(loadFixture("group-page-with-venue.html"));

        assertEquals(10, meetings.size());
        assertTrue(logs.messages(Level.WARN).isEmpty());
    }

    @Test
    void readsTheTeamsFromTheirHeaderColumnsWhenTheScheduleHasSpielortAndPlatzColumns() throws IOException {
        List<ScheduledMeetingDto> meetings = parser.parse(loadFixture("group-page-with-venue.html"));

        ScheduledMeetingDto first = meetings.get(0);
        assertEquals("Karlsruher ETV 3", first.homeTeam());
        assertEquals("TC Bühlertal 1", first.awayTeam());
        assertEquals(LocalDateTime.of(2026, 5, 10, 9, 30), first.date());
        assertEquals(12895844L, first.meetingId());

        // the only meeting with a venue filled in ("TC BW Gaggenau" in the Spielort column)
        ScheduledMeetingDto withVenue = meetings.get(6);
        assertEquals("TC Gernsbach 1", withVenue.homeTeam());
        assertEquals("KIT SC 2010 1", withVenue.awayTeam());
        assertEquals(LocalDateTime.of(2026, 7, 5, 9, 30), withVenue.date());
        assertEquals(12895987L, withVenue.meetingId());
    }

    @Test
    void warnsWhenRowsLinkAReportButNoneHasBothTeams() {
        String html = """
                <html><body>
                <h2>Spielplan</h2>
                <table cellspacing="0" class="result-set" border="0" cellpadding="0">
                  <tr>
                    <th colspan="3">Datum</th>
                    <th>Heimmannschaft</th>
                    <th>Gastmannschaft</th>
                    <th class="center">Spielbericht</th>
                  </tr>
                  <tr>
                    <td>So</td>
                    <td nowrap="nowrap"> 10.05.2026 09:30 </td>
                    <td nowrap="nowrap">&nbsp;</td>
                    <td nowrap="nowrap">&nbsp;</td>
                    <td nowrap="nowrap">&nbsp;</td>
                    <td class="center" nowrap="nowrap">
                      <a href="/cgi-bin/WebObjects/nuLigaTENDE.woa/wa/meetingReport?meeting=7&amp;federation=BAD&amp;championship=B1+S+2026">anzeigen</a>
                    </td>
                  </tr>
                </table>
                </body></html>
                """;

        List<ScheduledMeetingDto> meetings = parser.parse(html);

        assertTrue(meetings.isEmpty());
        assertEquals(
                List.of("Group page: 1 rows link a match report but none has both teams, 0 meetings"),
                logs.messages(Level.WARN));
    }

    @Test
    void ignoresByeRowsWithoutThrowing() {
        String html = """
                <html><body>
                <h2>Spielplan</h2>
                <table cellspacing="0" class="result-set" border="0" cellpadding="0">
                  <tr>
                    <th colspan="3">Datum</th>
                    <th>Heimmannschaft</th>
                    <th>Gastmannschaft</th>
                    <th class="center">Spielbericht</th>
                  </tr>
                  <tr>
                    <td>So</td>
                    <td nowrap="nowrap"> 10.05.2026 09:30 </td>
                    <td nowrap="nowrap">&nbsp;</td>
                    <td nowrap="nowrap">TC Musterstadt 1</td>
                    <td nowrap="nowrap">spielfrei</td>
                  </tr>
                  <tr>
                    <td class="tabelle-rowspan">&nbsp;</td>
                    <td class="tabelle-rowspan">&nbsp;</td>
                    <td class="tabelle-rowspan">&nbsp;</td>
                    <td nowrap="nowrap">TC Musterstadt 2</td>
                    <td nowrap="nowrap">TC Beispielhausen 1</td>
                    <td class="center" nowrap="nowrap">
                      <a href="/cgi-bin/WebObjects/nuLigaTENDE.woa/wa/meetingReport?meeting=99&amp;federation=BAD&amp;championship=B1+S+2026">anzeigen</a>
                    </td>
                  </tr>
                </table>
                </body></html>
                """;

        List<ScheduledMeetingDto> meetings = assertDoesNotThrow(() -> parser.parse(html));

        assertEquals(1, meetings.size());
        assertEquals("TC Musterstadt 2", meetings.get(0).homeTeam());
        assertEquals("TC Beispielhausen 1", meetings.get(0).awayTeam());
        assertEquals(99L, meetings.get(0).meetingId());
    }

    @Test
    void ignoresRowsWithMissingScoresWithoutThrowing() {
        String html = """
                <html><body>
                <h2>Spielplan</h2>
                <table cellspacing="0" class="result-set" border="0" cellpadding="0">
                  <tr>
                    <td>So</td>
                    <td nowrap="nowrap"> 10.05.2026 09:30 </td>
                    <td nowrap="nowrap">&nbsp;</td>
                    <td nowrap="nowrap">TC Musterstadt 1</td>
                    <td nowrap="nowrap">TC Beispielhausen 1</td>
                    <td class="center"></td>
                    <td class="center" nowrap="nowrap"></td>
                    <td class="center" nowrap="nowrap"></td>
                    <td class="center" nowrap="nowrap">
                      <a href="/cgi-bin/WebObjects/nuLigaTENDE.woa/wa/meetingReport?meeting=42&amp;federation=BAD&amp;championship=B1+S+2026">anzeigen</a>
                    </td>
                  </tr>
                </table>
                </body></html>
                """;

        List<ScheduledMeetingDto> meetings = assertDoesNotThrow(() -> parser.parse(html));

        assertEquals(1, meetings.size());
        assertEquals(42L, meetings.get(0).meetingId());
    }

    @Test
    void returnsEmptyListWhenNoSpielplanSectionIsPresent() {
        List<ScheduledMeetingDto> meetings = parser.parse("<html><body><p>no schedule here</p></body></html>");

        assertTrue(meetings.isEmpty());
    }
}
