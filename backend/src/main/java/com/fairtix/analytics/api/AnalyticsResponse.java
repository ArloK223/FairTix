package com.fairtix.analytics.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AnalyticsResponse(
    OverviewStats overview,
    List<VenueCount> eventsByVenue,
    Map<String, Long> seatsByStatus,
    List<EventInventory> topEventsByBookings,
    Map<String, Long> holdsByStatus,
    double holdConfirmationRate,
    List<DailyHoldCount> holdsPerDay,
    Map<String, Long> usersByRole
) {

  public record OverviewStats(
      long totalEvents,
      long upcomingEvents,
      long totalUsers,
      long totalSeats,
      long bookedSeats,
      long activeHolds
  ) {}

  public record VenueCount(String venue, long count) {}

  public record EventInventory(UUID eventId, String eventTitle, long available, long held, long booked) {}

  public record DailyHoldCount(String date, long count) {}
}
