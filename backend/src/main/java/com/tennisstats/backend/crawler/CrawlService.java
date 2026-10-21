package com.tennisstats.backend.crawler;

import com.tennisstats.backend.domain.repository.MeetingRepository;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Crawls every configured nuLiga group once, plus every group linked from the overview page of each
 * configured championship (a group listed both ways is crawled once): fetches the group page, then the match report of each
 * played meeting that has no stored result yet, and hands it to {@link MeetingPersistenceService}.
 * Deliberately not {@code @Transactional}: each meeting commits on its own inside {@code persist},
 * so a failure halfway through a run keeps everything crawled before it. A {@link
 * CrawlFailedException} (nuLiga answered 429 or 403) is never isolated like other failures: it ends
 * the whole run.
 */
class CrawlService {

    private static final Logger log = LoggerFactory.getLogger(CrawlService.class);

    private final PageFetcher pageFetcher;
    private final MeetingPersistenceService persistenceService;
    private final MeetingRepository meetingRepository;
    private final CrawlerProperties properties;
    private final GroupPageParser groupPageParser = new GroupPageParser();
    private final LeaguePageParser leaguePageParser = new LeaguePageParser();

    CrawlService(
            PageFetcher pageFetcher,
            MeetingPersistenceService persistenceService,
            MeetingRepository meetingRepository,
            CrawlerProperties properties) {
        this.pageFetcher = pageFetcher;
        this.persistenceService = persistenceService;
        this.meetingRepository = meetingRepository;
        this.properties = properties;
    }

    CrawlSummary crawl() {
        Set<String> alreadyComplete = meetingRepository.findExternalIdsWithResults();
        LocalDate today = LocalDate.now();

        int groupsAttempted = 0;
        int groupsFailed = 0;
        int crawled = 0;
        int skipped = 0;
        int failed = 0;

        LinkedHashSet<CrawlerProperties.Group> groups = new LinkedHashSet<>(properties.groups());
        for (String championship : properties.championships()) {
            List<CrawlerProperties.Group> discovered;
            try {
                discovered = leaguePageParser.parse(pageFetcher.fetch(leaguePageUrl(championship)), championship);
            } catch (CrawlFailedException e) {
                throw e;
            } catch (RuntimeException e) {
                groupsAttempted++;
                groupsFailed++;
                log.warn("Skipping championship {}: overview page failed", championship, e);
                continue;
            }
            // most likely a misspelled championship: count it as failed rather than silently crawling nothing
            if (discovered.isEmpty()) {
                groupsAttempted++;
                groupsFailed++;
                log.warn("Skipping championship {}: overview page links no groups", championship);
                continue;
            }
            log.info("Championship {}: {} groups found", championship, discovered.size());
            groups.addAll(discovered);
        }

        for (CrawlerProperties.Group group : groups) {
            groupsAttempted++;

            List<ScheduledMeetingDto> meetings;
            try {
                meetings = groupPageParser.parse(pageFetcher.fetch(groupPageUrl(group)));
            } catch (CrawlFailedException e) {
                throw e;
            } catch (RuntimeException e) {
                groupsFailed++;
                log.warn("Skipping group {} / {}: group page failed", group.championship(), group.group(), e);
                continue;
            }

            int groupCrawled = 0;
            int groupSkipped = 0;
            int groupFailed = 0;
            for (ScheduledMeetingDto meeting : meetings) {
                if (meeting.date() == null
                        || meeting.date().toLocalDate().isAfter(today)
                        || alreadyComplete.contains(String.valueOf(meeting.meetingId()))) {
                    groupSkipped++;
                    continue;
                }
                try {
                    String reportHtml = pageFetcher.fetch(reportUrl(group, meeting.meetingId()));
                    persistenceService.persist(meeting, MatchReportParser.parse(reportHtml));
                    groupCrawled++;
                } catch (CrawlFailedException e) {
                    throw e;
                } catch (RuntimeException e) {
                    groupFailed++;
                    log.warn("Meeting {} failed, continuing with the next one", meeting.meetingId(), e);
                }
            }

            log.info(
                    "Group {} / {}: {} crawled, {} skipped, {} failed",
                    group.championship(),
                    group.group(),
                    groupCrawled,
                    groupSkipped,
                    groupFailed);
            crawled += groupCrawled;
            skipped += groupSkipped;
            failed += groupFailed;
        }

        return new CrawlSummary(groupsAttempted, groupsFailed, crawled, skipped, failed);
    }

    private String leaguePageUrl(String championship) {
        return "%s/leaguePage?championship=%s".formatted(properties.baseUrl(), encode(championship));
    }

    private String groupPageUrl(CrawlerProperties.Group group) {
        return "%s/groupPage?championship=%s&group=%s"
                .formatted(properties.baseUrl(), encode(group.championship()), encode(group.group()));
    }

    private String reportUrl(CrawlerProperties.Group group, Long meetingId) {
        return "%s/meetingReport?meeting=%d&federation=%s&championship=%s"
                .formatted(
                        properties.baseUrl(),
                        meetingId,
                        encode(properties.federation()),
                        encode(group.championship()));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
