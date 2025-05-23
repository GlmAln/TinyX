package com.epita.service.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PostEventEntity {
    public enum Type {
        CREATION,
        DELETION,
        LIKE,
        UNLIKE,
    }

    private final Type type;
    private final UUID postId;
    private final UUID userId;
    private final LocalDateTime eventTime;
}