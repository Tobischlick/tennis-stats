package com.tennisstats.backend.domain.repository;

import com.tennisstats.backend.domain.Meeting;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    Optional<Meeting> findByExternalId(String externalId);

    /** External ids of meetings with at least one stored set, i.e. whose result has already been crawled. */
    @Query("select distinct s.rubber.meeting.externalId from RubberSet s where s.rubber.meeting.externalId is not null")
    Set<String> findExternalIdsWithResults();
}
