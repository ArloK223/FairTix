package com.fairtix.venues.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import com.fairtix.venues.application.VenueService;
import com.fairtix.venues.domain.Venue;
import com.fairtix.venues.dto.CreateVenueRequest;
import com.fairtix.venues.dto.VenueResponse;

import jakarta.annotation.security.PermitAll;

import java.util.UUID;



@RestController
@RequestMapping("/api/venues")
public class VenueController {
    private final VenueService service;
    public VenueController(VenueService service){
        this.service = service;
    }

    /**
     * createVenue is responsible for creating new venues as necessary.
     * Accepts a JSON request body that contains the venue's details.
     * @param request the venue as a json payload
     * @return the newly created venue.
     */

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public VenueResponse createVenue(@RequestBody CreateVenueRequest request){
        Venue venue = service.createVenue(request.name(), request.address());
        return VenueResponse.from(venue);
    }

    /**
     * Take the details about the venues requested and returns information about that venue.
     * @param VenueName the name of the venue.
     * @param Address the address of that venue.
     * @param page the page number.
     * @param size number of items per page.
     * @return the requested page.
     */

    @PermitAll
    @GetMapping
    public Page<VenueResponse> list(
            @RequestParam(required = false) String VenueName,
            @RequestParam(required = false) String Address,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Venue> venues = service.findAll(VenueName, Address, PageRequest.of(page, Math.min(size, 100)));
        return (Page<VenueResponse>) venues.map(VenueResponse::from);
    }


    /**
     * Gets an individual venue based on its id
     * @param id the id of the venue
     * @return the requested venue which matches the id.
     */
    public VenueResponse getVenue(@PathVariable UUID id) {
        return VenueResponse.from(service.getVenue(id));
    }

}
