package com.epita.repository.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ApplicationScoped
@NoArgsConstructor
@MongoEntity(collection = "Posts")
public class Post {

    @BsonId
    private UUID id = UUID.randomUUID();

    private UUID authorId;

    private String text;

    private String mediaFileId;

    private UUID repostId;

    private UUID replyId;

    private LocalDateTime creationDate;

    public Post(UUID authorId, String text, String mediaFileId, UUID repostId, UUID replyId) {
        this.authorId = authorId;
        this.text = text;
        this.mediaFileId = mediaFileId;
        this.repostId = repostId;
        this.replyId = replyId;
        this.creationDate = LocalDateTime.now();
    }

}

