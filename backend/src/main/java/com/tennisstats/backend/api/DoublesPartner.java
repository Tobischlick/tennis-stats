package com.tennisstats.backend.api;

import com.tennisstats.backend.stats.DoublesPartnerRecord;
import java.util.Map;

/** Matches the {@code DoublesPartner} schema in api/openapi.yaml. */
public record DoublesPartner(PlayerSummary partner, int matchesPlayed, WinLoss record) {

    static DoublesPartner from(DoublesPartnerRecord record, Map<Long, String> clubs) {
        return new DoublesPartner(
                PlayerSummary.from(record.partner(), clubs.get(record.partner().getId())),
                record.matchesPlayed(),
                WinLoss.of(record.wins(), record.losses()));
    }
}
