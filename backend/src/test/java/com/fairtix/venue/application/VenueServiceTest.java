package com.fairtix.venue.application;

import com.fairtix.common.ResourceNotFoundException;
import com.fairtix.venues.application.VenueService;
import com.fairtix.venues.domain.Venue;
import com.fairtix.venues.dto.UpdateVenueRequest;
import com.fairtix.venues.infrastructure.VenueRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class VenueServiceTest {
    @Autowired
    private VenueService venueService;

    @Autowired
    private VenueRepository venueRepository;

    private Venue testVenue;

    @BeforeEach
    void setUp(){
        testVenue = venueRepository.save(new Venue("Test Venue", "Test Address"));
    }

    // -------------------------------------------------------------------------
    // Create Venue
    // -------------------------------------------------------------------------

    @Test
    void creatingVenueSucceeds() {

        Venue venue = venueService.createVenue(
                "New Venue",
                "New Address");

        assertThat(venue.getId()).isNotNull();
        assertThat(venue.getName()).isEqualTo("New Vnue");
        assertThat(venue.getAddress()).isEqualTo("New Address");
    }

    // -------------------------------------------------------------------------
    // Get Venue
    // -------------------------------------------------------------------------

    @Test
    void gettingExistingVenueReturnsVenue() {

        Venue venue = venueService.getVenue(testVenue.getId());

        assertThat(venue.getId()).isEqualTo(testVenue.getId());
        assertThat(venue.getAddress()).isEqualTo("Test Venue");
    }

    @Test
    void gettingNonexistentVenueThrowsException() {

        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> venueService.getVenue(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Venue not found");
    }

    // -------------------------------------------------------------------------
    // Update Venue
    // -------------------------------------------------------------------------

    @Test
    void updatingVenueChangesNameAndAddress() {

        UpdateVenueRequest request = new UpdateVenueRequest("Updated Venue", "Updated Address");

        Venue updated = venueService.update(testVenue.getId(), request);

        assertThat(updated.getName()).isEqualTo("Updated Event");
        assertThat(updated.getAddress()).isEqualTo("Updated Address");
    }

    @Test
    void updatingNonexistentEventThrowsException() {

        UpdateVenueRequest request = new UpdateVenueRequest("Updated", "Updated");

        assertThatThrownBy(() -> venueService.update(UUID.randomUUID(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Venue not found");
    }
    // -------------------------------------------------------------------------
    // Delete Venue
    // -------------------------------------------------------------------------

    @Test
    void deletingExistingVenueRemovesIt() {

        UUID id = testVenue.getId();

        venueService.delete(id);

        assertThat(venueRepository.findById(id)).isEmpty();
    }

    @Test
    void deletingNonexistentVenueThrowsException() {

        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> venueService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Venue not found");
    }

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    @Test
    void searchingByVenueReturnsMatchingVenue() {

        venueRepository.save(
                new Venue("Another Venue", "Another Address"));

        Page<Venue> results = venueService.search(
                "Test Venue",
                null,
                PageRequest.of(0, 10));

        assertThat(results.getContent())
                .extracting(Venue::getName)
                .allMatch(v -> v.toLowerCase().contains("test venue"));
    }

    @Test
    void searchingByTitleReturnsMatchingEvents() {

        Page<Venue> results = venueService.search(
                null,
                "Test",
                PageRequest.of(0, 10));

        assertThat(results.getContent())
                .extracting(Venue::getName)
                .allMatch(t -> t.toLowerCase().contains("test"));

    }

}
