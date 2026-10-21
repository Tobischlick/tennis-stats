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
 * Links one {@link Player} to one {@link Side} of a {@link Rubber}. A singles rubber has one
 * participant per side; a doubles rubber has two (the pairing).
 */
@Entity
@Table(
        name = "rubber_participant",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_rubber_participant_rubber_side_player",
                        columnNames = {"rubber_id", "side", "player_id"}))
public class RubberParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "rubber_id", nullable = false)
    private Rubber rubber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false, length = 10)
    private Side side;

    protected RubberParticipant() {
        // for JPA
    }

    public RubberParticipant(Rubber rubber, Player player, Side side) {
        this.rubber = rubber;
        this.player = player;
        this.side = side;
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

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RubberParticipant that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getClass());
    }
}
