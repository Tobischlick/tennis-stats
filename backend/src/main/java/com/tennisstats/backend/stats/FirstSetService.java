package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.Rubber;
import com.tennisstats.backend.domain.RubberParticipant;
import com.tennisstats.backend.domain.RubberSet;
import com.tennisstats.backend.domain.Side;
import com.tennisstats.backend.domain.repository.RubberParticipantRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Computes how a player's rubbers end after the first set: comebacks after losing it and blown
 * leads after winning it. Counts singles and doubles like {@link WinLossService}, but leaves out
 * rubbers decided by a retirement, since those say nothing about turning a match around.
 */
@Service
public class FirstSetService {

    private static final int FIRST_SET_NUMBER = 1;

    private final RubberParticipantRepository rubberParticipantRepository;

    public FirstSetService(RubberParticipantRepository rubberParticipantRepository) {
        this.rubberParticipantRepository = rubberParticipantRepository;
    }

    @Transactional(readOnly = true)
    public FirstSetStats getFirstSetStats(Long playerId) {
        List<RubberParticipant> participations = rubberParticipantRepository.findAll().stream()
                .filter(participant -> participant.getPlayer().getId().equals(playerId))
                .filter(participant -> participant.getRubber().getRetiredSide() == null)
                .toList();

        long lostFirstSet = 0;
        long comebacks = 0;
        long wonFirstSet = 0;
        long blownLeads = 0;
        for (RubberParticipant participation : participations) {
            Side firstSetWinner = firstSetWinnerOf(participation.getRubber());
            Side winner = RubberOutcome.winnerOf(participation.getRubber());
            if (firstSetWinner == null || winner == null) {
                continue;
            }
            boolean wonRubber = winner == participation.getSide();
            if (firstSetWinner == participation.getSide()) {
                wonFirstSet++;
                if (!wonRubber) {
                    blownLeads++;
                }
            } else {
                lostFirstSet++;
                if (wonRubber) {
                    comebacks++;
                }
            }
        }

        return FirstSetStats.of(lostFirstSet, comebacks, wonFirstSet, blownLeads);
    }

    private static Side firstSetWinnerOf(Rubber rubber) {
        RubberSet first = rubber.getSets().stream()
                .filter(set -> set.getSetNumber() == FIRST_SET_NUMBER && !set.isUnfinished())
                .findFirst()
                .orElse(null);
        if (first == null || first.getHomeScore().equals(first.getAwayScore())) {
            return null;
        }
        return first.getHomeScore() > first.getAwayScore() ? Side.HOME : Side.AWAY;
    }
}
