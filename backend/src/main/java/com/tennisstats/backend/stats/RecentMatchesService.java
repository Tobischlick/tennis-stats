package com.tennisstats.backend.stats;

import com.tennisstats.backend.domain.Meeting;
import com.tennisstats.backend.domain.Player;
import com.tennisstats.backend.domain.Rubber;
import com.tennisstats.backend.domain.RubberParticipant;
import com.tennisstats.backend.domain.RubberSet;
import com.tennisstats.backend.domain.Side;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lists a player's most recent rubbers (singles and doubles) with opponents and set scores.
 *
 * <p>Selection and order deliberately mirror {@link StreakService}: newest {@link
 * Meeting#getPlayedOn()} first, rubbers whose sets don't settle a winner skipped, so the list lines
 * up with {@code streak.recentResults}.
 */
@Service
public class RecentMatchesService {

    private final RecentMatchRepository recentMatchRepository;

    public RecentMatchesService(RecentMatchRepository recentMatchRepository) {
        this.recentMatchRepository = recentMatchRepository;
    }

    @Transactional(readOnly = true)
    public List<MatchDetail> getRecentMatches(Long playerId, int limit) {
        List<RubberParticipant> participations = recentMatchRepository.findParticipationsNewestFirst(playerId);
        if (participations.isEmpty()) {
            return List.of();
        }
        recentMatchRepository.fetchSets(rubbersOf(participations));

        List<RubberParticipant> selected = new ArrayList<>();
        List<Side> winners = new ArrayList<>();
        for (RubberParticipant participation : participations) {
            if (selected.size() == limit) {
                break;
            }
            Side winner = RubberOutcome.winnerOf(participation.getRubber());
            if (winner != null) {
                selected.add(participation);
                winners.add(winner);
            }
        }
        if (selected.isEmpty()) {
            return List.of();
        }
        recentMatchRepository.fetchParticipants(rubbersOf(selected));

        List<MatchDetail> matches = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++) {
            matches.add(toMatchDetail(selected.get(i), winners.get(i)));
        }
        return List.copyOf(matches);
    }

    private static List<Rubber> rubbersOf(List<RubberParticipant> participations) {
        return participations.stream().map(RubberParticipant::getRubber).toList();
    }

    static MatchDetail toMatchDetail(RubberParticipant participation, Side winner) {
        Rubber rubber = participation.getRubber();
        Meeting meeting = rubber.getMeeting();
        Side side = participation.getSide();
        List<RubberParticipant> others = rubber.getParticipants().stream()
                .filter(other -> !other.equals(participation))
                .sorted(Comparator.comparing(RubberParticipant::getId))
                .toList();
        List<Player> opponents = others.stream()
                .filter(other -> other.getSide() != side)
                .map(RubberParticipant::getPlayer)
                .toList();
        Player partner = others.stream()
                .filter(other -> other.getSide() == side)
                .map(RubberParticipant::getPlayer)
                .findFirst()
                .orElse(null);
        List<SetScore> sets = rubber.getSets().stream()
                .sorted(Comparator.comparing(RubberSet::getSetNumber))
                .map(set -> SetScore.of(set, side))
                .toList();
        boolean home = side == Side.HOME;
        return new MatchDetail(
                meeting.getPlayedOn(),
                rubber.getType(),
                winner == side ? MatchResult.WIN : MatchResult.LOSS,
                opponents,
                partner,
                (home ? meeting.getHomeTeam() : meeting.getAwayTeam()).getName(),
                (home ? meeting.getAwayTeam() : meeting.getHomeTeam()).getName(),
                sets);
    }
}
