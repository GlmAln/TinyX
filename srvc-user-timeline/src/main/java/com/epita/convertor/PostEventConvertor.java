package com.epita.convertor;

import com.epita.common.command.PostEventCommand;
import com.epita.service.entity.PostEventEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PostEventConvertor {
    public PostEventEntity commandToEntity(PostEventCommand postEventCommand) {
        return new PostEventEntity(
                commandTypeToEntityType(postEventCommand.getType()),
                postEventCommand.getPostId(),
                postEventCommand.getUserId(),
                postEventCommand.getEventTime()
        );
    }

    public PostEventEntity.Type commandTypeToEntityType(PostEventCommand.Type type) {
        return PostEventEntity.Type.valueOf(type.name());
    }
}
