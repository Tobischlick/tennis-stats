package com.tennisstats.backend.domain;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;

/**
 * One set (or, for a deciding match tie-break, its stand-in) within a {@link Rubber}. Named
 * "RubberSet" rather than "Set" to avoid colliding with {@code java.util.Set}.
 *
 * <p>{@code homeScore}/{@code awayScore} are deliberately plain integers rather than anything
 * shaped like "6 or 7" games: a {@link SetType#MATCH_TIE_BREAK} score is a 10-point tally, not a
 * game count. {@code homeTiebreakScore}/{@code awayTiebreakScore} hold the breaker score (e.g. the
 * "(4)" in 7-6(4)) when a {@link SetType#REGULAR} set was decided by a tie-break.
 */
@Entity
@Table(
        name = "rubber_set",
        uniqueConstraints = @UniqueConstraint(name = "uk_rubber_set_rubber_number", columnNames = {"rubber_id", "set_number"}))
public class RubberSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "rubber_id", nullable = false)
    private Rubber rubber;

    /** 1-based ordinal of this set within its rubber. */
    @Column(name = "set_number", nullable = false)
    private Integer setNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private SetType type = SetType.REGULAR;

    @Column(name = "home_score", nullable = false)
    private Integer homeScore;

    @Column(name = "away_score", nullable = false)
    private Integer awayScore;

    @Column(name = "home_tiebreak_score")
    private Integer homeTiebreakScore;

    @Column(name = "away_tiebreak_score")
    private Integer awayTiebreakScore;

    /** True for a set cut short by a retirement; it mustn't count towards tie-break or deciding-set stats. */
    @Column(name = "unfinished", nullable = false)
    private boolean unfinished;

    protected RubberSet() {
        // for JPA
    }

    public RubberSet(Rubber rubber, int setNumber, SetType type, int homeScore, int awayScore) {
        this.rubber = rubber;
        this.setNumber = setNumber;
        this.type = type;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }

    public void setTiebreakScore(int homeTiebreakScore, int awayTiebreakScore) {
        this.homeTiebreakScore = homeTiebreakScore;
        this.awayTiebreakScore = awayTiebreakScore;
    }

    public boolean isUnfinished() {
        return unfinished;
    }

    public void setUnfinished(boolean unfinished) {
        this.unfinished = unfinished;
    }

    public Long getId() {
        return id;
    }

    public Rubber getRubber() {
        return rubber;
    }

    public void setRubber(Rubber rubber) {
        this.rubber = rubber;
    }

    public Integer getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(Integer setNumber) {
        this.setNumber = setNumber;
    }

    public SetType getType() {
        return type;
    }

    public void setType(SetType type) {
        this.type = type;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(Integer homeScore) {
        this.homeScore = homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(Integer awayScore) {
        this.awayScore = awayScore;
    }

    public Integer getHomeTiebreakScore() {
        return homeTiebreakScore;
    }

    public Integer getAwayTiebreakScore() {
        return awayTiebreakScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RubberSet rubberSet)) return false;
        return id != null && id.equals(rubberSet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getClass());
    }
}
