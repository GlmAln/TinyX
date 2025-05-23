package com.epita.service;

import com.epita.common.api.response.PostIdResponse;
import com.epita.common.api.response.UserResponse;
import com.epita.common.command.PostEventCommand;
import com.epita.common.utils.ErrorCode;
import com.epita.controller.contracts.PostRequestContract;
import com.epita.repository.PostEventPublisher;
import com.epita.repository.PostRepository;
import com.epita.repository.RepoSocialRestClient;
import com.epita.repository.UserRepository;
import com.epita.repository.entity.Post;
import com.epita.repository.entity.User;
import jakarta.inject.Inject;

import jakarta.enterprise.context.ApplicationScoped;

import com.epita.common.api.response.PostResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing posts in the Repo-Social service.
 * Provides methods for creating, retrieving, deleting, and validating posts.
 */
@ApplicationScoped
public class PostService {

    @Inject
    PostRepository postRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    PostEventPublisher postEventPublisher;

    @Inject
    RepoSocialRestClient repoSocialRestClient;

    private static final Logger LOG = LoggerFactory.getLogger(PostService.class);

    /**
     * Retrieves a post by its ID.
     *
     * @param postId the UUID of the post to retrieve.
     * @return the post response containing post details.
     */
    public PostResponse getPost(UUID postId) {
        LOG.info("Fetching post with ID: {}", postId);
        Post post = postRepository.getPostById(postId);
        if (post == null)
            handlePostNotFound(postId);
        LOG.info("Post with ID {} fetched successfully", postId);
        return new PostResponse(post.getId(), post.getAuthorId(), post.getText(), post.getMediaFileId(), post.getRepostId(), post.getReplyId(), post.getCreationDate());
    }

    /**
     * Retrieves all posts created by a specific user.
     *
     * @param userId the UUID of the user whose posts are to be retrieved.
     * @return a list of post responses.
     */
    public List<PostResponse> getPostsByUser(UUID userId) {
        LOG.info("Fetching posts for user with ID: {}", userId);
        User user = userRepository.getUserById(userId);
        if (user == null)
            handleUserNotFound(userId);
        List<Post> posts = postRepository.getUserPosts(userId);
        LOG.info("Fetched {} posts for user with ID: {}", posts.size(), userId);
        return posts
                .stream()
                .map(post -> new PostResponse(
                        post.getId(),
                        post.getAuthorId(),
                        post.getText(),
                        post.getMediaFileId(),
                        post.getRepostId(),
                        post.getReplyId(),
                        post.getCreationDate()
                ))
                .toList();
    }

    /**
     * Retrieves replies to a specific post.
     *
     * @param postId the UUID of the post whose replies are to be retrieved.
     * @return a list of post responses representing the replies.
     */
    public List<PostResponse> getPostReplies(UUID postId) {
        LOG.info("Fetching replies for post with ID: {}", postId);
        List<Post> replies = postRepository.getPostReplies(postId);
        LOG.info("Fetched {} replies for post with ID: {}", replies.size(), postId);
        return replies
                .stream()
                .map(post -> new PostResponse(
                        post.getId(),
                        post.getAuthorId(),
                        post.getText(),
                        post.getMediaFileId(),
                        post.getRepostId(),
                        post.getReplyId(),
                        post.getCreationDate()
                ))
                .toList();
    }

    /**
     * Retrieves reposts of a specific post.
     *
     * @param postId the UUID of the post whose reposts are to be retrieved.
     * @return a list of post responses representing the reposts.
     */
    public List<PostResponse> getPostReposts(UUID postId) {
        LOG.info("Fetching reposts for post with ID: {}", postId);
        List<Post> reposts = postRepository.getPostReposts(postId);
        LOG.info("Fetched {} reposts for post with ID: {}", reposts.size(), postId);
        return reposts
                .stream()
                .map(post -> new PostResponse(
                        post.getId(),
                        post.getAuthorId(),
                        post.getText(),
                        post.getMediaFileId(),
                        post.getRepostId(),
                        post.getReplyId(),
                        post.getCreationDate()
                ))
                .toList();
    }

    /**
     * Deletes a post by its ID.
     *
     * @param userId the UUID of the user attempting to delete the post.
     * @param postId the UUID of the post to delete.
     */
    public void deletePost(UUID userId, UUID postId) {
        LOG.info("Deleting post with ID: {} by user with ID: {}", postId, userId);
        Post post = postRepository.getPostById(postId);
        if (post == null)
            handlePostNotFound(postId);
        if (!post
                .getAuthorId()
                .equals(userId)) {
            LOG.warn("User with ID {} is not authorized to delete post with ID: {}", userId, postId);
            ErrorCode.FORBIDDEN_ACTION.throwException(userId);
        }
        postRepository.deletePost(postId);
        LOG.info("Post with ID {} deleted successfully", postId);

        PostEventCommand postDeletion = new PostEventCommand(PostEventCommand.Type.DELETION, postId, userId, Optional.ofNullable(post.getText()), post.getCreationDate());
        postEventPublisher.publish(postDeletion);
        LOG.info("Post deletion event published for post ID: {}", postId);
    }

    /**
     * Validates whether a user can create a post, considering reply and repost constraints.
     *
     * @param replyId the UUID of the post being replied to, if applicable.
     * @param repostId the UUID of the post being reposted, if applicable.
     * @param userId the UUID of the user attempting to create the post.
     */
    public void canPost(UUID replyId, UUID repostId, UUID userId) {
        LOG.info("Checking if user with ID: {} can post", userId);
        if (replyId != null || repostId != null) {
            try {
                if (replyId != null) {
                    Post replyPost = postRepository.getPostById(replyId);
                    if (replyPost == null) {
                        LOG.warn("Reply post with ID {} not found", replyId);
                        ErrorCode.POST_NOT_FOUND.throwException(replyId);
                    }
                }
                if (repostId != null) {
                    Post repostPost = postRepository.getPostById(repostId);
                    if (repostPost == null) {
                        LOG.warn("Repost with ID {} not found", repostId);
                        ErrorCode.POST_NOT_FOUND.throwException(repostId);
                    }
                }

                UUID authorId = replyId != null ? postRepository
                        .getPostById(replyId)
                        .getAuthorId() : postRepository
                        .getPostById(repostId)
                        .getAuthorId();
                if (repoSocialRestClient.doesUserBlock(userId, authorId) || repoSocialRestClient.isUserBlocked(userId, authorId)) {
                    LOG.warn("User with ID {} is blocked or blocks user with ID: {}", userId, authorId);
                    ErrorCode.POST_CREATION_FORBIDDEN.throwException(userId, authorId);
                }
            } catch (Exception e) {
                LOG.warn("Caught exception while checking if user is blocked: {}", e.getMessage());
                ErrorCode.REPO_SOCIAL_FAILED.throwException(e.getMessage());
            }
        }
        LOG.info("User with ID: {} can post", userId);
    }

    /**
     * Creates a new post.
     *
     * @param userId the UUID of the user creating the post.
     * @param post the post request contract containing post details.
     * @return the ID of the created post.
     */
    public PostIdResponse createPost(UUID userId, PostRequestContract post) {
        LOG.info("Creating post for user with ID: {}", userId);
        if (post.getText() == null && post.getMediaId() == null && post.getRepostId() == null) {
            LOG.warn("Post must contain at least one of (text, media, repost)");
            throw new IllegalArgumentException("Post must contain at least one of (text, media, repost)");
        }
        if (post.getText() != null && post.getMediaId() != null && post.getRepostId() != null) {
            LOG.warn("Post must contain at most two of (text, media, repost)");
            throw new IllegalArgumentException("Post must contain at most two of (text, media, repost)");
        }

        if (post.getText() != null && post
                .getText()
                .length() > 160) {
            LOG.warn("Post text must not exceed 160 characters");
            throw new IllegalArgumentException("Post text must not exceed 160 characters");
        }

        User user = userRepository.getUserById(userId);
        if (user == null) {
            LOG.info("User with ID {} not found, creating new user", userId);
            userRepository.createUser(new User(userId, ""));
        }

        Post newPost = new Post(
                userId,
                post.getText(),
                post.getMediaId(),
                post.getRepostId(),
                post.getReplyId());
        UUID postId = postRepository.createPost(newPost);
        LOG.info("Post created with ID: {}", postId);

        PostEventCommand postCreation = new PostEventCommand(PostEventCommand.Type.CREATION, postId, userId, Optional.ofNullable(post.getText()), newPost.getCreationDate());
        postEventPublisher.publish(postCreation);
        LOG.info("Post creation event published for post ID: {}", postId);

        return new PostIdResponse(postId);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId the UUID of the user to retrieve.
     * @return the user response containing user details.
     */
    public UserResponse getUserById(UUID userId) {
        LOG.info("Fetching user with ID: {}", userId);
        User user = userRepository.getUserById(userId);
        if (user == null)
            handleUserNotFound(userId);
        LOG.info("User with ID {} fetched successfully", userId);
        return new UserResponse(user.getId(), user.getName());
    }

    /**
     * Handles the scenario where a user is not found.
     * Logs a warning and throws a USER_NOT_FOUND exception.
     *
     * @param userId the UUID of the user that was not found.
     */
    private void handleUserNotFound(UUID userId) {
        LOG.warn("User with ID {} not found", userId);
        ErrorCode.USER_NOT_FOUND.throwException(userId);
    }

    /**
     * Handles the scenario where a post is not found.
     * Logs a warning and throws a POST_NOT_FOUND exception.
     *
     * @param postId the UUID of the post that was not found.
     */
    private void handlePostNotFound(UUID postId) {
        LOG.warn("Post with ID {} not found", postId);
        ErrorCode.POST_NOT_FOUND.throwException(postId);
    }
}
