package com.tennisstats.backend.stats;

import static org.assertj.core.api.Assertions.assertThat;

import com.tennisstats.backend.domain.Meeting;
import com.tennisstats.backend.domain.Player;
import com.tennisstats.backend.domain.Rubber;
import com.tennisstats.backend.domain.RubberType;
import com.tennisstats.backend.domain.SetType;
import com.tennisstats.backend.domain.Side;
import com.tennisstats.backend.domain.Team;
import com.tennisstats.backend.domain.repository.MeetingRepository;
import com.tennisstats.backend.domain.repository.PlayerRepository;
import com.tennisstats.backend.domain.repository.TeamRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

@DataJpaTest
@Import(HeadToHeadService.class)
@DirtiesContext
class HeadToHeadServiceTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private HeadToHeadService headToHeadService;

    @Test
    void countsSinglesWinsAndLossesBetweenTwoPlayers() {
        Team home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        Team away = teamRepository.save(new Team("TC Rot-Gelb 1"));

        Player anna = playerRepository.save(new Player("Anna", "Adler"));
        Player bea = playerRepository.save(new Player("Bea", "Berger"));
        Player unrelated = playerRepository.save(new Player("Clara", "Conrad"));

        // Anna beats Bea, playing HOME, 2 sets to 0.
        Meeting first = new Meeting(home, away, LocalDate.of(2026, 5, 3));
        Rubber firstRubber = first.addRubber(RubberType.SINGLES, 1);
        firstRubber.addParticipant(anna, Side.HOME);
        firstRubber.addParticipant(bea, Side.AWAY);
        firstRubber.addSet(1, SetType.REGULAR, 6, 3);
        firstRubber.addSet(2, SetType.REGULAR, 6, 4);
        meetingRepository.save(first);

        // Bea beats Anna in the rematch, playing HOME.
        Meeting second = new Meeting(away, home, LocalDate.of(2026, 5, 17));
        Rubber secondRubber = second.addRubber(RubberType.SINGLES, 1);
        secondRubber.addParticipant(bea, Side.HOME);
        secondRubber.addParticipant(anna, Side.AWAY);
        secondRubber.addSet(1, SetType.REGULAR, 6, 3);
        secondRubber.addSet(2, SetType.REGULAR, 4, 6);
        secondRubber.addSet(3, SetType.MATCH_TIE_BREAK, 10, 7);
        meetingRepository.save(second);

        // Anna beats Bea again, playing AWAY.
        Meeting third = new Meeting(away, home, LocalDate.of(2026, 6, 1));
        Rubber thirdRubber = third.addRubber(RubberType.SINGLES, 1);
        thirdRubber.addParticipant(bea, Side.HOME);
        thirdRubber.addParticipant(anna, Side.AWAY);
        thirdRubber.addSet(1, SetType.REGULAR, 3, 6);
        thirdRubber.addSet(2, SetType.REGULAR, 2, 6);
        meetingRepository.save(third);

        // A singles rubber against someone else shouldn't affect the head-to-head between Anna and Bea.
        Meeting unrelatedMeeting = new Meeting(home, away, LocalDate.of(2026, 6, 8));
        Rubber unrelatedRubber = unrelatedMeeting.addRubber(RubberType.SINGLES, 2);
        unrelatedRubber.addParticipant(anna, Side.HOME);
        unrelatedRubber.addParticipant(unrelated, Side.AWAY);
        unrelatedRubber.addSet(1, SetType.REGULAR, 6, 1);
        unrelatedRubber.addSet(2, SetType.REGULAR, 6, 2);
        meetingRepository.saveAndFlush(unrelatedMeeting);

        HeadToHeadRecord record = headToHeadService.getHeadToHead(anna.getId(), bea.getId());

        assertThat(record.hasMatches()).isTrue();
        assertThat(record.matchesPlayed()).isEqualTo(3);
        assertThat(record.playerAWins()).isEqualTo(2);
        assertThat(record.playerBWins()).isEqualTo(1);
    }

    @Test
    void returnsNoMatchesForTwoPlayersWhoNeverPlayedEachOther() {
        Player anna = playerRepository.save(new Player("Anna", "Adler"));
        Player bea = playerRepository.saveAndFlush(new Player("Bea", "Berger"));

        HeadToHeadRecord record = headToHeadService.getHeadToHead(anna.getId(), bea.getId());

        assertThat(record.hasMatches()).isFalse();
        assertThat(record.matchesPlayed()).isZero();
        assertThat(record.playerAWins()).isZero();
        assertThat(record.playerBWins()).isZero();
    }

    @Test
    void doesNotCountADoublesEncounterBetweenTheTwoPlayers() {
        Team home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        Team away = teamRepository.save(new Team("TC Rot-Gelb 1"));

        Player anna = playerRepository.save(new Player("Anna", "Adler"));
        Player bea = playerRepository.save(new Player("Bea", "Berger"));
        Player annasPartner = playerRepository.save(new Player("Clara", "Conrad"));
        Player beasPartner = playerRepository.save(new Player("Dana", "Diehl"));

        // Anna and Bea face each other in a doubles rubber, each with their own partner.
        Meeting doublesMeeting = new Meeting(home, away, LocalDate.of(2026, 5, 3));
        Rubber doublesRubber = doublesMeeting.addRubber(RubberType.DOUBLES, 2);
        doublesRubber.addParticipant(anna, Side.HOME);
        doublesRubber.addParticipant(annasPartner, Side.HOME);
        doublesRubber.addParticipant(bea, Side.AWAY);
        doublesRubber.addParticipant(beasPartner, Side.AWAY);
        doublesRubber.addSet(1, SetType.REGULAR, 6, 2);
        doublesRubber.addSet(2, SetType.REGULAR, 6, 1);
        meetingRepository.saveAndFlush(doublesMeeting);

        HeadToHeadRecord record = headToHeadService.getHeadToHead(anna.getId(), bea.getId());

        assertThat(record.hasMatches()).isFalse();
        assertThat(record.matchesPlayed()).isZero();
    }

    @Test
    void recentResultsAreNewestFirstFromPlayerAsPerspective() {
        Team home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        Team away = teamRepository.save(new Team("TC Rot-Gelb 1"));
        Player anna = playerRepository.save(new Player("Anna", "Adler"));
        Player bea = playerRepository.save(new Player("Bea", "Berger"));

        // Anna beats Bea first, then loses the rematch two weeks later.
        Meeting older = new Meeting(home, away, LocalDate.of(2026, 5, 3));
        Rubber olderRubber = older.addRubber(RubberType.SINGLES, 1);
        olderRubber.addParticipant(anna, Side.HOME);
        olderRubber.addParticipant(bea, Side.AWAY);
        olderRubber.addSet(1, SetType.REGULAR, 6, 3);
        olderRubber.addSet(2, SetType.REGULAR, 6, 4);
        meetingRepository.save(older);

        Meeting newer = new Meeting(home, away, LocalDate.of(2026, 5, 17));
        Rubber newerRubber = newer.addRubber(RubberType.SINGLES, 1);
        newerRubber.addParticipant(anna, Side.HOME);
        newerRubber.addParticipant(bea, Side.AWAY);
        newerRubber.addSet(1, SetType.REGULAR, 3, 6);
        newerRubber.addSet(2, SetType.REGULAR, 4, 6);
        meetingRepository.saveAndFlush(newer);

        HeadToHeadRecord record = headToHeadService.getHeadToHead(anna.getId(), bea.getId());

        assertThat(record.recentResults()).containsExactly(MatchResult.LOSS, MatchResult.WIN);
    }

    @Test
    void recentResultsSkipRubbersThatDidNotSettleAWinner() {
        Team home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        Team away = teamRepository.save(new Team("TC Rot-Gelb 1"));
        Player anna = playerRepository.save(new Player("Anna", "Adler"));
        Player bea = playerRepository.save(new Player("Bea", "Berger"));

        Meeting settled = new Meeting(home, away, LocalDate.of(2026, 5, 3));
        Rubber settledRubber = settled.addRubber(RubberType.SINGLES, 1);
        settledRubber.addParticipant(anna, Side.HOME);
        settledRubber.addParticipant(bea, Side.AWAY);
        settledRubber.addSet(1, SetType.REGULAR, 6, 3);
        settledRubber.addSet(2, SetType.REGULAR, 6, 4);
        meetingRepository.save(settled);

        // One set each: the sets don't settle a winner.
        Meeting unsettled = new Meeting(home, away, LocalDate.of(2026, 5, 17));
        Rubber unsettledRubber = unsettled.addRubber(RubberType.SINGLES, 1);
        unsettledRubber.addParticipant(anna, Side.HOME);
        unsettledRubber.addParticipant(bea, Side.AWAY);
        unsettledRubber.addSet(1, SetType.REGULAR, 6, 3);
        unsettledRubber.addSet(2, SetType.REGULAR, 3, 6);
        meetingRepository.saveAndFlush(unsettled);

        HeadToHeadRecord record = headToHeadService.getHeadToHead(anna.getId(), bea.getId());

        assertThat(record.matchesPlayed()).isEqualTo(2);
        assertThat(record.recentResults()).containsExactly(MatchResult.WIN);
    }

    @Test
    void recentResultsAreEmptyWhenThePlayersHaveNeverMet() {
        Player anna = playerRepository.save(new Player("Anna", "Adler"));
        Player bea = playerRepository.saveAndFlush(new Player("Bea", "Berger"));

        HeadToHeadRecord record = headToHeadService.getHeadToHead(anna.getId(), bea.getId());

        assertThat(record.recentResults()).isNotNull().isEmpty();
    }
}
