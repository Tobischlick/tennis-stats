package com.tennisstats.backend.api;

import com.tennisstats.backend.domain.Player;

/** Lightweight player identity for search lists and stats headers, per the {@code PlayerSummary} schema in api/openapi.yaml. */
public record PlayerSummary(String id, String name, String club) {

    public static PlayerSummary from(Player player, String club) {
        String name = player.getFirstName() + " " + player.getLastName();
        return new PlayerSummary(String.valueOf(player.getId()), name, club);
    }
}
