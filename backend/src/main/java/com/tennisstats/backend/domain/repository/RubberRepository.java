package com.tennisstats.backend.domain.repository;

import com.tennisstats.backend.domain.Meeting;
import com.tennisstats.backend.domain.Rubber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RubberRepository extends JpaRepository<Rubber, Long> {

    @Modifying
    @Query("delete from Rubber r where r.meeting = :meeting")
    void deleteByMeeting(Meeting meeting);
}
