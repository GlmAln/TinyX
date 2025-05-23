package com.epita.controller;

import com.epita.controller.contract.PostsContract;
import com.epita.service.PostsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

/**
 * REST controller for managing posts in the Repo-Social service.
 * Provides endpoints for liking, unliking, and retrieving likes for posts.
 */
@Path("/api/repo-social/posts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PostsController {

    @Inject
    PostsService postsService;

    /**
     * Endpoint to return a simple hello message.
     *
     * @return a "Word" string.
     */
    @GET
    @Path("/hello")
    public String hello() {
        return "Word";
    }

    /**
     * Likes a post for a specific user.
     *
     * @param userId  the UUID of the user liking the post.
     * @param request the request containing the post ID to like.
     * @return a CREATED response if the operation is successful, or an error response if the user is blocked or the post is not found.
     */
    @POST
    @Path("/like")
    public Response likePost(@HeaderParam("X-user-id") UUID userId, PostsContract request) {
        try {
            postsService.likePost(userId, request.getPostId());
            return Response
                    .status(Response.Status.CREATED)
                    .build();
        } catch (ForbiddenException e) {
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity("User is blocked")
                    .build();
        } catch (NotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("Post not found")
                    .build();
        }
    }

    /**
     * Unlikes a post for a specific user.
     *
     * @param userId  the UUID of the user unliking the post.
     * @param request the request containing the post ID to unlike.
     * @return a NO_CONTENT response if the operation is successful, or a NOT_FOUND response if the like is not found.
     */
    @DELETE
    @Path("/unlike")
    public Response unlikePost(@HeaderParam("X-user-id") UUID userId, PostsContract request) {
        try {
            postsService.unlikePost(userId, request.getPostId());
            return Response
                    .status(Response.Status.NO_CONTENT)
                    .build();
        } catch (NotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("Like not found")
                    .build();
        }
    }

    /**
     * Retrieves the likes for a specific post.
     *
     * @param postId the UUID of the post to retrieve likes for.
     * @return a NO_CONTENT response if the operation is successful, or a NOT_FOUND response if the likes are not found.
     */
    @GET
    @Path("/{postId}/likes")
    public Response getLikesOfPostId(@PathParam("postId") UUID postId) {
        try {
            postsService.getLikesOfPostId(postId);
            return Response
                    .status(Response.Status.NO_CONTENT)
                    .build();
        } catch (NotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("Like not found")
                    .build();
        }
    }

}
