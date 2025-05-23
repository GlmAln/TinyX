package com.epita.repository.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@MongoEntity(collection = "HomeTimeline")
public class HomeTimelineModel {
    @BsonId
    private UUID id;
    private UUID user_id;
    private List<UUID> followed_users;
}
