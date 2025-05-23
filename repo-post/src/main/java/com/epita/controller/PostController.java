package com.epita.controller;

import com.epita.common.api.response.PostIdResponse;
import com.epita.common.api.response.PostResponse;
import com.epita.common.api.response.UserResponse;
import com.epita.controller.contracts.FileData;
import com.epita.controller.contracts.PostRequestMultiPart;
import com.epita.controller.contracts.PostRequestContract;
import com.epita.repository.entity.User;
import com.epita.service.FileStorageService;
import com.epita.service.PostService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing posts and related operations.
 * Provides endpoints for creating, retrieving, updating, and deleting posts.
 */
@Path("/api")
public class PostController {
    @Inject
    PostService _postService;

    @Inject
    FileStorageService _fileStorageService;

    @Inject
    User user;

    private final Logger LOG = Logger.getLogger(PostController.class);

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
     * Retrieves a post by its ID.
     *
     * @param postId the UUID of the post to retrieve.
     * @return the post details or a NOT_FOUND response if the post does not exist.
     */
    @GET
    @Path("/posts/{postId}")
    public Response getPost(@PathParam("postId") final UUID postId) {
        try {
            return Response
                    .ok(_postService.getPost(postId))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

    /**
     * Retrieves all posts by a specific user.
     *
     * @param userId the UUID of the user.
     * @return a list of posts or a NOT_FOUND response if the user does not exist.
     */
    @GET
    @Path("/users/{userId}/posts")
    public Response getPostsByUser(@PathParam("userId") final UUID userId) {
        try {

            List<PostResponse> posts = _postService.getPostsByUser(userId);
            return Response
                    .ok(posts)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId the UUID of the user to retrieve.
     * @return the user details or a BAD_REQUEST/NOT_FOUND response if the user does not exist.
     */
    @GET
    @Path("/users/{userId}")
    public Response getUser(@PathParam("userId") final UUID userId) {
        if (userId == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("User ID must not be null")
                    .build();
        }
        try {
            UserResponse user = _postService.getUserById(userId);
            return Response
                    .ok(user)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

    /**
     * Retrieves replies to a specific post.
     *
     * @param postId the UUID of the post.
     * @return a list of replies or a NOT_FOUND response if the post does not exist.
     */
    @GET
    @Path("/posts/{postId}/replies")
    public Response getReplies(@PathParam("postId") final UUID postId) {
        try {
            _postService.getPost(postId);
        } catch (IllegalArgumentException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
        List<PostResponse> replies = _postService.getPostReplies(postId);
        return Response
                .ok(replies)
                .build();
    }

    /**
     * Retrieves reposts of a specific post.
     *
     * @param postId the UUID of the post.
     * @return a list of reposts or a NOT_FOUND response if the post does not exist.
     */
    @GET
    @Path("/posts/{postId}/reposts")
    public Response getReposts(@PathParam("postId") final UUID postId) {
        try {
            _postService.getPost(postId);
        } catch (IllegalArgumentException e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
        List<PostResponse> reposts = _postService.getPostReposts(postId);
        return Response
                .ok(reposts)
                .build();
    }

    /**
     * Retrieves the media associated with a specific post.
     *
     * @param postId the UUID of the post.
     * @return the media file or a NOT_FOUND/INTERNAL_SERVER_ERROR response if an error occurs.
     */
    @GET
    @Path("/posts/{postId}/media")
    public Response getPostMedia(@PathParam("postId") final UUID postId) {
        try {
            PostResponse post = _postService.getPost(postId);
            if (post.getMedia() == null) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("No media found for this post : " + postId)
                        .build();
            }
            FileData file = _fileStorageService.getFile(post.getMedia());
            return Response
                    .ok(file
                            .getInputStream()
                            .readAllBytes())
                    .type(file.getContentType())
                    .header("Content-Disposition", "attachment; filename=\"" + file.getFileName() + "\"")
                    .build();
        } catch (WebApplicationException e) {
            if (e
                    .getResponse()
                    .getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(e.getMessage())
                        .build();
            } else {
                return Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("An unexpected error occurred when getting file")
                        .build();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new post.
     *
     * @param userId       the UUID of the user creating the post.
     * @param postsRequest the post request containing the post details.
     * @return a CREATED response with the post ID or an error response if validation fails.
     */
    @POST
    @Path("/posts")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createPost(@HeaderParam("X-user-id") UUID userId, PostRequestMultiPart postsRequest) {
        if (userId == null || postsRequest == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("User ID and post content must not be null")
                    .build();
        }

        // Check that replyId and repostId, if not empty/null, are valid UUIDs
        UUID replyId = null;
        UUID repostId = null;
        if (postsRequest.replyId != null && !postsRequest.replyId.isEmpty()) {
            try {
                replyId = UUID.fromString(postsRequest.replyId);
            } catch (IllegalArgumentException e) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("Invalid reply ID")
                        .build();
            }
        }
        if (postsRequest.repostId != null && !postsRequest.repostId.isEmpty()) {
            try {
                repostId = UUID.fromString(postsRequest.repostId);
            } catch (IllegalArgumentException e) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("Invalid repost ID")
                        .build();
            }
        }
        try {

            // Store the file if it exists:
            LOG.info("Trying to store file: ");
            _postService.canPost(replyId, repostId, userId);
            String mediaId = _fileStorageService.storeFile(postsRequest.media);
            PostIdResponse postIdResponse = _postService.createPost(userId, new PostRequestContract(postsRequest.text, mediaId, repostId, replyId));
            return Response
                    .status(Response.Status.CREATED)
                    .entity(postIdResponse)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();

        } catch (WebApplicationException e) {
            if (e
                    .getResponse()
                    .getStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
                return Response
                        .status(Response.Status.FORBIDDEN)
                        .entity("Cannot create post because the user is blocked")
                        .build();
            } else if (e
                    .getResponse()
                    .getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(e.getMessage())
                        .build();
            } else if (e
                    .getResponse()
                    .getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode()) {
                return Response
                        .status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity("RepoSocial request failed: " + e.getLocalizedMessage())
                        .build();
            } else {
                Log.info(e
                        .getResponse()
                        .getStatus());
                return Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("An unexpected error occurred: " + e.getMessage())
                        .build();
            }
        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An unexpected error occurred" + e.getMessage())
                    .build();
        }
    }

    /**
     * Deletes a post by its ID.
     *
     * @param userId the UUID of the user attempting to delete the post.
     * @param postId the UUID of the post to delete.
     * @return a NO_CONTENT response if successful or an error response if validation fails.
     */
    @DELETE
    @Path("/posts/{postId}")
    public Response deletePost(@HeaderParam("X-user-id") UUID userId, @PathParam("postId") final UUID postId) {
        if (userId == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("User ID must not be null")
                    .build();
        }
        try {
            PostResponse post = _postService.getPost(postId);
            _postService.deletePost(userId, postId);
            // Delete the file if it exists:
            if (post.getMedia() != null) {
                _fileStorageService.deleteFile(post.getMedia());
            }

            return Response
                    .status(Response.Status.NO_CONTENT)
                    .build();
        } catch (WebApplicationException e) {
            if (e
                    .getResponse()
                    .getStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
                return Response
                        .status(Response.Status.FORBIDDEN)
                        .entity("User forbidden from deleting this post")
                        .build();
            } else if (e
                    .getResponse()
                    .getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(e.getMessage())
                        .build();
            } else {
                return Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("An unexpected error occurred")
                        .build();
            }
        }
    }
}
