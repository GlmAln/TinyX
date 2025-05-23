package com.epita.controller.api;

import com.epita.service.SearchService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;

/**
 * REST controller for handling search operations.
 * Provides an endpoint to search for posts based on specified terms.
 */
@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SearchController {

    @Inject
    SearchService searchService;

    /**
     * Endpoint to search for posts based on the provided search terms.
     *
     * @param terms the search terms provided in the "X-Terms" header.
     * @return a response containing the search results or an error response if the terms are invalid.
     * @throws IOException if an error occurs during the search operation.
     */
    @GET
    public Response searchEndpoint(@HeaderParam("X-Terms") String terms) throws IOException {
        if (terms == null || terms
                .trim()
                .isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("The query parameter 'terms' is required")
                    .build();
        }

        return Response
                .ok(searchService.fetchPosts(terms))
                .build();
    }
}
