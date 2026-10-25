package com.tennisstats.backend.crawler;

import static org.assertj.core.api.Assertions.assertThat;

import com.tennisstats.backend.domain.Side;
import java.util.List;
import org.junit.jupiter.api.Test;

class MatchReportTest {

    private static MatchReport reportWith(Rubber rubber) {
        return new MatchReport("TC Musterstadt 1", "TC Beispielhausen 1", null, List.of(rubber), List.of());
    }

    private static Rubber rubber(List<Score> sets, Score matchesResult, Side retiredSide) {
        return new Rubber(1, List.of("Muster, Max"), List.of("Beispiel, Ben"), sets, matchesResult, null, null, retiredSide);
    }

    @Test
    void hasAResultOnceARubberHasSets() {
        assertThat(reportWith(rubber(List.of(new Score(6, 4), new Score(6, 3)), new Score(1, 0), null)).hasResult())
                .isTrue();
    }

    @Test
    void hasAResultForAWalkoverScoredWithoutSets() {
        assertThat(reportWith(rubber(List.of(), new Score(1, 0), Side.AWAY)).hasResult()).isTrue();
    }

    @Test
    void hasNoResultWhileNoRubberIsScored() {
        assertThat(reportWith(rubber(List.of(), null, null)).hasResult()).isFalse();
    }

    @Test
    void hasNoResultWithoutRubbers() {
        assertThat(new MatchReport("TC Musterstadt 1", "TC Beispielhausen 1", null, List.of(), List.of()).hasResult())
                .isFalse();
    }
}
