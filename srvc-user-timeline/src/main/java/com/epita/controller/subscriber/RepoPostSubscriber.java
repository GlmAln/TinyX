package com.epita.controller.subscriber;

import com.epita.common.command.PostEventCommand;
import com.epita.convertor.PostEventConvertor;
import com.epita.service.UserTimelineService;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static io.quarkus.mongodb.runtime.dns.MongoDnsClientProvider.vertx;

@Startup
@ApplicationScoped
public class RepoPostSubscriber implements Consumer<PostEventCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(RepoPostSubscriber.class);

    @Inject
    UserTimelineService userTimelineService;

    @Inject
    PostEventConvertor postEventConvertor;

    private final PubSubCommands.RedisSubscriber subscriber;

    public RepoPostSubscriber(final RedisDataSource ds) {
        LOG.info("Initializing RepoPostSubscriber and subscribing to 'post_events' channel.");
        subscriber = ds
                .pubsub(PostEventCommand.class)
                .subscribe("post_events", this);
        LOG.info("Successfully subscribed to 'post_events' channel.");
    }

    @Override
    public void accept(final PostEventCommand postEventCommand) {
        LOG.info("Received PostEventCommand: {}", postEventCommand);
        try {
            vertx.executeBlocking(future -> {
                LOG.debug("Processing PostEventCommand: {}", postEventCommand);
                userTimelineService.applyPostEvent(postEventConvertor.commandToEntity(postEventCommand));
                LOG.info("Successfully processed PostEventCommand: {}", postEventCommand);
            });
        } catch (Exception e) {
            LOG.error("Error while processing PostEventCommand: {}", postEventCommand, e);
            throw e;
        }
    }

    @PreDestroy
    public void terminate() {
        LOG.info("Unsubscribing from 'post_events' channel and terminating RepoPostSubscriber.");
        subscriber.unsubscribe();
        LOG.info("Successfully unsubscribed from 'post_events' channel.");
    }
}
