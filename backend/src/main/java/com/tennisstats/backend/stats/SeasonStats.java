package com.tennisstats.backend.stats;

/**
 * A player's win/loss and tie-break record within one {@link Season}, counted by the same rules as
 * {@link WinLossService} and {@link TieBreakService}, so the seasons add up to the overall record.
 */
public record SeasonStats(Season season, WinLossRecord record, TieBreakStats tieBreak) {}
