package com.tennisstats.backend.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A BTV league "Medenspiel": one team-level meeting between a home and an away {@link Team},
 * made up of several {@link Rubber}s (singles/doubles games). "Match" is ambiguous between this
 * team-level event and one game within it, so this codebase reserves "Meeting" for the former and
 * "Rubber" (the standard team-tennis term) for the latter.
 */
@Entity
@Table(name = "meeting", uniqueConstraints = @UniqueConstraint(name = "uk_meeting_external_id", columnNames = "external_id"))
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @Column(name = "played_on", nullable = false)
    private LocalDate playedOn;

    /** nuLiga's identifier for this meeting, used by the crawler/persistence layer to upsert idempotently. */
    @Column(name = "external_id")
    private String externalId;

    /**
     * Whether the crawled match report already held a result. A meeting given entirely as a walkover has
     * one but no stored rubbers (rubbers against an absent side aren't stored), so this is tracked here.
     */
    @Column(name = "has_result", nullable = false)
    private boolean hasResult;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rubber> rubbers = new ArrayList<>();

    protected Meeting() {
        // for JPA
    }

    public Meeting(Team homeTeam, Team awayTeam, LocalDate playedOn) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.playedOn = playedOn;
    }

    public Rubber addRubber(RubberType type, int position) {
        Rubber rubber = new Rubber(this, type, position);
        rubbers.add(rubber);
        return rubber;
    }

    public Long getId() {
        return id;
    }

    public boolean hasResult() {
        return hasResult;
    }

    public void setHasResult(boolean hasResult) {
        this.hasResult = hasResult;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public LocalDate getPlayedOn() {
        return playedOn;
    }

    public void setPlayedOn(LocalDate playedOn) {
        this.playedOn = playedOn;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public List<Rubber> getRubbers() {
        return rubbers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Meeting meeting)) return false;
        return id != null && id.equals(meeting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getClass());
    }
}
