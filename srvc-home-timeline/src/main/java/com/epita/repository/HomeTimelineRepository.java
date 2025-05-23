package com.epita.repository;

import com.epita.controller.contract.TypeFollow;
import com.epita.repository.model.HomeTimelineModel;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class HomeTimelineRepository implements PanacheMongoRepository<HomeTimelineModel> {

    private static final Logger LOG = LoggerFactory.getLogger(HomeTimelineRepository.class);

    public void updateFollowedUsers(UUID userId, UUID followeeId, TypeFollow type) {
        LOG.info("Updating followed users for user ID: {}, followee ID: {}, type: {}", userId, followeeId, type);
        try {
            HomeTimelineModel timeline = getOrCreateTimeline(userId);

            if (type == TypeFollow.FOLLOW) {
                LOG.debug("Adding followee ID: {} to user ID: {}", followeeId, userId);
                addFollowee(timeline, followeeId);
            } else if (type == TypeFollow.UNFOLLOW) {
                LOG.debug("Removing followee ID: {} from user ID: {}", followeeId, userId);
                removeFollowee(timeline, followeeId);
            }

            persistOrUpdate(timeline);
            LOG.info("Successfully updated followed users for user ID: {}", userId);
        } catch (Exception e) {
            LOG.error("Error while updating followed users for user ID: {}", userId, e);
            throw e;
        }
    }

    private HomeTimelineModel getOrCreateTimeline(UUID userId) {
        LOG.debug("Fetching or creating timeline for user ID: {}", userId);
        HomeTimelineModel timeline = find("user_id", userId).firstResult();
        if (timeline == null) {
            LOG.debug("No timeline found for user ID: {}. Creating a new one.", userId);
            timeline = new HomeTimelineModel(UUID.randomUUID(), userId, new ArrayList<>());
        }
        return timeline;
    }

    private void addFollowee(HomeTimelineModel timeline, UUID followeeId) {
        List<UUID> followed = timeline.getFollowed_users();
        if (!followed.contains(followeeId)) {
            followed.add(followeeId);
            LOG.debug("Followee ID: {} added to timeline for user ID: {}", followeeId, timeline.getUser_id());
        } else {
            LOG.debug("Followee ID: {} already exists in timeline for user ID: {}", followeeId, timeline.getUser_id());
        }
    }

    private void removeFollowee(HomeTimelineModel timeline, UUID followeeId) {
        List<UUID> followed = timeline.getFollowed_users();
        if (followed.remove(followeeId)) {
            LOG.debug("Followee ID: {} removed from timeline for user ID: {}", followeeId, timeline.getUser_id());
        } else {
            LOG.debug("Followee ID: {} not found in timeline for user ID: {}", followeeId, timeline.getUser_id());
        }
    }

    public List<UUID> getFollowedUsers(UUID userId) {
        LOG.info("Fetching followed users for user ID: {}", userId);
        try {
            HomeTimelineModel timeline = find("user_id", userId).firstResult();
            if (timeline != null) {
                LOG.info("Found {} followed users for user ID: {}", timeline
                        .getFollowed_users()
                        .size(), userId);
                return timeline.getFollowed_users();
            }
            LOG.info("No followed users found for user ID: {}", userId);
            return new ArrayList<>();
        } catch (Exception e) {
            LOG.error("Error while fetching followed users for user ID: {}", userId, e);
            throw e;
        }
    }
}
