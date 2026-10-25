package com.tennisstats.backend.domain.repository;

import com.tennisstats.backend.domain.Meeting;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    Optional<Meeting> findByExternalId(String externalId);

    /** External ids of meetings whose result has already been crawled. */
    @Query("select m.externalId from Meeting m where m.externalId is not null and m.hasResult = true")
    Set<String> findExternalIdsWithResults();
}
