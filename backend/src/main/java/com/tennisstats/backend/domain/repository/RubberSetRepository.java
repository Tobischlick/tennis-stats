package com.tennisstats.backend.domain.repository;

import com.tennisstats.backend.domain.Meeting;
import com.tennisstats.backend.domain.RubberSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RubberSetRepository extends JpaRepository<RubberSet, Long> {

    @Modifying
    @Query("delete from RubberSet s where s.rubber.meeting = :meeting")
    void deleteByRubberMeeting(Meeting meeting);
}
