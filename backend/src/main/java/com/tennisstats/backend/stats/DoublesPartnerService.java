package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.Player;
import com.tennisstats.backend.domain.Rubber;
import com.tennisstats.backend.domain.RubberParticipant;
import com.tennisstats.backend.domain.RubberType;
import com.tennisstats.backend.domain.Side;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Computes a player's record with each doubles partner, i.e. the other participant on the same side
 * of a {@link RubberType#DOUBLES} rubber. Wins and losses are counted like in {@link
 * WinLossService}: a rubber whose winner isn't known (no retirement and the sets don't settle it)
 * counts towards neither, and a partner with only such rubbers is left out.
 */
@Service
public class DoublesPartnerService {

    // Most matches first, then the best win percentage; the name only keeps ties in a stable order.
    private static final Comparator<DoublesPartnerRecord> MOST_MATCHES_FIRST = Comparator.comparingInt(
                    DoublesPartnerRecord::matchesPlayed)
            .thenComparingDouble(DoublesPartnerRecord::winPercentage)
            .reversed()
            .thenComparing(record -> record.partner().getLastName())
            .thenComparing(record -> record.partner().getFirstName())
            .thenComparing(record -> record.partner().getId());

    private final RecentMatchRepository recentMatchRepository;

    public DoublesPartnerService(RecentMatchRepository recentMatchRepository) {
        this.recentMatchRepository = recentMatchRepository;
    }

    /** Partners sorted by matches played, then win percentage; empty if the player has no settled doubles. */
    @Transactional(readOnly = true)
    public List<DoublesPartnerRecord> getDoublesPartners(Long playerId) {
        List<RubberParticipant> participations = recentMatchRepository.findParticipationsNewestFirst(playerId).stream()
                .filter(participation -> participation.getRubber().getType() == RubberType.DOUBLES)
                .toList();
        if (participations.isEmpty()) {
            return List.of();
        }
        List<Rubber> rubbers = participations.stream().map(RubberParticipant::getRubber).toList();
        recentMatchRepository.fetchParticipants(rubbers);
        recentMatchRepository.fetchSets(rubbers);

        Map<Long, Player> partners = new LinkedHashMap<>();
        Map<Long, int[]> winsAndLosses = new LinkedHashMap<>();
        for (RubberParticipant participation : participations) {
            Rubber rubber = participation.getRubber();
            Side side = participation.getSide();
            Side winner = RubberOutcome.winnerOf(rubber);
            Player partner = partnerOf(rubber, playerId, side);
            if (winner == null || partner == null) {
                continue;
            }
            partners.putIfAbsent(partner.getId(), partner);
            int[] record = winsAndLosses.computeIfAbsent(partner.getId(), id -> new int[2]);
            record[winner == side ? 0 : 1]++;
        }

        return partners.values().stream()
                .map(partner -> {
                    int[] record = winsAndLosses.get(partner.getId());
                    return new DoublesPartnerRecord(partner, record[0], record[1]);
                })
                .sorted(MOST_MATCHES_FIRST)
                .toList();
    }

    private static Player partnerOf(Rubber rubber, Long playerId, Side side) {
        return rubber.getParticipants().stream()
                .filter(participant -> participant.getSide() == side)
                .map(RubberParticipant::getPlayer)
                .filter(player -> !player.getId().equals(playerId))
                .findFirst()
                .orElse(null);
    }
}
