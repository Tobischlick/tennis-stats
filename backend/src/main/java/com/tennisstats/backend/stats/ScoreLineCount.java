package com.tennisstats.backend.stats;

/** A score line from the winner's perspective (e.g. "6:4 6:3") and how many rubbers ended with it. */
public record ScoreLineCount(String scoreLine, long count) {}
