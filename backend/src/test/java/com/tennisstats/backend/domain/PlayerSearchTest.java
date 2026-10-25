package com.tennisstats.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tennisstats.backend.PostgresTestcontainer;
import com.tennisstats.backend.domain.repository.PlayerRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class PlayerSearchTest implements PostgresTestcontainer {

    @Autowired
    private PlayerRepository playerRepository;

    @BeforeEach
    void setUp() {
        playerRepository.save(new Player("Tobias", "Schlick"));
        playerRepository.save(new Player("Aaron", "Schlick"));
        playerRepository.save(new Player("Tobias", "Müller"));
    }

    private List<String> search(String query) {
        return playerRepository.searchByName(query).stream()
                .map(player -> player.getFirstName() + " " + player.getLastName())
                .toList();
    }

    @Test
    void findsAPlayerByFirstAndLastName() {
        assertThat(search("Tobias Schlick")).containsExactly("Tobias Schlick");
    }

    @Test
    void findsAPlayerByLastAndFirstName() {
        assertThat(search("Schlick Tobias")).containsExactly("Tobias Schlick");
    }

    @Test
    void findsAPlayerInNuLigasLastNameCommaFirstNameNotation() {
        assertThat(search("Schlick, Tobias")).containsExactly("Tobias Schlick");
    }

    @Test
    void findsAPlayerByPartialWords() {
        assertThat(search("tob schl")).containsExactly("Tobias Schlick");
    }

    @Test
    void requiresEveryWordToMatch() {
        assertThat(search("Tobias Meier")).isEmpty();
    }

    @Test
    void matchesASingleWordAgainstFirstOrLastNameSortedByLastThenFirstName() {
        assertThat(search("schlick")).containsExactly("Aaron Schlick", "Tobias Schlick");
        assertThat(search("tobias")).containsExactly("Tobias Müller", "Tobias Schlick");
    }

    @Test
    void ignoresExtraWhitespaceAndCommas() {
        assertThat(search("  Schlick ,  Tobias ")).containsExactly("Tobias Schlick");
    }

    @Test
    void returnsNothingForAQueryWithoutAnyWord() {
        assertThat(search(" , ")).isEmpty();
    }
}
