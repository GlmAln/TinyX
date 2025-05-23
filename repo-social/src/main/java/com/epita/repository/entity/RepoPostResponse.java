package com.epita.repository.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class RepoPostResponse {
    private UUID id;
    private UUID authorId;
    private String text;
    private String media;
    private UUID repostId;
    private UUID replyId;
    private LocalDateTime creationDate;

    public RepoPostResponse(UUID id, UUID authorId, String text, String media, UUID repostId, UUID replyId, LocalDateTime creationDate) {
        this.id = id;
        this.authorId = authorId;
        this.text = text;
        this.media = media;
        this.repostId = repostId;
        this.replyId = replyId;
        this.creationDate = creationDate;
    }

    public RepoPostResponse() {}
}
