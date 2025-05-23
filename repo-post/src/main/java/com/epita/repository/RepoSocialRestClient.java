package com.epita.repository;

import com.epita.common.api.response.BlockedUserIdResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@ApplicationScoped
public class RepoSocialRestClient {
    private static final String REPO_SOCIAL_URL = "http://repo-social/api/repo-social";

    private final Client client = ClientBuilder.newClient();

    private static final Logger LOG = LoggerFactory.getLogger(RepoSocialRestClient.class);

    public boolean doesUserBlock(UUID userId, UUID blockedUserId) {
        try {
            LOG.info("Checking if user {} blocks user {}", userId, blockedUserId);
            BlockedUserIdResponse[] blockedUsers = client
                    .target(REPO_SOCIAL_URL + "/users/" + userId + "/blocks")
                    .request(MediaType.APPLICATION_JSON)
                    .get(BlockedUserIdResponse[].class);

            for (BlockedUserIdResponse blockedUser : blockedUsers) {
                if (blockedUser
                        .getBlockedUserId()
                        .equals(blockedUserId)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            LOG.error("Error while checking if user {} blocks user {}", userId, blockedUserId, e);
            throw e;
        }
    }

    public boolean isUserBlocked(UUID userId, UUID blockedUserId) {
        try {
            LOG.info("Checking if user {} is blocked by user {}", userId, blockedUserId);
            BlockedUserIdResponse[] blockedUsers = client
                    .target(REPO_SOCIAL_URL + "/users/" + userId + "/blocked")
                    .request(MediaType.APPLICATION_JSON)
                    .get(BlockedUserIdResponse[].class);

            for (BlockedUserIdResponse blockedUser : blockedUsers) {
                if (blockedUser
                        .getBlockedUserId()
                        .equals(blockedUserId)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            LOG.error("Error while checking if user {} is blocked by user {}", userId, blockedUserId, e);
            throw e;
        }
    }
}
