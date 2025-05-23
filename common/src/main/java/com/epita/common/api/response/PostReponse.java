package com.epita.common.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PostReponse {
    private UUID id;
    private UUID authorId;
    private String text;
    private String mediaId;
    private UUID repostId;
    private UUID replyId;
    private LocalDateTime creationDate;
}