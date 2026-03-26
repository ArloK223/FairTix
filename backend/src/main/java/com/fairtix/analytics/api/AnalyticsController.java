package com.fairtix.analytics.api;

import com.fairtix.analytics.application.AnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

  private final AnalyticsService analyticsService;

  public AnalyticsController(AnalyticsService analyticsService) {
    this.analyticsService = analyticsService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/dashboard")
  public AnalyticsResponse getDashboardAnalytics() {
    return analyticsService.getDashboardAnalytics();
  }
}
