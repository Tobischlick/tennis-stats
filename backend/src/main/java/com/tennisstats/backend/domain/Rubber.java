package com.tennisstats.backend.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A single singles/doubles game within a {@link Meeting} (a "rubber" in team-tennis terminology),
 * e.g. "Einzel 1" or "Doppel 2". Made up of {@link RubberSet}s and played by {@link RubberParticipant}s.
 */
@Entity
@Table(
        name = "rubber",
        uniqueConstraints = @UniqueConstraint(name = "uk_rubber_meeting_position", columnNames = {"meeting_id", "position"}))
public class Rubber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private RubberType type;

    /** Order of this rubber within its meeting (e.g. 1..6), as listed on the meeting report. */
    @Column(name = "position", nullable = false)
    private Integer position;

    /**
     * The side nuLiga marks "(w.o.)": it retired, conceded a walkover or didn't turn up, so the
     * other side won regardless of the set scores. {@code null} for a rubber played to the end.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "retired_side", length = 10)
    private Side retiredSide;

    @OneToMany(mappedBy = "rubber", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RubberSet> sets = new ArrayList<>();

    @OneToMany(mappedBy = "rubber", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RubberParticipant> participants = new ArrayList<>();

    protected Rubber() {
        // for JPA
    }

    public Rubber(Meeting meeting, RubberType type, int position) {
        this.meeting = meeting;
        this.type = type;
        this.position = position;
    }

    public RubberSet addSet(int setNumber, SetType setType, int homeScore, int awayScore) {
        RubberSet set = new RubberSet(this, setNumber, setType, homeScore, awayScore);
        sets.add(set);
        return set;
    }

    public RubberParticipant addParticipant(Player player, Side side) {
        RubberParticipant participant = new RubberParticipant(this, player, side);
        participants.add(participant);
        return participant;
    }

    public Long getId() {
        return id;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    public RubberType getType() {
        return type;
    }

    public void setType(RubberType type) {
        this.type = type;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Side getRetiredSide() {
        return retiredSide;
    }

    public void setRetiredSide(Side retiredSide) {
        this.retiredSide = retiredSide;
    }

    public List<RubberSet> getSets() {
        return sets;
    }

    public List<RubberParticipant> getParticipants() {
        return participants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rubber rubber)) return false;
        return id != null && id.equals(rubber.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getClass());
    }
}
