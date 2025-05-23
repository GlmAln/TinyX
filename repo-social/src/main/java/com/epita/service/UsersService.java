package com.epita.service;

import com.epita.repository.FollowEventPublisher;
import com.epita.repository.Neo4jRepository;
import com.epita.repository.RepoPostRestClient;
import com.epita.repository.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing user-related operations in the Repo-Social service.
 * Provides methods for following, unfollowing, blocking, unblocking users,
 * and retrieving user relationships such as followers, followees, and blocked users.
 */
@ApplicationScoped
public class UsersService {

    private static final Logger LOG = LoggerFactory.getLogger(UsersService.class);

    @Inject
    public Neo4jRepository neo4jRepository;

    @Inject
    public FollowEventPublisher followEventPublisher;

    @Inject
    public RepoPostRestClient repoPostRestClient;

    /**
     * Follow a user
     *
     * @param followerId The ID of the user who wants to follow
     * @param followeeId The ID of the user to be followed
     * @throws ForbiddenException If the user is blocked
     * @throws NotFoundException  If the target user does not exist
     * @throws ForbiddenException If followerId equals to followeeId
     */
    public void followUser(UUID followerId, UUID followeeId) {
        LOG.info("Attempting to follow user. Follower ID: {}, Followee ID: {}", followerId, followeeId);
        if (followerId.equals(followeeId)) {
            LOG.error("Follower ID and Followee ID cannot be the same.");
            throw new ForbiddenException("FollowerId and FolloweeId cannot be the same");
        }
        try {
            UsersSocial followee = new UsersSocial(followeeId);
            UsersSocial follower = new UsersSocial(followerId);

            if (!userExistVerify(follower) || !userExistVerify(followee)) {
                LOG.error("User not found. Follower ID: {}, Followee ID: {}", followerId, followeeId);
                throw new NotFoundException("User not found");
            }

            if (neo4jRepository.blockRelationExists(followee, follower) || neo4jRepository.blockRelationExists(follower, followee)) {
                LOG.error("User is blocked. Follower ID: {}, Followee ID: {}", followerId, followeeId);
                throw new ForbiddenException("User is blocked");
            }

            if (neo4jRepository.followRelationExists(follower, followee)) {
                LOG.info("Already following. Follower ID: {}, Followee ID: {}", followerId, followeeId);
                return;
            }

            neo4jRepository.createFollowRelation(follower, followee);
            LOG.info("Created follow relationship. Follower ID: {}, Followee ID: {}", followerId, followeeId);

            FollowEvent event = new FollowEvent(followerId, TypeFollow.FOLLOW, followeeId);
            followEventPublisher.publish(event);
            LOG.info("Published follow event. Follower ID: {}, Followee ID: {}", followerId, followeeId);
        } catch (Exception e) {
            LOG.error("Error while following user. Follower ID: {}, Followee ID: {}", followerId, followeeId, e);
            throw e;
        }
    }


    /**
     * Unfollow a user
     *
     * @param followerId The ID of the user who wants to unfollow
     * @param followeeId The ID of the user to be unfollowed
     * @throws NotFoundException If the follow relationship does not exist
     */
    public void unfollowUser(UUID followerId, UUID followeeId) {
        LOG.info("Attempting to unfollow user. Follower ID: {}, Followee ID: {}", followerId, followeeId);
        if (followerId.equals(followeeId)) {
            LOG.error("Follower ID and Followee ID cannot be the same.");
            throw new ForbiddenException("FollowerId and FolloweeId cannot be the same");
        }
        try {
            UsersSocial followee = new UsersSocial(followeeId);
            UsersSocial follower = new UsersSocial(followerId);

            if (!neo4jRepository.followRelationExists(follower, followee)) {
                LOG.error("Follow relationship not found. Follower ID: {}, Followee ID: {}", followerId, followeeId);
                throw new NotFoundException("Follow relation not found");
            }

            neo4jRepository.removeFollowRelation(follower, followee);
            LOG.info("Removed follow relationship. Follower ID: {}, Followee ID: {}", followerId, followeeId);

            FollowEvent event = new FollowEvent(followerId, TypeFollow.UNFOLLOW, followeeId);
            followEventPublisher.publish(event);
            LOG.info("Published unfollow event. Follower ID: {}, Followee ID: {}", followerId, followeeId);
        } catch (Exception e) {
            LOG.error("Error while unfollowing user. Follower ID: {}, Followee ID: {}", followerId, followeeId, e);
            throw e;
        }
    }

    /**
     * Block a user
     *
     * @param blockerId The ID of the user who wants to block
     * @param blockedId The ID of the user to be blocked
     */
    public void blockUser(UUID blockerId, UUID blockedId) {
        LOG.info("Attempting to block user. Blocker ID: {}, Blocked ID: {}", blockerId, blockedId);
        if (blockerId.equals(blockedId)) {
            LOG.error("Blocker ID and Blocked ID cannot be the same.");
            throw new ForbiddenException("BlockerId and BlockedId cannot be the same");
        }
        try {
            UsersSocial blocker = new UsersSocial(blockerId);
            UsersSocial blocked = new UsersSocial(blockedId);

            if (!userExistVerify(blocker) || !userExistVerify(blocked)) {
                LOG.error("User not found. Blocker ID: {}, Blocked ID: {}", blockerId, blockedId);
                throw new NotFoundException("User not found");
            }

            if (neo4jRepository.followRelationExists(blocker, blocked) || neo4jRepository.followRelationExists(blocked, blocker)) {
                neo4jRepository.removeFollowRelation(blocker, blocked);
                neo4jRepository.removeFollowRelation(blocked, blocker);
                LOG.info("Removed follow relationships between Blocker ID: {} and Blocked ID: {}", blockerId, blockedId);

                FollowEvent event = new FollowEvent(blockerId, TypeFollow.UNFOLLOW, blockedId);
                followEventPublisher.publish(event);
                LOG.info("Published unfollow event for Blocker ID: {}, Blocked ID: {}", blockerId, blockedId);

                event = new FollowEvent(blockedId, TypeFollow.UNFOLLOW, blockerId);
                followEventPublisher.publish(event);
                LOG.info("Published unfollow event for Blocked ID: {}, Blocker ID: {}", blockedId, blockerId);
            }

            if (!neo4jRepository.blockRelationExists(blocker, blocked)) {
                neo4jRepository.createBlockRelation(blocker, blocked);
                LOG.info("Created block relationship. Blocker ID: {}, Blocked ID: {}", blockerId, blockedId);
            }
        } catch (Exception e) {
            LOG.error("Error while blocking user. Blocker ID: {}, Blocked ID: {}", blockerId, blockedId, e);
            throw e;
        }
    }

    /**
     * Unblock a user
     *
     * @param blockerId The ID of the user who wants to unblock
     * @param blockedId The ID of the user to be unblocked
     * @throws NotFoundException If the block relationship does not exist
     */
    public void unblockUser(UUID blockerId, UUID blockedId) {
        LOG.info("Attempting to unblock user. Blocker ID: {}, Blocked ID: {}", blockerId, blockedId);
        if (blockerId.equals(blockedId)) {
            LOG.error("Blocker ID and Blocked ID cannot be the same.");
            throw new ForbiddenException("BlockerId and BlockedId cannot be the same");
        }
        try {
            UsersSocial blocker = new UsersSocial(blockerId);
            UsersSocial blocked = new UsersSocial(blockedId);

            if (!neo4jRepository.blockRelationExists(blocker, blocked)) {
                LOG.error("Block relationship not found. Blocker ID: {}, Blocked ID: {}", blockerId, blockedId);
                throw new NotFoundException("Block relation not found");
            }

            neo4jRepository.removeBlockRelation(blocker, blocked);
            LOG.info("Removed block relationship. Blocker ID: {}, Blocked ID: {}", blockerId, blockedId);
        } catch (Exception e) {
            LOG.error("Error while unblocking user. Blocker ID: {}, Blocked ID: {}", blockerId, blockedId, e);
            throw e;
        }
    }

    /**
     * Get all posts liked by a user
     *
     * @param userId The ID of the user
     * @return List of post IDs liked by the user
     * @throws NotFoundException If the user does not exist
     */
    public List<PostsSocial> getLikedPosts(UUID userId) {
        UsersSocial user = new UsersSocial(userId);
        // Check if user exists
        if (!userExistVerify(user)) {
            throw new NotFoundException("User not found");
        }

        // Find all posts with LIKES relationship from this user
        return neo4jRepository.getAllLikedPost(user);
    }

    /**
     * Get all users followed by a user
     *
     * @param userId The ID of the user
     * @return List of user IDs followed by the user
     * @throws NotFoundException If the user does not exist
     */
    public List<UsersSocial> getFollowees(UUID userId) {
        UsersSocial user = new UsersSocial(userId);
        // Check if user exists
        if (!userExistVerify(user)) {
            throw new NotFoundException("User not found");
        }

        // Find all users with FOLLOWS relationship from this user
        return neo4jRepository.getAllFollowsOfUser(user);
    }

    /**
     * Get all followers of a user
     *
     * @param userId The ID of the user
     * @return List of user IDs following the user
     * @throws NotFoundException If the user does not exist
     */
    public List<UsersSocial> getFollowers(UUID userId) {
        UsersSocial user = new UsersSocial(userId);
        // Check if user exists
        if (!userExistVerify(user)) {
            throw new NotFoundException("User not found");
        }
        return neo4jRepository.getAllFollowersOfUser(user);
    }

    /**
     * Get all users blocked by a user
     *
     * @param userId The ID of the user
     * @return List of user IDs blocked by the user
     * @throws NotFoundException If the user does not exist
     */
    public List<UsersSocial> getBlockedUsers(UUID userId) {
        UsersSocial user = new UsersSocial(userId);
        // Check if user exists
        if (!userExistVerify(user)) {
            throw new NotFoundException("User not found");
        }

        // Find all users with BLOCKS relationship from this user
        return neo4jRepository.getAllBlockedOfUser(user);
    }

    /**
     * Get all users who blocked the specified user
     *
     * @param userId The ID of the user
     * @return List of user IDs who blocked the user
     * @throws NotFoundException If the user does not exist
     */
    public List<UsersSocial> getUsersWhoBlockedMe(UUID userId) {
        UsersSocial user = new UsersSocial(userId);
        // Check if user exists
        if (!userExistVerify(user)) {
            throw new NotFoundException("User not found");
        }
        return neo4jRepository.getAllBlockersOfUser(user);
    }

    /**
     * Verifies if a user exists in the repository.
     * If not, it attempts to fetch and add the user to the repository.
     *
     * @param user The user to verify.
     * @return True if the user exists, false otherwise.
     */
    public boolean userExistVerify(UsersSocial user) {
        LOG.debug("Verifying existence of user with ID: {}", user.getId());
        if (!neo4jRepository.userExists(user)) {
            if (repoPostRestClient.getUserById(user.getId()) == null) {
                LOG.debug("User with ID: {} does not exist.", user.getId());
                return false;
            } else {
                neo4jRepository.addUser(user);
                LOG.debug("User with ID: {} added to Neo4j.", user.getId());
                return true;
            }
        }
        LOG.debug("User with ID: {} already exists.", user.getId());
        return true;
    }
}