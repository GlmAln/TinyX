package com.epita.repository;

import com.epita.common.api.response.PostIdResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserTimelineRestClient {
    private static final Logger LOG = LoggerFactory.getLogger(UserTimelineRestClient.class);
    private static final String USER_TIMELINE_URL = "http://srvc-user-timeline/api";
    private final Client client = ClientBuilder.newClient();

    public List<PostIdResponse> getUserTimeline(List<UUID> userIds) {
        LOG.info("Fetching user timeline for user IDs: {}", userIds);
        try {
            WebTarget target = client.target(USER_TIMELINE_URL + "/users/timeline");
            for (UUID id : userIds) {
                target = target.queryParam("userIds", id.toString());
            }

            LOG.debug("Constructed target URL: {}", target.getUri());

            List<PostIdResponse> response = target
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<>() {
                    });

            LOG.info("Successfully fetched user timeline for user IDs: {}", userIds);
            return response;
        } catch (Exception e) {
            LOG.error("Error fetching user timeline for user IDs: {}", userIds, e);
            throw new RuntimeException("Error fetching post from RepoPost service", e);
        }
    }
}
