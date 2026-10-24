package com.tennisstats.backend.stats;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SeasonTest {

    @Test
    void summerRunsFromAprilFirstToSeptemberThirtieth() {
        Season summer = new Season(Season.Type.SUMMER, 2026);

        assertThat(Season.of(LocalDate.of(2026, 3, 31))).isNotEqualTo(summer);
        assertThat(Season.of(LocalDate.of(2026, 4, 1))).isEqualTo(summer);
        assertThat(Season.of(LocalDate.of(2026, 9, 30))).isEqualTo(summer);
        assertThat(Season.of(LocalDate.of(2026, 10, 1))).isNotEqualTo(summer);
        assertThat(summer.label()).isEqualTo("Sommer 2026");
        assertThat(summer.startsOn()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(summer.endsOn()).isEqualTo(LocalDate.of(2026, 9, 30));
    }

    @Test
    void winterSpansNewYearAndBelongsToTheYearItStarted() {
        Season winter = new Season(Season.Type.WINTER, 2025);

        assertThat(Season.of(LocalDate.of(2025, 9, 30))).isEqualTo(new Season(Season.Type.SUMMER, 2025));
        assertThat(Season.of(LocalDate.of(2025, 10, 1))).isEqualTo(winter);
        assertThat(Season.of(LocalDate.of(2025, 12, 31))).isEqualTo(winter);
        assertThat(Season.of(LocalDate.of(2026, 1, 1))).isEqualTo(winter);
        assertThat(Season.of(LocalDate.of(2026, 3, 31))).isEqualTo(winter);
        assertThat(Season.of(LocalDate.of(2026, 4, 1))).isEqualTo(new Season(Season.Type.SUMMER, 2026));
        assertThat(winter.label()).isEqualTo("Winter 2025/26");
        assertThat(winter.startsOn()).isEqualTo(LocalDate.of(2025, 10, 1));
        assertThat(winter.endsOn()).isEqualTo(LocalDate.of(2026, 3, 31));
    }

    @Test
    void labelsTheWinterAcrossACenturyWithTwoDigits() {
        assertThat(new Season(Season.Type.WINTER, 2099).label()).isEqualTo("Winter 2099/00");
        assertThat(new Season(Season.Type.WINTER, 2008).label()).isEqualTo("Winter 2008/09");
    }

    @Test
    void ordersSeasonsChronologically() {
        Season summer2025 = new Season(Season.Type.SUMMER, 2025);
        Season winter2025 = new Season(Season.Type.WINTER, 2025);
        Season summer2026 = new Season(Season.Type.SUMMER, 2026);
        List<Season> seasons = new ArrayList<>(List.of(summer2026, winter2025, summer2025));

        seasons.sort(null);

        assertThat(seasons).containsExactly(summer2025, winter2025, summer2026);
    }
}
