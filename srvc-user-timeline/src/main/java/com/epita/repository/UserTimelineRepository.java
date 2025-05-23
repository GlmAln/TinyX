package com.epita.repository;

import com.epita.repository.model.StoredPostModel;
import com.epita.repository.model.UserTimelineModel;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserTimelineRepository implements PanacheMongoRepositoryBase<UserTimelineModel, UUID> {

    private static final Logger LOG = LoggerFactory.getLogger(UserTimelineRepository.class);

    public void addUserPostToTimeline(UUID userId, UUID postId, LocalDateTime insertDate) {
        LOG.info("Adding post to timeline. User ID: {}, Post ID: {}, Insert Date: {}", userId, postId, insertDate);
        try {
            List<UserTimelineModel> foundUserTimelines = list("user_id", userId);

            if (foundUserTimelines.isEmpty()) {
                LOG.debug("No timeline found for User ID: {}. Creating a new timeline.", userId);
                UserTimelineModel newUserTimeline = new UserTimelineModel()
                        .withUser_id(userId)
                        .withPosts(new ArrayList<>(List.of(new StoredPostModel(postId, insertDate))));
                persist(newUserTimeline);
                LOG.info("New timeline created and post added for User ID: {}", userId);
                return;
            }

            UserTimelineModel foundUserTimeline = foundUserTimelines.get(0);
            foundUserTimeline.posts.add(new StoredPostModel(postId, insertDate));
            update(foundUserTimeline);
            LOG.info("Post added to existing timeline for User ID: {}", userId);
        } catch (Exception e) {
            LOG.error("Error while adding post to timeline. User ID: {}, Post ID: {}", userId, postId, e);
            throw e;
        }
    }

    public void deleteUserPostToTimeline(UUID userId, UUID postId) {
        LOG.info("Deleting post from timeline. User ID: {}, Post ID: {}", userId, postId);
        try {
            List<UserTimelineModel> foundUserTimelines = list("user_id", userId);
            if (foundUserTimelines.isEmpty()) {
                LOG.debug("No timeline found for User ID: {}. Nothing to delete.", userId);
                return;
            }

            UserTimelineModel foundUserTimeline = foundUserTimelines.get(0);
            boolean removed = foundUserTimeline.posts.removeIf(storedPostModel -> storedPostModel
                    .getPost_id()
                    .equals(postId));
            if (removed) {
                update(foundUserTimeline);
                LOG.info("Post deleted from timeline for User ID: {}, Post ID: {}", userId, postId);
            } else {
                LOG.debug("Post not found in timeline for User ID: {}, Post ID: {}", userId, postId);
            }
        } catch (Exception e) {
            LOG.error("Error while deleting post from timeline. User ID: {}, Post ID: {}", userId, postId, e);
            throw e;
        }
    }

    public List<StoredPostModel> getUsersTimelineSorted(List<UUID> userIds) {
        LOG.info("Fetching and sorting timelines for User IDs: {}", userIds);
        try {
            List<StoredPostModel> sortedTimelines = streamAll()
                    .filter(userTimelineModel -> userIds.contains(userTimelineModel.getUser_id()))
                    .map(userTimelineModel -> userTimelineModel.posts)
                    .flatMap(List::stream)
                    .sorted(Comparator.comparing(storedPostModel -> storedPostModel.insert_date))
                    .toList();
            LOG.info("Successfully fetched and sorted timelines for User IDs: {}", userIds);
            return sortedTimelines;
        } catch (Exception e) {
            LOG.error("Error while fetching and sorting timelines for User IDs: {}", userIds, e);
            throw e;
        }
    }
}