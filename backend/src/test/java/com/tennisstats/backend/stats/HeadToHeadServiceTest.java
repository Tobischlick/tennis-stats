package com.tennisstats.backend.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.tennisstats.backend.PostgresTestcontainer;
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

@DataJpaTest
@Import(HeadToHeadService.class)
class HeadToHeadServiceTest implements PostgresTestcontainer {

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
        assertThat(record.doubles().matchesPlayed()).isZero();
        assertThat(record.doubles().meetings()).isEmpty();
    }

    @Test
    void reportsADoublesEncounterSeparatelyWithoutMixingItIntoTheSinglesRecord() {
        Team home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        Team away = teamRepository.save(new Team("TC Rot-Gelb 1"));

        Player anna = playerRepository.save(new Player("Anna", "Adler"));
        Player bea = playerRepository.save(new Player("Bea", "Berger"));
        Player annasPartner = playerRepository.save(new Player("Clara", "Conrad"));
        Player beasPartner = playerRepository.save(new Player("Dana", "Diehl"));

        // Anna and Bea face each other in a doubles rubber, each with their own partner; Bea's pair wins.
        Meeting doublesMeeting = new Meeting(home, away, LocalDate.of(2026, 5, 3));
        Rubber doublesRubber = doublesMeeting.addRubber(RubberType.DOUBLES, 2);
        doublesRubber.addParticipant(anna, Side.HOME);
        doublesRubber.addParticipant(annasPartner, Side.HOME);
        doublesRubber.addParticipant(bea, Side.AWAY);
        doublesRubber.addParticipant(beasPartner, Side.AWAY);
        doublesRubber.addSet(1, SetType.REGULAR, 2, 6);
        doublesRubber.addSet(2, SetType.REGULAR, 6, 7).setTiebreakScore(5, 7);
        meetingRepository.save(doublesMeeting);

        // Anna beats Bea in singles a week later.
        Meeting singlesMeeting = new Meeting(home, away, LocalDate.of(2026, 5, 10));
        Rubber singlesRubber = singlesMeeting.addRubber(RubberType.SINGLES, 1);
        singlesRubber.addParticipant(anna, Side.HOME);
        singlesRubber.addParticipant(bea, Side.AWAY);
        singlesRubber.addSet(1, SetType.REGULAR, 6, 3);
        singlesRubber.addSet(2, SetType.REGULAR, 6, 4);
        meetingRepository.saveAndFlush(singlesMeeting);

        HeadToHeadRecord record = headToHeadService.getHeadToHead(anna.getId(), bea.getId());

        assertThat(record.hasMatches()).isTrue();
        assertThat(record.matchesPlayed()).isEqualTo(1);
        assertThat(record.playerAWins()).isEqualTo(1);
        assertThat(record.playerBWins()).isZero();
        assertThat(record.recentResults()).containsExactly(MatchResult.WIN);

        HeadToHeadRecord.Doubles doubles = record.doubles();
        assertThat(doubles.matchesPlayed()).isEqualTo(1);
        assertThat(doubles.playerAWins()).isZero();
        assertThat(doubles.playerBWins()).isEqualTo(1);
        assertThat(doubles.meetings()).singleElement().satisfies(meeting -> {
            assertThat(meeting.playedOn()).isEqualTo(LocalDate.of(2026, 5, 3));
            assertThat(meeting.result()).isEqualTo(MatchResult.LOSS);
            assertThat(meeting.partner()).isEqualTo(annasPartner);
            assertThat(meeting.opponents()).containsExactlyInAnyOrder(bea, beasPartner);
            assertThat(meeting.sets()).extracting(SetScore::own, SetScore::opponent)
                    .containsExactly(tuple(2, 6), tuple(6, 7));
            assertThat(meeting.sets().get(1).ownTieBreak()).isEqualTo(5);
        });
    }

    @Test
    void playersWhoOnlyMetInDoublesHaveNoSinglesRecordButADoublesRecord() {
        Team home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        Team away = teamRepository.save(new Team("TC Rot-Gelb 1"));

        Player anna = playerRepository.save(new Player("Anna", "Adler"));
        Player bea = playerRepository.save(new Player("Bea", "Berger"));
        Player annasPartner = playerRepository.save(new Player("Clara", "Conrad"));
        Player beasPartner = playerRepository.save(new Player("Dana", "Diehl"));

        // Bea plays HOME this time, so Anna's perspective is the flipped one.
        Meeting doublesMeeting = new Meeting(away, home, LocalDate.of(2026, 5, 3));
        Rubber doublesRubber = doublesMeeting.addRubber(RubberType.DOUBLES, 1);
        doublesRubber.addParticipant(bea, Side.HOME);
        doublesRubber.addParticipant(beasPartner, Side.HOME);
        doublesRubber.addParticipant(anna, Side.AWAY);
        doublesRubber.addParticipant(annasPartner, Side.AWAY);
        doublesRubber.addSet(1, SetType.REGULAR, 3, 6);
        doublesRubber.addSet(2, SetType.REGULAR, 6, 4);
        doublesRubber.addSet(3, SetType.MATCH_TIE_BREAK, 8, 10);
        meetingRepository.saveAndFlush(doublesMeeting);

        HeadToHeadRecord record = headToHeadService.getHeadToHead(anna.getId(), bea.getId());

        assertThat(record.hasMatches()).isFalse();
        assertThat(record.matchesPlayed()).isZero();
        assertThat(record.recentResults()).isEmpty();
        assertThat(record.doubles().playerAWins()).isEqualTo(1);
        assertThat(record.doubles().playerBWins()).isZero();
        assertThat(record.doubles().meetings()).singleElement().satisfies(meeting -> {
            assertThat(meeting.result()).isEqualTo(MatchResult.WIN);
            assertThat(meeting.ownTeam()).isEqualTo("TC Blau-Weiss 1");
            assertThat(meeting.sets()).extracting(SetScore::own, SetScore::opponent)
                    .containsExactly(tuple(6, 3), tuple(4, 6), tuple(10, 8));
        });
    }

    @Test
    void doesNotCountDoublesWhereTheTwoPlayersWerePartners() {
        Team home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        Team away = teamRepository.save(new Team("TC Rot-Gelb 1"));

        Player anna = playerRepository.save(new Player("Anna", "Adler"));
        Player bea = playerRepository.save(new Player("Bea", "Berger"));
        Player clara = playerRepository.save(new Player("Clara", "Conrad"));
        Player dana = playerRepository.save(new Player("Dana", "Diehl"));

        Meeting doublesMeeting = new Meeting(home, away, LocalDate.of(2026, 5, 3));
        Rubber doublesRubber = doublesMeeting.addRubber(RubberType.DOUBLES, 1);
        doublesRubber.addParticipant(anna, Side.HOME);
        doublesRubber.addParticipant(bea, Side.HOME);
        doublesRubber.addParticipant(clara, Side.AWAY);
        doublesRubber.addParticipant(dana, Side.AWAY);
        doublesRubber.addSet(1, SetType.REGULAR, 6, 2);
        doublesRubber.addSet(2, SetType.REGULAR, 6, 1);
        meetingRepository.saveAndFlush(doublesMeeting);

        HeadToHeadRecord record = headToHeadService.getHeadToHead(anna.getId(), bea.getId());

        assertThat(record.hasMatches()).isFalse();
        assertThat(record.doubles().matchesPlayed()).isZero();
        assertThat(record.doubles().meetings()).isEmpty();
    }

    @Test
    void doublesMeetingsAreNewestFirstAndSkipUnsettledRubbers() {
        Team home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        Team away = teamRepository.save(new Team("TC Rot-Gelb 1"));

        Player anna = playerRepository.save(new Player("Anna", "Adler"));
        Player bea = playerRepository.save(new Player("Bea", "Berger"));
        Player clara = playerRepository.save(new Player("Clara", "Conrad"));
        Player dana = playerRepository.save(new Player("Dana", "Diehl"));

        doubles(home, away, LocalDate.of(2026, 5, 3), anna, clara, bea, dana, 6, 1);
        doubles(home, away, LocalDate.of(2026, 5, 17), anna, clara, bea, dana, 1, 6);
        // One set each: the sets don't settle a winner.
        Meeting unsettled = new Meeting(home, away, LocalDate.of(2026, 6, 1));
        Rubber unsettledRubber = unsettled.addRubber(RubberType.DOUBLES, 1);
        unsettledRubber.addParticipant(anna, Side.HOME);
        unsettledRubber.addParticipant(clara, Side.HOME);
        unsettledRubber.addParticipant(bea, Side.AWAY);
        unsettledRubber.addParticipant(dana, Side.AWAY);
        unsettledRubber.addSet(1, SetType.REGULAR, 6, 3);
        unsettledRubber.addSet(2, SetType.REGULAR, 3, 6);
        meetingRepository.saveAndFlush(unsettled);

        HeadToHeadRecord.Doubles doubles =
                headToHeadService.getHeadToHead(anna.getId(), bea.getId()).doubles();

        assertThat(doubles.matchesPlayed()).isEqualTo(3);
        assertThat(doubles.meetings())
                .extracting(MatchDetail::playedOn, MatchDetail::result)
                .containsExactly(
                        tuple(LocalDate.of(2026, 5, 17), MatchResult.LOSS),
                        tuple(LocalDate.of(2026, 5, 3), MatchResult.WIN));
    }

    private void doubles(
            Team home,
            Team away,
            LocalDate playedOn,
            Player homeA,
            Player homeB,
            Player awayA,
            Player awayB,
            int homeGames,
            int awayGames) {
        Meeting meeting = new Meeting(home, away, playedOn);
        Rubber rubber = meeting.addRubber(RubberType.DOUBLES, 1);
        rubber.addParticipant(homeA, Side.HOME);
        rubber.addParticipant(homeB, Side.HOME);
        rubber.addParticipant(awayA, Side.AWAY);
        rubber.addParticipant(awayB, Side.AWAY);
        rubber.addSet(1, SetType.REGULAR, homeGames, awayGames);
        rubber.addSet(2, SetType.REGULAR, homeGames, awayGames);
        meetingRepository.save(meeting);
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
