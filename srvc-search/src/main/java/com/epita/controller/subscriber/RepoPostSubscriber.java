package com.epita.controller.subscriber;

import com.epita.common.command.PostEventCommand;
import com.epita.service.PostService;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static io.quarkus.mongodb.runtime.dns.MongoDnsClientProvider.vertx;

@Startup
@ApplicationScoped
public class RepoPostSubscriber implements Consumer<PostEventCommand> {
    @Inject
    PostService postService;

    private static final Logger LOG = LoggerFactory.getLogger(RepoPostSubscriber.class);

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
        vertx.executeBlocking(future -> {
            try {
                if (postEventCommand.getType() == PostEventCommand.Type.CREATION) {
                    LOG.info("Initiating creation of post: {}", postEventCommand.getPostId());
                    postService.createPost(postEventCommand);
                } else {
                    LOG.info("Initiating deletion of post: {}", postEventCommand.getPostId());
                    postService.deletePost(postEventCommand);
                }
                LOG.info("Successfully processed PostEventCommand: {}", postEventCommand);
                future.complete();
            } catch (Exception e) {
                LOG.error("Error while processing PostEventCommand: {}", postEventCommand, e);
                future.fail(e);
            }
        });
    }

    @PreDestroy
    public void terminate() {
        LOG.info("Unsubscribing from 'post_events' channel and terminating RepoPostSubscriber.");
        subscriber.unsubscribe();
        LOG.info("Successfully unsubscribed from 'post_events' channel.");
    }
}