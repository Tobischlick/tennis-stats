package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.Rubber;
import com.tennisstats.backend.domain.RubberParticipant;
import com.tennisstats.backend.domain.RubberType;
import com.tennisstats.backend.domain.Side;
import com.tennisstats.backend.domain.repository.RubberParticipantRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Computes the head-to-head record between two players.
 *
 * <p>Scope decision: only {@link RubberType#SINGLES} rubbers count, and only when the two players
 * are on opposite sides of that same rubber (one HOME, one AWAY) — a clean, unambiguous 1-on-1. A
 * doubles rubber where both players happen to be on opposite sides is deliberately excluded: each
 * would be playing alongside a different, unrelated partner, so the outcome reflects two pairs
 * rather than a meaningful encounter between just these two players. This matches how players
 * actually talk about "our head-to-head".
 */
@Service
public class HeadToHeadService {

    private final RubberParticipantRepository rubberParticipantRepository;

    public HeadToHeadService(RubberParticipantRepository rubberParticipantRepository) {
        this.rubberParticipantRepository = rubberParticipantRepository;
    }

    @Transactional(readOnly = true)
    public HeadToHeadRecord getHeadToHead(Long playerAId, Long playerBId) {
        List<Rubber> singlesRubbers = rubberParticipantRepository.findAll().stream()
                .filter(participant -> participant.getPlayer().getId().equals(playerAId))
                .map(RubberParticipant::getRubber)
                .filter(rubber -> rubber.getType() == RubberType.SINGLES)
                .distinct()
                .sorted(Comparator.comparing((Rubber rubber) -> rubber.getMeeting().getPlayedOn())
                        .reversed())
                .toList();

        int matchesPlayed = 0;
        int playerAWins = 0;
        int playerBWins = 0;
        List<MatchResult> recentResults = new ArrayList<>();

        for (Rubber rubber : singlesRubbers) {
            Map<Long, Side> sideByPlayerId = rubber.getParticipants().stream()
                    .collect(Collectors.toMap(p -> p.getPlayer().getId(), RubberParticipant::getSide));

            Side sideA = sideByPlayerId.get(playerAId);
            Side sideB = sideByPlayerId.get(playerBId);
            if (sideA == null || sideB == null || sideA == sideB) {
                continue;
            }

            matchesPlayed++;
            Side winner = RubberOutcome.winnerOf(rubber);
            if (winner == null) {
                continue;
            }
            if (winner == sideA) {
                playerAWins++;
                recentResults.add(MatchResult.WIN);
            } else {
                playerBWins++;
                recentResults.add(MatchResult.LOSS);
            }
        }

        if (matchesPlayed == 0) {
            return HeadToHeadRecord.noMatches(playerAId, playerBId);
        }

        return HeadToHeadRecord.of(playerAId, playerBId, matchesPlayed, playerAWins, playerBWins, List.copyOf(recentResults));
    }
}
