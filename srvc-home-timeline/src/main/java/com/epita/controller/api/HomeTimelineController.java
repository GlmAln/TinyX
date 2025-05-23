package com.epita.controller.api;

import com.epita.service.HomeTimelineService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

/**
 * REST controller for managing the home timeline in the Repo-Social service.
 * Provides an endpoint to retrieve the home timeline for a specific user.
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HomeTimelineController {

    @Inject
    HomeTimelineService homeTimelineService;

    /**
     * Retrieves the home timeline for a specific user.
     *
     * @param userId the UUID of the user whose home timeline is to be retrieved.
     * @return a response containing the home timeline or an error response if the user is not found.
     */
    @GET
    @Path("/users/{userId}/home-timeline")
    public Response getHomeTimeline(@PathParam("userId") UUID userId) {
        return Response
                .ok(homeTimelineService.getHomeTimeline(userId))
                .build();
    }
}
