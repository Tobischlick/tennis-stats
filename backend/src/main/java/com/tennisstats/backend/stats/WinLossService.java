package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.RubberParticipant;
import com.tennisstats.backend.domain.Side;
import com.tennisstats.backend.domain.repository.RubberParticipantRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Computes a player's overall win/loss record across all rubbers (singles and doubles) they've
 * played, deriving the winner of each rubber from its {@link RubberSet} scores since no entity
 * stores that directly.
 */
@Service
public class WinLossService {

    private final RubberParticipantRepository rubberParticipantRepository;

    public WinLossService(RubberParticipantRepository rubberParticipantRepository) {
        this.rubberParticipantRepository = rubberParticipantRepository;
    }

    @Transactional(readOnly = true)
    public WinLossRecord getWinLossRecord(Long playerId) {
        List<RubberParticipant> participations = rubberParticipantRepository.findAll().stream()
                .filter(participant -> participant.getPlayer().getId().equals(playerId))
                .toList();

        if (participations.isEmpty()) {
            return WinLossRecord.noData(playerId);
        }

        int wins = 0;
        int losses = 0;
        for (RubberParticipant participation : participations) {
            Side winner = RubberOutcome.winnerOf(participation.getRubber());
            if (winner == null) {
                continue;
            }
            if (winner == participation.getSide()) {
                wins++;
            } else {
                losses++;
            }
        }

        return WinLossRecord.of(playerId, wins, losses);
    }
}
