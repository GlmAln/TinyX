package com.epita.controller.subscriber;

import com.epita.controller.contract.FollowContract;
import com.epita.service.HomeTimelineService;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;
import io.quarkus.runtime.Startup;
import io.vertx.core.Vertx;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

@Startup
@ApplicationScoped
public class FollowSubscriber implements Consumer<FollowContract> {
    private static final Logger LOG = LoggerFactory.getLogger(FollowSubscriber.class);
    private static final String CHANNEL = "queue-follow-events";
    private final PubSubCommands.RedisSubscriber subscriber;

    @Inject
    HomeTimelineService homeTimelineService;

    @Inject
    Vertx vertx;

    public FollowSubscriber(final RedisDataSource ds) {
        LOG.info("Initializing FollowSubscriber and subscribing to channel: {}", CHANNEL);
        subscriber = ds
                .pubsub(FollowContract.class)
                .subscribe(CHANNEL, this);
        LOG.info("Successfully subscribed to channel: {}", CHANNEL);
    }

    @Override
    public void accept(final FollowContract contract) {
        LOG.info("Received FollowContract: {}", contract);
        vertx.executeBlocking(future -> {
            try {
                LOG.info("Processing follow event for user ID: {}, followee ID: {}, type: {}",
                        contract.getUserId(), contract.getFolloweeId(), contract.getTypeFollow());
                homeTimelineService.handleFollowEvent(
                        contract.getUserId(),
                        contract.getFolloweeId(),
                        contract.getTypeFollow()
                );
                LOG.info("Successfully processed follow event for user ID: {}", contract.getUserId());
                future.complete();
            } catch (Exception e) {
                LOG.error("Error while processing follow event for user ID: {}", contract.getUserId(), e);
                future.fail(e);
            }
        });
    }

    @PreDestroy
    public void terminate() {
        LOG.info("Unsubscribing from channel: {} and terminating FollowSubscriber.", CHANNEL);
        subscriber.unsubscribe();
        LOG.info("Successfully unsubscribed from channel: {}", CHANNEL);
    }
}
