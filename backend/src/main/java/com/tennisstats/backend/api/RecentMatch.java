package com.tennisstats.backend.api;

import com.tennisstats.backend.domain.RubberType;
import com.tennisstats.backend.stats.MatchDetail;
import com.tennisstats.backend.stats.MatchResult;
import com.tennisstats.backend.stats.SetScore;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Matches the {@code RecentMatch} schema in api/openapi.yaml. */
public record RecentMatch(
        LocalDate playedOn,
        RubberType type,
        MatchResult result,
        List<PlayerSummary> opponents,
        PlayerSummary partner,
        String ownTeam,
        String opponentTeam,
        List<SetScore> sets) {

    public static RecentMatch from(MatchDetail match, Map<Long, String> clubs) {
        return new RecentMatch(
                match.playedOn(),
                match.type(),
                match.result(),
                match.opponents().stream()
                        .map(opponent -> PlayerSummary.from(opponent, clubs.get(opponent.getId())))
                        .toList(),
                match.partner() == null
                        ? null
                        : PlayerSummary.from(match.partner(), clubs.get(match.partner().getId())),
                match.ownTeam(),
                match.opponentTeam(),
                match.sets());
    }
}
