package com.fairtix.venue.api;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fairtix.venues.application.VenueService;
import com.fairtix.venues.domain.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class VenueControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private VenueService venueService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // -------------------------------------------------------------------------
    // POST /api/venue — create venue
    // -------------------------------------------------------------------------

    void createVenue_asAdmin_returns200() throws Exception {
        String body = """
                        ""
                {
                    "Name":         "Test Venue",
                    "Address":      "105 Collie Way"
                }""";

        mockMvc.perform(post("/api/venues")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.name").value("Test Venue"))
                .andExpect(jsonPath("$.address").value("Test Address"));
    }

    void createVenue_asUser_returns403() throws Exception {
        String body = """
                {
                  "Name":     "Test Venue",
                  "Address":      "105 Collie Way"
                }
                """;
        mockMvc.perform(post("/api/venues")
                        .with(user("user@test.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    void createVenue_unauthenticated_returns403() throws Exception {
        String body = """
                {
                  "Name":     "Test Venue",
                  "Address":      "105 Collie Way"
                }
                """;

        mockMvc.perform(post("/api/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // GET /api/venues — list venues (permitAll)
    // -------------------------------------------------------------------------

    void listVenues_unauthenticated_returns200() throws Exception {
        venueService.createVenue("Test Venue", "105 Collie Way");

        mockMvc.perform(get("/api/venues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Test Venue"));
    }
    @Test
    void listVenues_pagination_works() throws Exception {
        venueService.createVenue("Venue A", "First Address");
        venueService.createVenue("Venue B", "Second Address");
        venueService.createVenue("Venue C", "Third Address");

        mockMvc.perform(get("/api/venues")
                        .param("page", "0")
                        .param("size", "2")
                        .param("Venues", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    // -------------------------------------------------------------------------
    // GET /api/venues/{id} — get by ID (permitAll)
    // -------------------------------------------------------------------------

    @Test
    void getVenue_existingId_returns200() throws Exception {
        Venue venue = venueService.createVenue("My Venue", "Address");

        mockMvc.perform(get("/api/venues/{id}", venue.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(venue.getId().toString()))
                .andExpect(jsonPath("$.title").value("My Venue"))
                .andExpect(jsonPath("$.venue").value("Address"));
    }

    @Test
    void getVenue_nonExistentId_returns404() throws Exception {
        mockMvc.perform(get("/api/venues/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(containsString("Venue not found")));
    }

}
