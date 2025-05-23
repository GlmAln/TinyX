package com.epita.repository;

import com.epita.repository.entity.RepoPostResponse;
import com.epita.repository.entity.RepoPostUserResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@ApplicationScoped
public class RepoPostRestClient {

    private static final Logger LOG = LoggerFactory.getLogger(RepoPostRestClient.class);

    private static final String REPO_POST_URL = "http://repo-post/api";

    private final Client client = ClientBuilder.newClient();

    public RepoPostResponse getPostById(UUID postId) {
        LOG.info("Fetching post with ID: {}", postId);
        try {
            Response response = client
                    .target(REPO_POST_URL + "/posts/" + postId)
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                LOG.error("Post with ID: {} not found", postId);
                throw new NotFoundException("Post not found");
            }

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                LOG.error("Error fetching post with ID: {}. Status: {}", postId, response.getStatus());
                throw new RuntimeException("Error fetching post. Status: " + response.getStatus());
            }

            LOG.info("Successfully fetched post with ID: {}", postId);
            return response.readEntity(RepoPostResponse.class);
        } catch (NotFoundException e) {
            LOG.error("Post with ID: {} not found", postId, e);
            throw e;
        } catch (Exception e) {
            LOG.error("Error fetching post with ID: {}", postId, e);
            throw new RuntimeException("Error fetching post from RepoPost service", e);
        }
    }

    public RepoPostUserResponse getUserById(UUID userId) {
        LOG.info("Fetching user with ID: {}", userId);
        try {
            Response response = client
                    .target(REPO_POST_URL + "/users/" + userId)
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                LOG.error("User with ID: {} not found", userId);
                throw new NotFoundException("User not found");
            }

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                LOG.error("Error fetching user with ID: {}. Status: {}", userId, response.getStatus());
                throw new RuntimeException("Error fetching user. Status: " + response.getStatus());
            }

            LOG.info("Successfully fetched user with ID: {}", userId);
            return response.readEntity(RepoPostUserResponse.class);
        } catch (NotFoundException e) {
            LOG.error("User with ID: {} not found", userId, e);
            throw e;
        } catch (Exception e) {
            LOG.error("Error fetching user with ID: {}", userId, e);
            throw new RuntimeException("Error fetching user from RepoPost service", e);
        }
    }
}
