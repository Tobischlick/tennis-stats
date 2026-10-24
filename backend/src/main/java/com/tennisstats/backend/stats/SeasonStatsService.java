package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.RubberParticipant;
import com.tennisstats.backend.domain.Side;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Splits a player's win/loss and tie-break record by {@link Season}, derived from each meeting's
 * {@link com.tennisstats.backend.domain.Meeting#getPlayedOn() playedOn}. Wins and losses use {@link
 * RubberOutcome} over the same rubbers as {@link WinLossService}, tie-breaks the same sets and rule
 * as {@link TieBreakService}, so the seasons add up to the overall numbers.
 */
@Service
public class SeasonStatsService {

    private final RecentMatchRepository recentMatchRepository;
    private final TieBreakSetRepository tieBreakSetRepository;

    public SeasonStatsService(RecentMatchRepository recentMatchRepository, TieBreakSetRepository tieBreakSetRepository) {
        this.recentMatchRepository = recentMatchRepository;
        this.tieBreakSetRepository = tieBreakSetRepository;
    }

    /**
     * Oldest season first; only seasons with a decided rubber or a tie-break of the player, empty if
     * there are none.
     */
    @Transactional(readOnly = true)
    public List<SeasonStats> getSeasonStats(Long playerId) {
        Map<Season, int[]> winsAndLosses = new TreeMap<>();
        List<RubberParticipant> participations = recentMatchRepository.findParticipationsNewestFirst(playerId);
        if (!participations.isEmpty()) {
            recentMatchRepository.fetchSets(participations.stream().map(RubberParticipant::getRubber).toList());
        }
        for (RubberParticipant participation : participations) {
            Side winner = RubberOutcome.winnerOf(participation.getRubber());
            if (winner == null) {
                continue;
            }
            Season season = Season.of(participation.getRubber().getMeeting().getPlayedOn());
            winsAndLosses.computeIfAbsent(season, s -> new int[2])[winner == participation.getSide() ? 0 : 1]++;
        }

        Map<Season, List<TieBreakSetRow>> tieBreaks = new TreeMap<>();
        for (TieBreakSetRow row : tieBreakSetRepository.findTieBreakSetRowsForPlayer(playerId)) {
            tieBreaks.computeIfAbsent(Season.of(row.playedOn()), s -> new ArrayList<>()).add(row);
        }

        // A season can have tie-breaks but no decided rubber (all its rubbers' sets were level), so it's listed too.
        SortedSet<Season> seasons = new TreeSet<>(winsAndLosses.keySet());
        seasons.addAll(tieBreaks.keySet());
        List<SeasonStats> result = new ArrayList<>();
        for (Season season : seasons) {
            int[] counts = winsAndLosses.getOrDefault(season, new int[2]);
            result.add(new SeasonStats(
                    season,
                    WinLossRecord.of(playerId, counts[0], counts[1]),
                    TieBreakService.statsOf(tieBreaks.getOrDefault(season, List.of()))));
        }
        return List.copyOf(result);
    }
}
