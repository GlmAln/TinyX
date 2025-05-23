package com.epita.controller.api;

import com.epita.service.UserTimelineService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing user timelines in the Repo-Social service.
 * Provides an endpoint to retrieve timelines for a list of users.
 */
@Path("/api")
public class UserTimelineController {

    @Inject
    UserTimelineService userTimelineService;

    /**
     * Retrieves the timelines for a list of users.
     *
     * @param userIds the list of UUIDs representing the users whose timelines are to be retrieved.
     * @return a response containing the user timelines or an error response if the operation fails.
     */
    @GET
    @Path("/users/timeline")
    public Response getUserTimelines(@RestQuery("userIds") List<UUID> userIds) {
        return Response
                .ok(userTimelineService
                        .getUserTimelines(userIds))
                .build();
    }
}
