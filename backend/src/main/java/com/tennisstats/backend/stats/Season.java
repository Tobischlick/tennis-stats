package com.tennisstats.backend.stats;

import java.time.LocalDate;
import java.time.Month;

/**
 * A BTV season: summer runs April–September ("Sommer 2026"), winter October–March ("Winter
 * 2025/26"), so January to March belong to the winter that started the previous October. {@code
 * startYear} is the calendar year the season starts in. Ordered chronologically.
 */
public record Season(Type type, int startYear) implements Comparable<Season> {

    public enum Type {
        SUMMER,
        WINTER
    }

    public static Season of(LocalDate playedOn) {
        Month month = playedOn.getMonth();
        if (month.compareTo(Month.APRIL) >= 0 && month.compareTo(Month.SEPTEMBER) <= 0) {
            return new Season(Type.SUMMER, playedOn.getYear());
        }
        int startYear = month.compareTo(Month.OCTOBER) >= 0 ? playedOn.getYear() : playedOn.getYear() - 1;
        return new Season(Type.WINTER, startYear);
    }

    /** German display name as used by BTV, e.g. "Sommer 2026" or "Winter 2025/26". */
    public String label() {
        if (type == Type.SUMMER) {
            return "Sommer " + startYear;
        }
        return "Winter %d/%02d".formatted(startYear, (startYear + 1) % 100);
    }

    public LocalDate startsOn() {
        return type == Type.SUMMER ? LocalDate.of(startYear, Month.APRIL, 1) : LocalDate.of(startYear, Month.OCTOBER, 1);
    }

    public LocalDate endsOn() {
        return type == Type.SUMMER
                ? LocalDate.of(startYear, Month.SEPTEMBER, 30)
                : LocalDate.of(startYear + 1, Month.MARCH, 31);
    }

    @Override
    public int compareTo(Season other) {
        return startsOn().compareTo(other.startsOn());
    }
}
