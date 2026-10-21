package com.tennisstats.backend.stats;

import java.util.List;

/** One page of a player's settled rubbers, newest first, plus how many there are in total. */
public record MatchPage(List<MatchDetail> matches, int total) {}
