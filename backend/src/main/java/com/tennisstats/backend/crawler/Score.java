package com.tennisstats.backend.crawler;

/**
 * A "home:away" score pair, e.g. a set score (6:4) or a match/sets/games summary (2:7).
 */
public record Score(int home, int away) {

	/**
	 * True when this score reached double digits, i.e. it's a match tie-break played
	 * instead of a full set (nuLiga plays a 10-point tie-break as the deciding 3rd set).
	 */
	public boolean isMatchTieBreak() {
		return home >= 10 || away >= 10;
	}

	/**
	 * True when this set score is one a set can end on (6:4, 7:5, 7:6, 10:8, ...), false for a
	 * set cut short by a retirement, e.g. 0:3.
	 */
	public boolean isFinishedSet() {
		int high = Math.max(home, away);
		int low = Math.min(home, away);
		if (isMatchTieBreak()) {
			return high - low >= 2;
		}
		return (high == 6 && low <= 4) || (high == 7 && (low == 5 || low == 6));
	}
}
