package com.tennisstats.backend.api;

import com.tennisstats.backend.stats.OverviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OverviewController {

    private final OverviewService overviewService;

    public OverviewController(OverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GetMapping("/api/v1/overview")
    public Overview getOverview() {
        return Overview.from(overviewService.getOverview());
    }
}
