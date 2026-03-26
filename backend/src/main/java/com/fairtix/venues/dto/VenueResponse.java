
package com.fairtix.venues.dto;
import java.time.Instant;
import java.util.UUID;

import com.fairtix.venues.domain.Venue;

/**
 * A response payload for a venue
 *
 * Returns by venue api endpoints.
 * @param id the unique id of a venue
 * @param name the name of the venue
 * @param address the address of the venue.
 */

public record VenueResponse (UUID id, String name, String address) {
    /**
     * Maps a {@link Venue} object to an API response.
     * @param venue the venue entity
     * @return the corresponding {@link VenueResponse}
     */
    public static VenueResponse from(Venue venue){
        return new VenueResponse(venue.getId(), venue.getName(), venue.getAddress());
    }
}
