package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.RubberParticipant;
import com.tennisstats.backend.domain.Side;
import com.tennisstats.backend.domain.repository.RubberParticipantRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Computes a player's current streak and recent-form results, ordering their rubbers by {@link
 * com.tennisstats.backend.domain.Meeting#getPlayedOn()} (most recent first) and deriving each
 * result from {@link RubberOutcome#winnerOf}.
 *
 * <p>A rubber whose sets don't settle a winner ({@code winnerOf} returns {@code null}) is simply
 * skipped in the ordered sequence, rather than counted as a result or allowed to break a streak
 * it didn't actually break.
 */
@Service
public class StreakService {

    private static final int RECENT_RESULTS_LIMIT = 5;

    private final RubberParticipantRepository rubberParticipantRepository;

    public StreakService(RubberParticipantRepository rubberParticipantRepository) {
        this.rubberParticipantRepository = rubberParticipantRepository;
    }

    @Transactional(readOnly = true)
    public StreakStats getStreakStats(Long playerId) {
        List<RubberParticipant> participations = rubberParticipantRepository.findAll().stream()
                .filter(participant -> participant.getPlayer().getId().equals(playerId))
                .sorted(Comparator.comparing(
                                (RubberParticipant participant) -> participant.getRubber().getMeeting().getPlayedOn())
                        .reversed())
                .toList();

        List<MatchResult> results = new ArrayList<>();
        for (RubberParticipant participation : participations) {
            Side winner = RubberOutcome.winnerOf(participation.getRubber());
            if (winner == null) {
                continue;
            }
            results.add(winner == participation.getSide() ? MatchResult.WIN : MatchResult.LOSS);
        }

        if (results.isEmpty()) {
            return StreakStats.noData(playerId);
        }

        MatchResult streakResult = results.get(0);
        int streakLength = 0;
        for (MatchResult result : results) {
            if (result != streakResult) {
                break;
            }
            streakLength++;
        }

        List<MatchResult> recentResults = List.copyOf(results.subList(0, Math.min(RECENT_RESULTS_LIMIT, results.size())));

        return StreakStats.of(playerId, streakResult, streakLength, recentResults);
    }
}
