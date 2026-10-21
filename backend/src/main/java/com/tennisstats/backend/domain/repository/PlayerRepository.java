package com.tennisstats.backend.domain.repository;

import com.tennisstats.backend.domain.Player;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByFirstNameAndLastName(String firstName, String lastName);

    @Query("""
            SELECT p FROM Player p
            WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY p.lastName, p.firstName
            """)
    List<Player> searchByName(@Param("query") String query);
}
