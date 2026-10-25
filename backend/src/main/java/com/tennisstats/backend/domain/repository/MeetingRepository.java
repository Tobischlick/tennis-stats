package com.tennisstats.backend.domain.repository;

import com.tennisstats.backend.domain.Meeting;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    Optional<Meeting> findByExternalId(String externalId);

    /**
     * External ids of meetings whose result has already been crawled: at least one rubber has a stored set
     * or is marked as a walkover. A meeting given entirely as a walkover has no sets at all.
     */
    @Query("""
            select distinct r.meeting.externalId from Rubber r
            where r.meeting.externalId is not null and (r.sets is not empty or r.retiredSide is not null)""")
    Set<String> findExternalIdsWithResults();
}
