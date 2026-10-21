package com.tennisstats.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;

/**
 * A tennis player as tracked across BTV league play, independent of any one team or meeting.
 */
@Entity
@Table(name = "player", uniqueConstraints = @UniqueConstraint(name = "uk_player_external_id", columnNames = "external_id"))
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    /** nuLiga's identifier for this player, used by the crawler/persistence layer to upsert idempotently. */
    @Column(name = "external_id")
    private String externalId;

    protected Player() {
        // for JPA
    }

    public Player(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Player(String firstName, String lastName, String externalId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.externalId = externalId;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player player)) return false;
        return id != null && id.equals(player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getClass());
    }
}
