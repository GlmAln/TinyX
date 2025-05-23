package com.epita.repository;

import com.epita.common.command.PostEventCommand;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publisher for post events.
 * This class is responsible for publishing post events to a Redis channel.
 */
@ApplicationScoped
public class PostEventPublisher {
    private static final String CHANNEL = "post_events";

    private static final Logger LOG = LoggerFactory.getLogger(PostEventPublisher.class);

    private final PubSubCommands<PostEventCommand> publisher;

    public PostEventPublisher(final RedisDataSource ds) {
        publisher = ds.pubsub(PostEventCommand.class);
    }

    public void publish(final PostEventCommand message) {
        LOG.info("Publishing post event: {}", message);
        publisher.publish(CHANNEL, message);
    }
}
