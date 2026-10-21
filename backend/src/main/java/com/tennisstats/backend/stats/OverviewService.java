package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.Rubber;
import com.tennisstats.backend.domain.RubberSet;
import com.tennisstats.backend.domain.RubberType;
import com.tennisstats.backend.domain.Side;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Computes the database overview: totals of what's stored plus a few fun stats.
 *
 * <p>Walkovers and retirements (a rubber with a {@code retiredSide}) count as matches, but not as
 * completed ones: they're left out of the deciding-set share and the most common score line, and
 * their sets cut short ({@code unfinished}) count as neither sets nor tie-breaks. They do count
 * towards win streaks, like {@link StreakService} counts them on the player page.
 */
@Service
public class OverviewService {

    private static final int DECIDING_SET_NUMBER = 3;

    private static final Comparator<Rubber> PLAYED_ORDER = Comparator.comparing(
                    (Rubber rubber) -> rubber.getMeeting().getPlayedOn())
            .thenComparing(rubber -> rubber.getMeeting().getId())
            .thenComparing(Rubber::getPosition);

    private final OverviewRepository overviewRepository;

    public OverviewService(OverviewRepository overviewRepository) {
        this.overviewRepository = overviewRepository;
    }

    @Transactional(readOnly = true)
    public DatabaseOverview getOverview() {
        List<String> teamNames = overviewRepository.findTeamNames();
        long clubs = teamNames.stream()
                .map(PlayerClubService::clubName)
                .distinct()
                .count();

        List<Rubber> rubbers = overviewRepository.findRubbersWithSets().stream()
                .sorted(PLAYED_ORDER)
                .toList();
        Map<Long, Side> winners = new HashMap<>();
        Map<String, Long> scoreLines = new HashMap<>();
        long completedMatches = 0;
        long decidingSetMatches = 0;
        for (Rubber rubber : rubbers) {
            Side winner = RubberOutcome.winnerOf(rubber);
            if (winner == null) {
                continue;
            }
            winners.put(rubber.getId(), winner);
            if (rubber.getRetiredSide() != null) {
                continue;
            }
            completedMatches++;
            if (rubber.getSets().size() >= DECIDING_SET_NUMBER) {
                decidingSetMatches++;
            }
            scoreLines.merge(scoreLine(rubber, winner), 1L, Long::sum);
        }

        return new DatabaseOverview(
                overviewRepository.countPlayers(),
                clubs,
                teamNames.size(),
                overviewRepository.countMeetings(),
                overviewRepository.countRubbers(RubberType.SINGLES),
                overviewRepository.countRubbers(RubberType.DOUBLES),
                overviewRepository.countFinishedSets(),
                overviewRepository.findFirstPlayedOn(),
                overviewRepository.findLastPlayedOn(),
                overviewRepository.countTieBreaks(),
                overviewRepository.countMatchTieBreaks(),
                completedMatches,
                decidingSetMatches,
                mostCommon(scoreLines),
                longestWinStreak(rubbers, winners));
    }

    private static String scoreLine(Rubber rubber, Side winner) {
        return rubber.getSets().stream()
                .sorted(Comparator.comparing(RubberSet::getSetNumber))
                .map(set -> winner == Side.HOME
                        ? set.getHomeScore() + ":" + set.getAwayScore()
                        : set.getAwayScore() + ":" + set.getHomeScore())
                .collect(Collectors.joining(" "));
    }

    private static ScoreLineCount mostCommon(Map<String, Long> scoreLines) {
        return scoreLines.entrySet().stream()
                .min(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .map(entry -> new ScoreLineCount(entry.getKey(), entry.getValue()))
                .orElse(null);
    }

    private int longestWinStreak(List<Rubber> rubbersInPlayedOrder, Map<Long, Side> winners) {
        Map<Long, Integer> order = new HashMap<>();
        for (int i = 0; i < rubbersInPlayedOrder.size(); i++) {
            order.put(rubbersInPlayedOrder.get(i).getId(), i);
        }
        Map<Long, List<ParticipationRow>> byPlayer = overviewRepository.findParticipations().stream()
                .filter(row -> winners.containsKey(row.rubberId()))
                .collect(Collectors.groupingBy(ParticipationRow::playerId, Collectors.toCollection(ArrayList::new)));

        int longest = 0;
        for (List<ParticipationRow> participations : byPlayer.values()) {
            participations.sort(Comparator.comparing(row -> order.get(row.rubberId())));
            int current = 0;
            for (ParticipationRow row : participations) {
                current = winners.get(row.rubberId()) == row.side() ? current + 1 : 0;
                longest = Math.max(longest, current);
            }
        }
        return longest;
    }
}
