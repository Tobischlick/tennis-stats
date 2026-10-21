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
 * Lists a player's rubbers (singles and doubles) with opponents and set scores, one page at a time.
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
    public MatchPage getMatches(Long playerId, int offset, int limit) {
        List<RubberParticipant> participations = recentMatchRepository.findParticipationsNewestFirst(playerId);
        if (participations.isEmpty()) {
            return new MatchPage(List.of(), 0);
        }
        recentMatchRepository.fetchSets(rubbersOf(participations));

        List<RubberParticipant> settled = new ArrayList<>();
        List<Side> winners = new ArrayList<>();
        for (RubberParticipant participation : participations) {
            Side winner = RubberOutcome.winnerOf(participation.getRubber());
            if (winner != null) {
                settled.add(participation);
                winners.add(winner);
            }
        }
        int from = Math.min(offset, settled.size());
        int to = Math.min(from + limit, settled.size());
        if (from == to) {
            return new MatchPage(List.of(), settled.size());
        }
        List<RubberParticipant> selected = settled.subList(from, to);
        recentMatchRepository.fetchParticipants(rubbersOf(selected));

        List<MatchDetail> matches = new ArrayList<>();
        for (int i = from; i < to; i++) {
            matches.add(toMatchDetail(settled.get(i), winners.get(i)));
        }
        return new MatchPage(List.copyOf(matches), settled.size());
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
