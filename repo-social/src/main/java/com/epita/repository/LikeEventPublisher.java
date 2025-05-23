package com.epita.repository;

import com.epita.repository.entity.LikeEvent;
import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class LikeEventPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(LikeEventPublisher.class);
    private static final String CHANNEL = "queue-likes-events";
    private final PubSubCommands<LikeEvent> publisher;

    public LikeEventPublisher(final RedisDataSource ds) {
        LOG.info("Initializing LikeEventPublisher.");
        try {
            publisher = ds.pubsub(LikeEvent.class);
            LOG.info("LikeEventPublisher initialized successfully.");
        } catch (Exception e) {
            LOG.error("Error while initializing LikeEventPublisher.", e);
            throw e;
        }
    }

    public void publish(final LikeEvent message) {
        LOG.info("Publishing LikeEvent to channel: {}. Event: {}", CHANNEL, message);
        try {
            publisher.publish(CHANNEL, message);
            LOG.info("Successfully published LikeEvent to channel: {}", CHANNEL);
        } catch (Exception e) {
            LOG.error("Error while publishing LikeEvent to channel: {}", CHANNEL, e);
            throw e;
        }
    }
}