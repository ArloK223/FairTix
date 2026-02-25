package com.fairtix.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Request payload for creating a seat hold.
 *
 * @param seatIds         the seats to hold
 * @param holderId        opaque identifier for the holder (session/user id)
 * @param durationMinutes how long the hold lasts; uses the server default when
 *                        null
 */
public record CreateHoldRequest(
    @NotEmpty(message = "At least one seat is required")
    @Size(max = 10, message = "Cannot request more than 10 seats per hold")
    List<UUID> seatIds,
    @NotBlank(message = "holderId must not be blank") String holderId,
    @Min(value = 1, message = "durationMinutes must be at least 1") Integer durationMinutes) {
}
