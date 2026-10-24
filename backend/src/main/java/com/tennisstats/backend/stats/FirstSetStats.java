package com.tennisstats.backend.stats;

/**
 * A player's record after the first set: of the rubbers where they lost it, how many they still
 * won (comebacks), and of the rubbers where they won it, how many they still lost (blown leads).
 *
 * <p>The rates are {@code null} when their denominator is zero, like {@link DecidingSetStats}.
 */
public record FirstSetStats(
        long lostFirstSet, long comebacks, Double comebackRate, long wonFirstSet, long blownLeads, Double blownLeadRate) {

    public static FirstSetStats noData() {
        return new FirstSetStats(0, 0, null, 0, 0, null);
    }

    static FirstSetStats of(long lostFirstSet, long comebacks, long wonFirstSet, long blownLeads) {
        return new FirstSetStats(
                lostFirstSet,
                comebacks,
                lostFirstSet == 0 ? null : (double) comebacks / lostFirstSet,
                wonFirstSet,
                blownLeads,
                wonFirstSet == 0 ? null : (double) blownLeads / wonFirstSet);
    }

    public boolean hasData() {
        return lostFirstSet + wonFirstSet > 0;
    }
}
