package com.tennisstats.backend.api;

import com.tennisstats.backend.stats.DoublesPartnerRecord;
import java.util.List;
import java.util.Map;

/** Matches the {@code DoublesPartners} schema in api/openapi.yaml. */
public record DoublesPartners(
        String playerId, PlayerSummary player, int matchesPlayed, boolean hasEnoughData, List<DoublesPartner> partners) {

    public static DoublesPartners from(
            Long playerId, PlayerSummary player, List<DoublesPartnerRecord> partners, Map<Long, String> clubs) {
        int matchesPlayed = partners.stream().mapToInt(DoublesPartnerRecord::matchesPlayed).sum();
        String id = String.valueOf(playerId);
        if (matchesPlayed == 0) {
            return new DoublesPartners(id, player, 0, false, List.of());
        }
        return new DoublesPartners(
                id,
                player,
                matchesPlayed,
                true,
                partners.stream().map(partner -> DoublesPartner.from(partner, clubs)).toList());
    }
}
