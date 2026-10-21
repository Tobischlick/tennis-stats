package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.Rubber;
import com.tennisstats.backend.domain.RubberParticipant;
import com.tennisstats.backend.domain.Side;
import com.tennisstats.backend.domain.repository.RubberParticipantRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Computes a player's deciding-set record: of the rubbers that went to a third set, whether a
 * regular set or a match tie-break, how many they won. Counts the same rubbers {@link
 * WinLossService} does, so the two numbers stay consistent.
 */
@Service
public class DecidingSetService {

    private static final int DECIDING_SET_NUMBER = 3;

    private final RubberParticipantRepository rubberParticipantRepository;

    public DecidingSetService(RubberParticipantRepository rubberParticipantRepository) {
        this.rubberParticipantRepository = rubberParticipantRepository;
    }

    @Transactional(readOnly = true)
    public DecidingSetStats getDecidingSetStats(Long playerId) {
        List<RubberParticipant> participations = rubberParticipantRepository.findAll().stream()
                .filter(participant -> participant.getPlayer().getId().equals(playerId))
                .filter(participant -> finishedSets(participant.getRubber()) >= DECIDING_SET_NUMBER)
                .toList();

        long played = 0;
        long won = 0;
        for (RubberParticipant participation : participations) {
            Side winner = RubberOutcome.winnerOf(participation.getRubber());
            if (winner == null) {
                continue;
            }
            played++;
            if (winner == participation.getSide()) {
                won++;
            }
        }

        if (played == 0) {
            return DecidingSetStats.noData();
        }
        return new DecidingSetStats(played, won, (double) won / played);
    }

    private static long finishedSets(Rubber rubber) {
        return rubber.getSets().stream().filter(set -> !set.isUnfinished()).count();
    }
}
