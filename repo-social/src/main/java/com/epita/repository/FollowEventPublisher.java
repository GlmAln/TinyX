package com.epita.repository;

import com.epita.repository.entity.FollowEvent;
import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class FollowEventPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(FollowEventPublisher.class);
    private static final String CHANNEL = "queue-follow-events";
    private final PubSubCommands<FollowEvent> publisher;

    public FollowEventPublisher(final RedisDataSource ds) {
        LOG.info("Initializing FollowEventPublisher.");
        publisher = ds.pubsub(FollowEvent.class);
        LOG.info("FollowEventPublisher initialized successfully.");
    }

    public void publish(final FollowEvent message) {
        LOG.info("Publishing FollowEvent to channel: {}. Event: {}", CHANNEL, message);
        try {
            publisher.publish(CHANNEL, message);
            LOG.info("Successfully published FollowEvent to channel: {}", CHANNEL);
        } catch (Exception e) {
            LOG.error("Error while publishing FollowEvent to channel: {}", CHANNEL, e);
            throw e;
        }
    }
}