package com.epita.service;

import com.epita.repository.Neo4jRepository;
import com.epita.repository.LikeEventPublisher;
import com.epita.repository.RepoPostRestClient;
import com.epita.repository.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.sql.Timestamp;

/**
 * Service for managing posts and likes in the Repo-Social service.
 * Provides methods to like, unlike, and retrieve likes for posts, as well as verifying user and post existence.
 */
@ApplicationScoped
public class PostsService {

    private static final Logger LOG = LoggerFactory.getLogger(PostsService.class);

    @Inject
    public Neo4jRepository neo4jRepository;

    @Inject
    public LikeEventPublisher likeEventPublisher;

    @Inject
    public RepoPostRestClient repoPostRestClient;

    /**
     * Like a post for a given user
     *
     * @param userId The ID of the user liking the post
     * @param postId The ID of the post to like
     * @throws ForbiddenException If the user is blocked
     * @throws NotFoundException  If the post does not exist
     */
    public void likePost(UUID userId, UUID postId) {
        LOG.info("Attempting to like post. User ID: {}, Post ID: {}", userId, postId);
        if (userId == null || postId == null) {
            LOG.error("Invalid request. User ID or Post ID is null.");
            throw new IllegalArgumentException("Invalid request");
        }

        UsersSocial user = new UsersSocial(userId);
        PostsSocial post = new PostsSocial(postId);

        if (!postExistVerify(post)) {
            LOG.error("Post not found. Post ID: {}", postId);
            throw new NotFoundException("Post not found");
        }

        if (!userExistVerify(user)) {
            LOG.error("User not found. User ID: {}", userId);
            throw new NotFoundException("User not found");
        }

        if (isUserBlocked(user, postId)) {
            LOG.error("User is blocked. User ID: {}, Post ID: {}", userId, postId);
            throw new ForbiddenException("User is blocked");
        }

        if (neo4jRepository.likeRelationExists(user, post)) {
            LOG.info("Like relationship already exists. User ID: {}, Post ID: {}", userId, postId);
            return;
        }

        neo4jRepository.createLikeRelation(user, post);
        LOG.info("Created like relationship. User ID: {}, Post ID: {}", userId, postId);

        Timestamp e = Timestamp.from(Instant.now());
        LikeEvent event = new LikeEvent(userId, postId, TypeLikes.LIKE, e);
        likeEventPublisher.publish(event);
        LOG.info("Published like event. User ID: {}, Post ID: {}", userId, postId);
    }


    /**
     * Unlike a post for a given user
     *
     * @param userId The ID of the user unliking the post
     * @param postId The ID of the post to unlike
     * @throws NotFoundException If the like does not exist
     */
    public void unlikePost(UUID userId, UUID postId) {
        LOG.info("Attempting to unlike post. User ID: {}, Post ID: {}", userId, postId);
        if (userId == null || postId == null) {
            LOG.error("Invalid request. User ID or Post ID is null.");
            throw new IllegalArgumentException("Invalid request");
        }

        UsersSocial user = new UsersSocial(userId);
        PostsSocial post = new PostsSocial(postId);

        if (!neo4jRepository.likeRelationExists(user, post)) {
            LOG.error("Like relationship not found. User ID: {}, Post ID: {}", userId, postId);
            throw new NotFoundException("Like not found");
        }

        neo4jRepository.removeLikeRelation(user, post);
        LOG.info("Removed like relationship. User ID: {}, Post ID: {}", userId, postId);

        Timestamp e = Timestamp.from(Instant.now());
        LikeEvent event = new LikeEvent(userId, postId, TypeLikes.UNLIKE, e);
        likeEventPublisher.publish(event);
        LOG.info("Published unlike event. User ID: {}, Post ID: {}", userId, postId);
    }

    /**
     * Get all users who liked a specific post
     *
     * @param postId The ID of the post
     * @return List of user IDs who liked the post
     * @throws NotFoundException If the post does not exist
     */
    public List<UsersSocial> getLikesOfPostId(UUID postId) {
        LOG.info("Fetching likes for post. Post ID: {}", postId);
        PostsSocial post = new PostsSocial(postId);

        if (!postExistVerify(post)) {
            LOG.error("Post not found. Post ID: {}", postId);
            throw new NotFoundException("Post not found");
        }

        List<UsersSocial> likers = neo4jRepository.getAllLikersFromPost(post);
        LOG.info("Found {} likers for Post ID: {}", likers.size(), postId);
        return likers;
    }

    /**
     * Checks if a user is blocked by the author of a post.
     *
     * @param userId1 The user performing the action.
     * @param postId  The ID of the post.
     * @return True if the user is blocked, false otherwise.
     */
    public Boolean isUserBlocked(UsersSocial userId1, UUID postId) {
        LOG.debug("Checking if user is blocked. User ID: {}, Post ID: {}", userId1.getId(), postId);
        RepoPostResponse userId = repoPostRestClient.getPostById(postId);
        UsersSocial authorId = new UsersSocial(userId.getAuthorId());
        boolean isBlocked = neo4jRepository.blockRelationExists(userId1, authorId) || neo4jRepository.blockRelationExists(authorId, userId1);
        LOG.debug("Block check result for User ID: {}, Post ID: {}: {}", userId1.getId(), postId, isBlocked);
        return isBlocked;
    }


    /**
     * Verifies if a post exists in the repository.
     * If not, it attempts to fetch and add it to the repository.
     *
     * @param post The post to verify.
     * @return True if the post exists, false otherwise.
     */
    public boolean postExistVerify(PostsSocial post) {
        LOG.debug("Verifying existence of post. Post ID: {}", post.getId());
        if (!neo4jRepository.postExists(post)) {
            if (repoPostRestClient.getPostById(post.getId()) == null) {
                LOG.debug("Post not found. Post ID: {}", post.getId());
                return false;
            } else {
                neo4jRepository.addPost(post);
                LOG.debug("Post added to Neo4j. Post ID: {}", post.getId());
                return true;
            }
        }
        LOG.debug("Post already exists. Post ID: {}", post.getId());
        return true;
    }

    /**
     * Verifies if a user exists in the repository.
     * If not, it attempts to fetch and add the user to the repository.
     *
     * @param user The user to verify.
     * @return True if the user exists, false otherwise.
     */
    public boolean userExistVerify(UsersSocial user) {
        LOG.debug("Verifying existence of user. User ID: {}", user.getId());
        if (!neo4jRepository.userExists(user)) {
            if (repoPostRestClient.getUserById(user.getId()) == null) {
                LOG.debug("User not found. User ID: {}", user.getId());
                return false;
            } else {
                neo4jRepository.addUser(user);
                LOG.debug("User added to Neo4j. User ID: {}", user.getId());
                return true;
            }
        }
        LOG.debug("User already exists. User ID: {}", user.getId());
        return true;
    }
}