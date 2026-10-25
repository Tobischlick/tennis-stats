package com.tennisstats.backend.domain.repository;

import com.tennisstats.backend.domain.Player;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PlayerRepository extends JpaRepository<Player, Long>, JpaSpecificationExecutor<Player> {

    Optional<Player> findByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Players whose first or last name contains every word of the query, case-insensitive, so "Tobias Schlick",
     * "Schlick, Tobias" and "Tob Schl" all find the same player. Words are split on whitespace and commas.
     */
    default List<Player> searchByName(String query) {
        List<String> words = Arrays.stream(query.toLowerCase(Locale.ROOT).split("[\\s,]+"))
                .filter(word -> !word.isEmpty())
                .toList();
        if (words.isEmpty()) {
            return List.of();
        }
        Specification<Player> everyWord = words.stream()
                .map(PlayerRepository::firstOrLastNameContains)
                .reduce(Specification::and)
                .orElseThrow();
        return findAll(everyWord, Sort.by("lastName", "firstName"));
    }

    private static Specification<Player> firstOrLastNameContains(String word) {
        String pattern = "%" + word + "%";
        return (player, query, cb) -> cb.or(
                cb.like(cb.lower(player.get("firstName")), pattern),
                cb.like(cb.lower(player.get("lastName")), pattern));
    }
}
