package com.epita.service;

import com.epita.controller.contract.TypeFollow;
import com.epita.convertor.PostConvertor;
import com.epita.repository.HomeTimelineRepository;
import com.epita.repository.UserTimelineRestClient;
import com.epita.service.entity.PostEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing the home timeline in the Repo-Social service.
 * Provides methods to retrieve a user's home timeline and handle follow events.
 */
@ApplicationScoped
public class HomeTimelineService {

    private static final Logger LOG = LoggerFactory.getLogger(HomeTimelineService.class);

    @Inject
    HomeTimelineRepository homeTimelineRepo;

    @Inject
    UserTimelineRestClient userTimelineRestClient;

    @Inject
    PostConvertor postConvertor;

    /**
     * Retrieves the home timeline for a user.
     * Combines posts from all users followed by the specified user.
     *
     * @param userId the UUID of the user whose home timeline is to be retrieved.
     * @return a list of post IDs representing the user's home timeline.
     */
    public List<UUID> getHomeTimeline(UUID userId) {
        LOG.info("Fetching home timeline for user ID: {}", userId);
        try {
            List<UUID> followeeIds = homeTimelineRepo.getFollowedUsers(userId);
            LOG.debug("Followee IDs for user ID {}: {}", userId, followeeIds);

            List<UUID> allUserTimeline = new ArrayList<>();
            List<PostEntity> userTimelineItems = postConvertor.responseListToEntityList(userTimelineRestClient.getUserTimeline(followeeIds));
            for (PostEntity item : userTimelineItems) {
                allUserTimeline.add(item.getPost_id());
            }

            LOG.info("Successfully fetched home timeline for user ID: {}", userId);
            return allUserTimeline;
        } catch (Exception e) {
            LOG.error("Error while fetching home timeline for user ID: {}", userId, e);
            throw e;
        }
    }

    /**
     * Handles follow or unfollow events for a user.
     * Updates the list of followed users in the home timeline repository.
     *
     * @param userId the UUID of the user performing the follow or unfollow action.
     * @param followeeId the UUID of the user being followed or unfollowed.
     * @param type the type of follow event (FOLLOW or UNFOLLOW).
     */
    public void handleFollowEvent(UUID userId, UUID followeeId, TypeFollow type) {
        LOG.info("Handling follow event. User ID: {}, Followee ID: {}, Type: {}", userId, followeeId, type);
        try {
            homeTimelineRepo.updateFollowedUsers(userId, followeeId, type);
            LOG.info("Successfully handled follow event for user ID: {}", userId);
        } catch (Exception e) {
            LOG.error("Error while handling follow event for user ID: {}", userId, e);
            throw e;
        }
    }
}
