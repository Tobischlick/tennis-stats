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
 * A club's team within a BTV league (e.g. "TC Blau-Weiss 1"), as it appears in a {@link Meeting}.
 */
@Entity
@Table(name = "team", uniqueConstraints = @UniqueConstraint(name = "uk_team_external_id", columnNames = "external_id"))
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    /** nuLiga's identifier for this team, used by the crawler/persistence layer to upsert idempotently. */
    @Column(name = "external_id")
    private String externalId;

    protected Team() {
        // for JPA
    }

    public Team(String name) {
        this.name = name;
    }

    public Team(String name, String externalId) {
        this.name = name;
        this.externalId = externalId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        if (!(o instanceof Team team)) return false;
        return id != null && id.equals(team.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getClass());
    }
}
