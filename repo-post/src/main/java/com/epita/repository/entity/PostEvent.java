package com.epita.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PostEvent {
    public static enum Type {
        CREATION,
        DELETION,
    }

    private Type type;
    private UUID postId;
    private UUID userId;
    private Optional<String> text;
    private LocalDateTime eventTime;


}
