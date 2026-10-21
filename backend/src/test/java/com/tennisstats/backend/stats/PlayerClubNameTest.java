package com.tennisstats.backend.stats;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PlayerClubNameTest {

    @Test
    void stripsTheTrailingTeamNumber() {
        assertThat(PlayerClubService.clubName("TC Weinheim 1902 2")).isEqualTo("TC Weinheim 1902");
        assertThat(PlayerClubService.clubName("TC BW 64 Leimen 1")).isEqualTo("TC BW 64 Leimen");
        assertThat(PlayerClubService.clubName("TC 70 Sandhausen 1")).isEqualTo("TC 70 Sandhausen");
        assertThat(PlayerClubService.clubName("TC RW Wiesloch 12")).isEqualTo("TC RW Wiesloch");
    }

    @Test
    void keepsANameWithoutATrailingTeamNumber() {
        assertThat(PlayerClubService.clubName("TC Hockenheim")).isEqualTo("TC Hockenheim");
        assertThat(PlayerClubService.clubName("TC Weinheim 1902")).isEqualTo("TC Weinheim 1902");
    }

    @Test
    void trimsWhitespace() {
        assertThat(PlayerClubService.clubName("  TC Plankstadt 1  ")).isEqualTo("TC Plankstadt");
        assertThat(PlayerClubService.clubName(" TC Plankstadt ")).isEqualTo("TC Plankstadt");
    }
}
