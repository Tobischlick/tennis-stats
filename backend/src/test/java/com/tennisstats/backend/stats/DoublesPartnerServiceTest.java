package com.tennisstats.backend.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(DoublesPartnerService.class)
class DoublesPartnerServiceTest implements PostgresTestcontainer {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private DoublesPartnerService doublesPartnerService;

    private Team home;
    private Team away;
    private Player anna;
    private Player opponentOne;
    private Player opponentTwo;
    private LocalDate playedOn;

    @BeforeEach
    void setUp() {
        home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        away = teamRepository.save(new Team("TC Rot-Gelb 1"));
        anna = playerRepository.save(new Player("Anna", "Adler"));
        opponentOne = playerRepository.save(new Player("Olga", "Ott"));
        opponentTwo = playerRepository.save(new Player("Paula", "Pohl"));
        playedOn = LocalDate.of(2026, 5, 3);
    }

    /** A doubles rubber of Anna and {@code partner} on {@code annasSide} against Olga and Paula, without sets yet. */
    private Rubber doubles(Player partner, Side annasSide) {
        Side otherSide = annasSide == Side.HOME ? Side.AWAY : Side.HOME;
        Meeting meeting = new Meeting(home, away, playedOn);
        playedOn = playedOn.plusWeeks(1);
        Rubber rubber = meeting.addRubber(RubberType.DOUBLES, 1);
        rubber.addParticipant(anna, annasSide);
        rubber.addParticipant(partner, annasSide);
        rubber.addParticipant(opponentOne, otherSide);
        rubber.addParticipant(opponentTwo, otherSide);
        return rubber;
    }

    private void annaWinsWith(Player partner) {
        Rubber rubber = doubles(partner, Side.HOME);
        rubber.addSet(1, SetType.REGULAR, 6, 3);
        rubber.addSet(2, SetType.REGULAR, 6, 4);
        meetingRepository.saveAndFlush(rubber.getMeeting());
    }

    private void annaLosesWith(Player partner) {
        // Anna plays AWAY here, so the home side's 6:2 6:1 is a loss for her pair.
        Rubber rubber = doubles(partner, Side.AWAY);
        rubber.addSet(1, SetType.REGULAR, 6, 2);
        rubber.addSet(2, SetType.REGULAR, 6, 1);
        meetingRepository.saveAndFlush(rubber.getMeeting());
    }

    @Test
    void sortsPartnersByMatchesPlayedThenWinPercentage() {
        Player clara = playerRepository.save(new Player("Clara", "Conrad"));
        Player dana = playerRepository.save(new Player("Dana", "Diehl"));
        Player eva = playerRepository.save(new Player("Eva", "Engel"));

        annaLosesWith(eva);
        annaWinsWith(clara);
        annaLosesWith(clara);
        annaWinsWith(dana);
        annaWinsWith(clara);

        assertThat(doublesPartnerService.getDoublesPartners(anna.getId()))
                .extracting(
                        record -> record.partner().getFirstName(),
                        DoublesPartnerRecord::wins,
                        DoublesPartnerRecord::losses)
                .containsExactly(tuple("Clara", 2, 1), tuple("Dana", 1, 0), tuple("Eva", 0, 1));
    }

    @Test
    void reportsAMixedRecordWithOnePartnerFromBothSides() {
        Player clara = playerRepository.save(new Player("Clara", "Conrad"));

        annaWinsWith(clara);
        annaLosesWith(clara);
        annaLosesWith(clara);

        assertThat(doublesPartnerService.getDoublesPartners(anna.getId())).singleElement().satisfies(record -> {
            assertThat(record.partner().getId()).isEqualTo(clara.getId());
            assertThat(record.matchesPlayed()).isEqualTo(3);
            assertThat(record.wins()).isEqualTo(1);
            assertThat(record.losses()).isEqualTo(2);
            assertThat(record.winPercentage()).isCloseTo(33.3, offset(0.1));
        });
    }

    @Test
    void countsRubbersWithoutAKnownWinnerLikeTheWinLossRecord() {
        Player clara = playerRepository.save(new Player("Clara", "Conrad"));
        Player dana = playerRepository.save(new Player("Dana", "Diehl"));

        // Retirement of the opponents: a win for Anna and Clara even without a finished set.
        Rubber retired = doubles(clara, Side.HOME);
        retired.addSet(1, SetType.REGULAR, 2, 3);
        retired.setRetiredSide(Side.AWAY);
        meetingRepository.saveAndFlush(retired.getMeeting());

        // An unfinished rubber with Clara counts towards neither wins nor losses.
        Rubber unfinished = doubles(clara, Side.HOME);
        unfinished.addSet(1, SetType.REGULAR, 6, 3);
        unfinished.addSet(2, SetType.REGULAR, 3, 6);
        meetingRepository.saveAndFlush(unfinished.getMeeting());

        // Dana's only rubber with Anna has no sets at all, so she isn't listed.
        meetingRepository.saveAndFlush(doubles(dana, Side.HOME).getMeeting());

        assertThat(doublesPartnerService.getDoublesPartners(anna.getId()))
                .extracting(
                        record -> record.partner().getFirstName(),
                        DoublesPartnerRecord::wins,
                        DoublesPartnerRecord::losses)
                .containsExactly(tuple("Clara", 1, 0));
    }

    @Test
    void returnsNoPartnersForAPlayerWithoutDoubles() {
        Meeting meeting = new Meeting(home, away, playedOn);
        Rubber singles = meeting.addRubber(RubberType.SINGLES, 1);
        singles.addParticipant(anna, Side.HOME);
        singles.addParticipant(opponentOne, Side.AWAY);
        singles.addSet(1, SetType.REGULAR, 6, 3);
        singles.addSet(2, SetType.REGULAR, 6, 4);
        meetingRepository.saveAndFlush(meeting);

        assertThat(doublesPartnerService.getDoublesPartners(anna.getId())).isEmpty();
    }

    @Test
    void doesNotCountOpponentsAsPartners() {
        Player clara = playerRepository.save(new Player("Clara", "Conrad"));
        annaWinsWith(clara);

        assertThat(doublesPartnerService.getDoublesPartners(opponentOne.getId()))
                .extracting(
                        record -> record.partner().getFirstName(),
                        DoublesPartnerRecord::wins,
                        DoublesPartnerRecord::losses)
                .containsExactly(tuple("Paula", 0, 1));
    }
}
