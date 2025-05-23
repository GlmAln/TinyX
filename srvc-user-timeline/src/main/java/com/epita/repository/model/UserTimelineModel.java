package com.epita.repository.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.ArrayList;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@With
@MongoEntity(collection = "userTimelines")
public class UserTimelineModel {
    @BsonId
    public UUID id = UUID.randomUUID();
    public UUID user_id;
    public ArrayList<StoredPostModel> posts;
}
