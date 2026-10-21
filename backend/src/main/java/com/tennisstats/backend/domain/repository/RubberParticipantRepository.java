package com.tennisstats.backend.domain.repository;

import com.tennisstats.backend.domain.Meeting;
import com.tennisstats.backend.domain.RubberParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RubberParticipantRepository extends JpaRepository<RubberParticipant, Long> {

    @Modifying
    @Query("delete from RubberParticipant p where p.rubber.meeting = :meeting")
    void deleteByRubberMeeting(Meeting meeting);
}
