package com.tennisstats.backend.crawler;

record CrawlSummary(
        int groupsAttempted, int groupsFailed, int meetingsCrawled, int meetingsSkipped, int meetingsFailed) {

    boolean nothingCouldBeCrawled() {
        return groupsAttempted > 0 && groupsFailed == groupsAttempted;
    }
}
