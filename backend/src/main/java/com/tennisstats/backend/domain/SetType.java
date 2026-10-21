package com.tennisstats.backend.domain;

/**
 * A {@link RubberSet}'s scoring format. A deciding rubber's "3rd set" is often played as a single
 * 10-point match tie-break instead of a normal set, so the score's meaning depends on this.
 */
public enum SetType {
    REGULAR,
    MATCH_TIE_BREAK
}
