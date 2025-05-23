package com.epita.controller;

import com.epita.service.UsersService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for managing user-related operations in the Repo-Social service.
 * Provides endpoints for following, unfollowing, blocking, unblocking users, and retrieving user-related data.
 */
@Path("/api/repo-social/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsersController {

    @Inject
    UsersService usersService;

    /**
     * Follows a user.
     *
     * @param userId     the UUID of the user performing the follow action.
     * @param followeeId the UUID of the user to be followed.
     * @return a CREATED response if the operation is successful, or an error response if the action is forbidden or the user is not found.
     */
    @POST
    @Path("/follow")
    public Response followUser(@HeaderParam("X-user-id") UUID userId, UUID followeeId) {
        try {
            usersService.followUser(userId, followeeId);
            return Response
                    .status(Response.Status.CREATED)
                    .build();
        } catch (ForbiddenException e) {
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (NotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Unfollows a user.
     *
     * @param userId     the UUID of the user performing the unfollow action.
     * @param followeeId the UUID of the user to be unfollowed.
     * @return a NO_CONTENT response if the operation is successful, or a NOT_FOUND response if the user is not found.
     */
    @DELETE
    @Path("/unfollow")
    public Response unfollowUser(@HeaderParam("X-user-id") UUID userId, UUID followeeId) {
        try {
            usersService.unfollowUser(userId, followeeId);
            return Response
                    .status(Response.Status.NO_CONTENT)
                    .build();
        } catch (NotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Blocks a user.
     *
     * @param userId        the UUID of the user performing the block action.
     * @param blockedUserId the UUID of the user to be blocked.
     * @return a CREATED response if the operation is successful, or an error response if the action is forbidden or the user is not found.
     */
    @POST
    @Path("/block")
    public Response blockUser(@HeaderParam("X-user-id") UUID userId, UUID blockedUserId) {
        try {
            usersService.blockUser(userId, blockedUserId);
            return Response
                    .status(Response.Status.CREATED)
                    .build();
        } catch (ForbiddenException e) {
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (NotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Unblocks a user.
     *
     * @param userId        the UUID of the user performing the unblock action.
     * @param blockedUserId the UUID of the user to be unblocked.
     * @return a NO_CONTENT response if the operation is successful, or an error response if the action is forbidden or the user is not found.
     */
    @DELETE
    @Path("/unblock")
    public Response unblockUser(@HeaderParam("X-user-id") UUID userId, UUID blockedUserId) {
        try {
            usersService.unblockUser(userId, blockedUserId);
            return Response
                    .status(Response.Status.NO_CONTENT)
                    .build();
        } catch (NotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (ForbiddenException e) {
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Retrieves the posts liked by a user.
     *
     * @param userId the UUID of the user.
     * @return a list of liked posts or a NOT_FOUND response if the user is not found.
     */
    @GET
    @Path("/{userId}/likes")
    public Response getLikedPosts(@PathParam("userId") UUID userId) {
        try {
            return Response
                    .ok(usersService.getLikedPosts(userId))
                    .build();
        } catch (NotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Retrieves the users followed by a specific user.
     *
     * @param userId the UUID of the user.
     * @return a list of followees or a NOT_FOUND response if the user is not found.
     */
    @GET
    @Path("/{userId}/follows")
    public Response getFollowees(@PathParam("userId") UUID userId) {
        try {
            return Response
                    .ok(usersService.getFollowees(userId))
                    .build();
        } catch (NotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Retrieves the followers of a specific user.
     *
     * @param userId the UUID of the user.
     * @return a list of followers or a NOT_FOUND response if the user is not found.
     */
    @GET
    @Path("/{userId}/followers")
    public Response getFollowers(@PathParam("userId") UUID userId) {
        try {
            return Response
                    .ok(usersService.getFollowers(userId))
                    .build();
        } catch (NotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Retrieves the users blocked by a specific user.
     *
     * @param userId the UUID of the user.
     * @return a list of blocked users or a NOT_FOUND response if the user is not found.
     */
    @GET
    @Path("/{userId}/blocks")
    public Response getBlockedUsers(@PathParam("userId") UUID userId) {
        try {
            return Response
                    .ok(usersService.getBlockedUsers(userId))
                    .build();
        } catch (NotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Retrieves the users who have blocked a specific user.
     *
     * @param userId the UUID of the user.
     * @return a list of users who blocked the specified user or a NOT_FOUND response if the user is not found.
     */
    @GET
    @Path("/{userId}/blocked")
    public Response getUsersWhoBlockedMe(@PathParam("userId") UUID userId) {
        try {
            return Response
                    .ok(usersService.getUsersWhoBlockedMe(userId))
                    .build();
        } catch (NotFoundException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }
}
