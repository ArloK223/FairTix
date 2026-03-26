package com.fairtix.venues.application;

import com.fairtix.venues.domain.Venue;
import com.fairtix.venues.infrastructure.VenueRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VenueService {
    private final VenueRepository repository;

    public VenueService(VenueRepository repository){
        this.repository = repository;
    }

    /**
     * Creates a new link that persists {@link Venue}
     *
     * @param name the name of the venue
     * @param address the address of the venue
     * @return a newly created event
     */
    public Venue createVenue(String name, String address){
        Venue venue = new Venue(name, address);
        return repository.save(venue);
    }

    /**
     *
     * @param id the id of the venue.
     * @throws IllegalArgumentException if the venue is not found.
     * @return the requested venue {@link Venue}
     */
    public Venue getVenue(UUID id){
        return repository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("Venue not found!"));
    }

    /**
     *
     * @param VenueName the name of a venue
     * @param Address the address of the venue
     * @param pageable determines if within a page.
     * @return finds venues within the pages.
     */
    public Page<Venue> findAll(String VenueName, String Address, Pageable pageable){
        return repository.findAll(pageable);
    }

    /**
     * Lists the venues as a whole.
     * @return all venues in the repository
     */
    public List<Venue> getAllVenues(){
        return repository.findAll();
    }
}
