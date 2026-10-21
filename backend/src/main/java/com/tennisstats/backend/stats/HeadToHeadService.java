package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.Rubber;
import com.tennisstats.backend.domain.RubberParticipant;
import com.tennisstats.backend.domain.RubberType;
import com.tennisstats.backend.domain.Side;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Computes the head-to-head record between two players.
 *
 * <p>Scope decision: only rubbers where the two players are on opposite sides (one HOME, one AWAY)
 * count, and singles and doubles are kept as two separate records. The singles record is the clean,
 * unambiguous 1-on-1 players mean by "our head-to-head". A doubles result reflects two pairs rather
 * than just these two players, so mixing it into the singles record would distort it; it is still
 * a time the two met on court, though, so it is reported next to the singles record as its own
 * {@link HeadToHeadRecord.Doubles} record, with partners and set scores. Doubles where the two
 * played together as partners are not an encounter between them and aren't counted at all.
 */
@Service
public class HeadToHeadService {

    private final RecentMatchRepository recentMatchRepository;

    public HeadToHeadService(RecentMatchRepository recentMatchRepository) {
        this.recentMatchRepository = recentMatchRepository;
    }

    @Transactional(readOnly = true)
    public HeadToHeadRecord getHeadToHead(Long playerAId, Long playerBId) {
        List<RubberParticipant> participations = recentMatchRepository.findParticipationsNewestFirst(playerAId);
        if (participations.isEmpty()) {
            return HeadToHeadRecord.noMatches(playerAId, playerBId);
        }
        List<Rubber> rubbers = participations.stream().map(RubberParticipant::getRubber).toList();
        recentMatchRepository.fetchParticipants(rubbers);
        recentMatchRepository.fetchSets(rubbers);

        int singlesPlayed = 0;
        int singlesAWins = 0;
        int singlesBWins = 0;
        List<MatchResult> singlesResults = new ArrayList<>();
        int doublesPlayed = 0;
        int doublesAWins = 0;
        int doublesBWins = 0;
        List<MatchDetail> doublesMeetings = new ArrayList<>();

        for (RubberParticipant participation : participations) {
            Rubber rubber = participation.getRubber();
            Side sideA = participation.getSide();
            Side sideB = sideOf(rubber, playerBId);
            if (sideB == null || sideB == sideA) {
                continue;
            }

            Side winner = RubberOutcome.winnerOf(rubber);
            if (rubber.getType() == RubberType.SINGLES) {
                singlesPlayed++;
                if (winner == null) {
                    continue;
                }
                if (winner == sideA) {
                    singlesAWins++;
                    singlesResults.add(MatchResult.WIN);
                } else {
                    singlesBWins++;
                    singlesResults.add(MatchResult.LOSS);
                }
            } else {
                doublesPlayed++;
                if (winner == null) {
                    continue;
                }
                if (winner == sideA) {
                    doublesAWins++;
                } else {
                    doublesBWins++;
                }
                doublesMeetings.add(RecentMatchesService.toMatchDetail(participation, winner));
            }
        }

        HeadToHeadRecord.Doubles doubles = doublesPlayed == 0
                ? HeadToHeadRecord.Doubles.NONE
                : new HeadToHeadRecord.Doubles(doublesPlayed, doublesAWins, doublesBWins, List.copyOf(doublesMeetings));
        if (singlesPlayed == 0) {
            return HeadToHeadRecord.noSinglesMatches(playerAId, playerBId, doubles);
        }
        return HeadToHeadRecord.of(
                playerAId, playerBId, singlesPlayed, singlesAWins, singlesBWins, List.copyOf(singlesResults), doubles);
    }

    private static Side sideOf(Rubber rubber, Long playerId) {
        return rubber.getParticipants().stream()
                .filter(participant -> participant.getPlayer().getId().equals(playerId))
                .map(RubberParticipant::getSide)
                .findFirst()
                .orElse(null);
    }
}
