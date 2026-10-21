package com.tennisstats.backend.crawler;

import com.tennisstats.backend.domain.Meeting;
import com.tennisstats.backend.domain.Player;
import com.tennisstats.backend.domain.RubberType;
import com.tennisstats.backend.domain.SetType;
import com.tennisstats.backend.domain.Side;
import com.tennisstats.backend.domain.Team;
import com.tennisstats.backend.domain.repository.MeetingRepository;
import com.tennisstats.backend.domain.repository.PlayerRepository;
import com.tennisstats.backend.domain.repository.RubberParticipantRepository;
import com.tennisstats.backend.domain.repository.RubberRepository;
import com.tennisstats.backend.domain.repository.RubberSetRepository;
import com.tennisstats.backend.domain.repository.TeamRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Maps a crawled {@link ScheduledMeetingDto}/{@link MatchReport} pair onto the domain model and
 * persists it. Upserts by {@link Meeting#getExternalId()} (nuLiga's meeting id), so re-running
 * persistence for the same meeting updates it in place instead of duplicating rows; teams and
 * players are found-or-created by name so repeated sightings resolve to the same row.
 */
@Service
public class MeetingPersistenceService {

    private static final String ABSENT_PLAYER_PLACEHOLDER = "Spieler/in nicht anwesend";

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final MeetingRepository meetingRepository;
    private final RubberRepository rubberRepository;
    private final RubberSetRepository rubberSetRepository;
    private final RubberParticipantRepository rubberParticipantRepository;
    private final EntityManager entityManager;

    public MeetingPersistenceService(
            TeamRepository teamRepository,
            PlayerRepository playerRepository,
            MeetingRepository meetingRepository,
            RubberRepository rubberRepository,
            RubberSetRepository rubberSetRepository,
            RubberParticipantRepository rubberParticipantRepository,
            EntityManager entityManager) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.meetingRepository = meetingRepository;
        this.rubberRepository = rubberRepository;
        this.rubberSetRepository = rubberSetRepository;
        this.rubberParticipantRepository = rubberParticipantRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public Meeting persist(ScheduledMeetingDto scheduledMeeting, MatchReport matchReport) {
        Team homeTeam = findOrCreateTeam(scheduledMeeting.homeTeam());
        Team awayTeam = findOrCreateTeam(scheduledMeeting.awayTeam());

        Meeting meeting = findOrCreateMeeting(scheduledMeeting, homeTeam, awayTeam);
        // flush the meeting's own field changes now, before the bulk deletes below - a bulk
        // delete doesn't see uncommitted changes, and the refresh that follows it would
        // otherwise overwrite them with the stale pre-update row.
        meeting = meetingRepository.saveAndFlush(meeting);
        deleteExistingRubbers(meeting);
        addRubbers(meeting, matchReport);

        return meetingRepository.saveAndFlush(meeting);
    }

    private Meeting findOrCreateMeeting(ScheduledMeetingDto scheduledMeeting, Team homeTeam, Team awayTeam) {
        String externalId = String.valueOf(scheduledMeeting.meetingId());

        return meetingRepository
                .findByExternalId(externalId)
                .map(meeting -> {
                    meeting.setHomeTeam(homeTeam);
                    meeting.setAwayTeam(awayTeam);
                    meeting.setPlayedOn(scheduledMeeting.date().toLocalDate());
                    return meeting;
                })
                .orElseGet(() -> {
                    Meeting meeting = new Meeting(homeTeam, awayTeam, scheduledMeeting.date().toLocalDate());
                    meeting.setExternalId(externalId);
                    return meeting;
                });
    }

    /**
     * Deletes any rubbers (and their sets/participants) already stored for this meeting, via
     * bulk {@code @Modifying} deletes on the repositories - the entity graph is rebuilt fresh on
     * every persist (a re-crawl always reports the meeting's full current state). These run as
     * direct {@code DELETE} statements rather than the load-then-{@code remove()} strategy a
     * plain derived {@code deleteBy...} query would use, so the old rows are gone immediately,
     * without ever loading the old entities into this persistence context. Deleting bottom-up
     * (participants, then sets, then rubbers) respects the FK constraints.
     *
     * <p>{@code meeting} is refreshed afterwards, even though nothing here reads it: when the
     * same {@link Meeting} is persisted twice in one session (as a re-crawl within the same
     * request/transaction does), Hibernate already holds its {@code rubbers} collection
     * initialized from the first run, still referencing the just-deleted rubbers - a bulk delete
     * doesn't touch the persistence context, so it can't update that collection for us. Left
     * alone, the next auto-flush - triggered by {@link #findOrCreatePlayer}'s lookup once new
     * rubbers are appended - finds that stale, still-"full" collection dirty against its old
     * snapshot and, via {@code orphanRemoval}, tries to re-delete the already-deleted rubbers
     * (a no-op {@code DELETE} that fails optimistic locking since 0 rows match). A plain
     * {@code meeting.getRubbers().clear()} has the same problem the other way around: it makes
     * Hibernate schedule those same doomed deletes right away. {@link EntityManager#refresh}
     * instead re-loads the collection (now genuinely empty) straight from the database and
     * resets its snapshot to match, so there is nothing left to reconcile.
     */
    private void deleteExistingRubbers(Meeting meeting) {
        if (meeting.getId() == null) {
            return; // brand new meeting, nothing to delete
        }
        rubberParticipantRepository.deleteByRubberMeeting(meeting);
        rubberSetRepository.deleteByRubberMeeting(meeting);
        rubberRepository.deleteByMeeting(meeting);
        entityManager.refresh(meeting);
    }

    private void addRubbers(Meeting meeting, MatchReport matchReport) {
        int position = 0;
        for (Rubber rubberDto : matchReport.singles()) {
            addRubber(meeting, RubberType.SINGLES, ++position, rubberDto);
        }
        for (Rubber rubberDto : matchReport.doubles()) {
            addRubber(meeting, RubberType.DOUBLES, ++position, rubberDto);
        }
    }

    /**
     * Skips rubbers where a side wasn't fielded: nuLiga lists such a side with the placeholder
     * "Spieler/in nicht anwesend" instead of real players. Nothing was played (no set scores), and
     * storing it would turn the placeholder into a fake player and give the present player a match
     * without an opponent. The caller still advances {@code position}, so stored positions keep
     * matching the report order.
     *
     * <p>A set is only flagged unfinished for a rubber nuLiga marks as retired: a rubber played to
     * the end is over by definition, whatever its set scores look like.
     */
    private void addRubber(Meeting meeting, RubberType type, int position, Rubber rubberDto) {
        if (Stream.concat(rubberDto.homePlayers().stream(), rubberDto.awayPlayers().stream())
                .anyMatch(name -> name.startsWith(ABSENT_PLAYER_PLACEHOLDER))) {
            return;
        }

        var rubber = meeting.addRubber(type, position);
        rubber.setRetiredSide(rubberDto.retiredSide());

        for (String playerName : rubberDto.homePlayers()) {
            rubber.addParticipant(findOrCreatePlayer(playerName), Side.HOME);
        }
        for (String playerName : rubberDto.awayPlayers()) {
            rubber.addParticipant(findOrCreatePlayer(playerName), Side.AWAY);
        }

        int setNumber = 0;
        for (Score score : rubberDto.sets()) {
            SetType setType = score.isMatchTieBreak() ? SetType.MATCH_TIE_BREAK : SetType.REGULAR;
            var set = rubber.addSet(++setNumber, setType, score.home(), score.away());
            set.setUnfinished(rubberDto.retiredSide() != null && !score.isFinishedSet());
        }
    }

    private Team findOrCreateTeam(String name) {
        return teamRepository.findByName(name).orElseGet(() -> teamRepository.save(new Team(name)));
    }

    /** Parses nuLiga's "Lastname, Firstname" player names, e.g. "Pohlmann, Ole". */
    private Player findOrCreatePlayer(String rawName) {
        List<String> parts = List.of(rawName.split(",", 2));
        String lastName = parts.get(0).trim();
        String firstName = parts.size() > 1 ? parts.get(1).trim() : "";

        return playerRepository
                .findByFirstNameAndLastName(firstName, lastName)
                .orElseGet(() -> playerRepository.save(new Player(firstName, lastName)));
    }
}
