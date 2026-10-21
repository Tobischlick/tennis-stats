package com.tennisstats.backend.api;

import com.tennisstats.backend.domain.Player;
import com.tennisstats.backend.stats.MatchDetail;
import com.tennisstats.backend.stats.MatchResult;
import com.tennisstats.backend.stats.SetScore;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Matches the {@code DoublesMeeting} schema in api/openapi.yaml. */
public record DoublesMeeting(
        LocalDate playedOn,
        MatchResult result,
        PlayerSummary partner,
        PlayerSummary opponentPartner,
        String ownTeam,
        String opponentTeam,
        List<SetScore> sets) {

    static DoublesMeeting from(MatchDetail match, Long opponentId, Map<Long, String> clubs) {
        Player opponentPartner = match.opponents().stream()
                .filter(player -> !player.getId().equals(opponentId))
                .findFirst()
                .orElse(null);
        return new DoublesMeeting(
                match.playedOn(),
                match.result(),
                summaryOf(match.partner(), clubs),
                summaryOf(opponentPartner, clubs),
                match.ownTeam(),
                match.opponentTeam(),
                match.sets());
    }

    private static PlayerSummary summaryOf(Player player, Map<Long, String> clubs) {
        return player == null ? null : PlayerSummary.from(player, clubs.get(player.getId()));
    }
}
