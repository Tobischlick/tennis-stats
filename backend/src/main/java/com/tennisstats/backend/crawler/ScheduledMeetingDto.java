package com.tennisstats.backend.crawler;

import java.time.LocalDateTime;

public record ScheduledMeetingDto(String homeTeam, String awayTeam, LocalDateTime date, Long meetingId) {
}
