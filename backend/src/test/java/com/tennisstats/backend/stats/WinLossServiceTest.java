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
@Import(WinLossService.class)
@DirtiesContext
class WinLossServiceTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private WinLossService winLossService;

    @Test
    void countsWinsAndLossesAcrossSinglesAndDoubles() {
        Team home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        Team away = teamRepository.save(new Team("TC Rot-Gelb 1"));

        Player anna = playerRepository.save(new Player("Anna", "Adler"));
        Player opponent = playerRepository.save(new Player("Bea", "Berger"));
        Player partner = playerRepository.save(new Player("Clara", "Conrad"));
        Player opponentA = playerRepository.save(new Player("Dana", "Diehl"));
        Player opponentB = playerRepository.save(new Player("Eva", "Engel"));

        // Anna wins a singles rubber, playing HOME, 2 sets to 0.
        Meeting singlesWin = new Meeting(home, away, LocalDate.of(2026, 5, 3));
        Rubber singlesWinRubber = singlesWin.addRubber(RubberType.SINGLES, 1);
        singlesWinRubber.addParticipant(anna, Side.HOME);
        singlesWinRubber.addParticipant(opponent, Side.AWAY);
        singlesWinRubber.addSet(1, SetType.REGULAR, 6, 3);
        singlesWinRubber.addSet(2, SetType.REGULAR, 6, 4);
        meetingRepository.save(singlesWin);

        // Anna loses a singles rubber, playing AWAY, 1 set to 2.
        Meeting singlesLoss = new Meeting(home, away, LocalDate.of(2026, 5, 10));
        Rubber singlesLossRubber = singlesLoss.addRubber(RubberType.SINGLES, 1);
        singlesLossRubber.addParticipant(opponent, Side.HOME);
        singlesLossRubber.addParticipant(anna, Side.AWAY);
        singlesLossRubber.addSet(1, SetType.REGULAR, 6, 3);
        singlesLossRubber.addSet(2, SetType.REGULAR, 4, 6);
        singlesLossRubber.addSet(3, SetType.MATCH_TIE_BREAK, 10, 7);
        meetingRepository.save(singlesLoss);

        // Anna and her partner win a doubles rubber, playing HOME.
        Meeting doublesWin = new Meeting(home, away, LocalDate.of(2026, 5, 17));
        Rubber doublesWinRubber = doublesWin.addRubber(RubberType.DOUBLES, 2);
        doublesWinRubber.addParticipant(anna, Side.HOME);
        doublesWinRubber.addParticipant(partner, Side.HOME);
        doublesWinRubber.addParticipant(opponentA, Side.AWAY);
        doublesWinRubber.addParticipant(opponentB, Side.AWAY);
        doublesWinRubber.addSet(1, SetType.REGULAR, 6, 2);
        doublesWinRubber.addSet(2, SetType.REGULAR, 6, 1);
        meetingRepository.save(doublesWin);

        // Anna and her partner lose a doubles rubber, playing AWAY.
        Meeting doublesLoss = new Meeting(home, away, LocalDate.of(2026, 5, 24));
        Rubber doublesLossRubber = doublesLoss.addRubber(RubberType.DOUBLES, 2);
        doublesLossRubber.addParticipant(opponentA, Side.HOME);
        doublesLossRubber.addParticipant(opponentB, Side.HOME);
        doublesLossRubber.addParticipant(anna, Side.AWAY);
        doublesLossRubber.addParticipant(partner, Side.AWAY);
        doublesLossRubber.addSet(1, SetType.REGULAR, 6, 2);
        doublesLossRubber.addSet(2, SetType.REGULAR, 6, 3);
        meetingRepository.saveAndFlush(doublesLoss);

        WinLossRecord record = winLossService.getWinLossRecord(anna.getId());

        assertThat(record.hasData()).isTrue();
        assertThat(record.wins()).isEqualTo(2);
        assertThat(record.losses()).isEqualTo(2);
        assertThat(record.winPercentage()).isEqualTo(50.0);
    }

    @Test
    void countsARetiredRubberForTheSideThatDidNotRetire() {
        Team home = teamRepository.save(new Team("TC Blau-Weiss 1"));
        Team away = teamRepository.save(new Team("TC Rot-Gelb 1"));
        Player anna = playerRepository.save(new Player("Anna", "Adler"));
        Player bea = playerRepository.save(new Player("Bea", "Berger"));

        // Bea leads 6:0 1:0 but retires; with set scores alone she'd be the winner.
        Meeting meeting = new Meeting(home, away, LocalDate.of(2026, 5, 3));
        Rubber rubber = meeting.addRubber(RubberType.SINGLES, 1);
        rubber.addParticipant(anna, Side.HOME);
        rubber.addParticipant(bea, Side.AWAY);
        rubber.setRetiredSide(Side.AWAY);
        rubber.addSet(1, SetType.REGULAR, 0, 6);
        rubber.addSet(2, SetType.REGULAR, 0, 1).setUnfinished(true);
        meetingRepository.saveAndFlush(meeting);

        assertThat(winLossService.getWinLossRecord(anna.getId()).wins()).isEqualTo(1);
        assertThat(winLossService.getWinLossRecord(bea.getId()).losses()).isEqualTo(1);
    }

    @Test
    void returnsNoDataForAPlayerWithNoMatches() {
        Player noMatches = playerRepository.saveAndFlush(new Player("Frida", "Fischer"));

        WinLossRecord record = winLossService.getWinLossRecord(noMatches.getId());

        assertThat(record.hasData()).isFalse();
        assertThat(record.wins()).isZero();
        assertThat(record.losses()).isZero();
        assertThat(record.winPercentage()).isZero();
    }
}
