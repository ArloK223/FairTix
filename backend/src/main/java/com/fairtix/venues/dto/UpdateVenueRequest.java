package com.fairtix.venues.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateVenueRequest(
        @NotBlank @Size(max = 500) String name,
        @NotBlank @Size(max = 500) String address)
       {

}