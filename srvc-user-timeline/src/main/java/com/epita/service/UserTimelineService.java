package com.epita.service;

import com.epita.common.api.response.PostIdResponse;
import com.epita.convertor.StoredPostModelConvertor;
import com.epita.repository.UserTimelineRepository;
import com.epita.service.entity.PostEventEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing user timelines.
 * Provides methods to retrieve user timelines and apply post events to update timelines.
 */
@ApplicationScoped
public class UserTimelineService {

    private static final Logger LOG = LoggerFactory.getLogger(UserTimelineService.class);

    @Inject
    UserTimelineRepository userTimelineRepository;

    @Inject
    StoredPostModelConvertor storedModelConvertor;

    /**
     * Retrieves the timelines for a list of users.
     * Combines and sorts posts from the specified users' timelines.
     *
     * @param userIds the list of user IDs whose timelines are to be retrieved.
     * @return a list of post ID responses representing the users' timelines.
     */
    public List<PostIdResponse> getUserTimelines(List<UUID> userIds) {
        LOG.info("Fetching timelines for user IDs: {}", userIds);
        try {
            List<PostIdResponse> timelines = storedModelConvertor.modelListToResponseList(
                    userTimelineRepository.getUsersTimelineSorted(userIds)
            );
            LOG.info("Successfully fetched timelines for user IDs: {}", userIds);
            return timelines;
        } catch (Exception e) {
            LOG.error("Error while fetching timelines for user IDs: {}", userIds, e);
            throw e;
        }
    }

    /**
     * Applies a post event to the user timeline.
     * Handles creation, like, and deletion events to update the timeline accordingly.
     *
     * @param postEventEntity the post event entity containing event details.
     */
    public void applyPostEvent(PostEventEntity postEventEntity) {
        LOG.info("Applying post event: {}", postEventEntity);
        try {
            if (postEventEntity.getType() == PostEventEntity.Type.CREATION ||
                    postEventEntity.getType() == PostEventEntity.Type.LIKE) {
                LOG.debug("Adding post to timeline. User ID: {}, Post ID: {}, Event Time: {}",
                        postEventEntity.getUserId(), postEventEntity.getPostId(), postEventEntity.getEventTime());
                userTimelineRepository.addUserPostToTimeline(
                        postEventEntity.getUserId(),
                        postEventEntity.getPostId(),
                        postEventEntity.getEventTime()
                );
                LOG.info("Post added to timeline successfully. User ID: {}, Post ID: {}",
                        postEventEntity.getUserId(), postEventEntity.getPostId());
            } else {
                LOG.debug("Deleting post from timeline. User ID: {}, Post ID: {}",
                        postEventEntity.getUserId(), postEventEntity.getPostId());
                userTimelineRepository.deleteUserPostToTimeline(
                        postEventEntity.getUserId(),
                        postEventEntity.getPostId()
                );
                LOG.info("Post deleted from timeline successfully. User ID: {}, Post ID: {}",
                        postEventEntity.getUserId(), postEventEntity.getPostId());
            }
        } catch (Exception e) {
            LOG.error("Error while applying post event: {}", postEventEntity, e);
            throw e;
        }
    }
}