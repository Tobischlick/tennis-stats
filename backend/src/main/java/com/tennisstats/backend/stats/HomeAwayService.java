package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.RubberParticipant;
import com.tennisstats.backend.domain.Side;
import com.tennisstats.backend.domain.repository.RubberParticipantRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Splits a player's win/loss record into home and away rubbers. A participant's {@link Side} is
 * relative to the meeting's home team, so {@code HOME} means the player's team hosted. Counts the
 * same rubbers as {@link WinLossService} (singles and doubles, only those with a winner).
 */
@Service
public class HomeAwayService {

    private final RubberParticipantRepository rubberParticipantRepository;

    public HomeAwayService(RubberParticipantRepository rubberParticipantRepository) {
        this.rubberParticipantRepository = rubberParticipantRepository;
    }

    @Transactional(readOnly = true)
    public HomeAwayStats getHomeAwayStats(Long playerId) {
        List<RubberParticipant> participations = rubberParticipantRepository.findAll().stream()
                .filter(participant -> participant.getPlayer().getId().equals(playerId))
                .toList();

        int homeWins = 0;
        int homeLosses = 0;
        int awayWins = 0;
        int awayLosses = 0;
        for (RubberParticipant participation : participations) {
            Side winner = RubberOutcome.winnerOf(participation.getRubber());
            if (winner == null) {
                continue;
            }
            boolean won = winner == participation.getSide();
            if (participation.getSide() == Side.HOME) {
                if (won) {
                    homeWins++;
                } else {
                    homeLosses++;
                }
            } else if (won) {
                awayWins++;
            } else {
                awayLosses++;
            }
        }

        return new HomeAwayStats(homeWins, homeLosses, awayWins, awayLosses);
    }
}
