package com.epita.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;
import java.sql.Timestamp;

@AllArgsConstructor
@Getter
public class LikeEvent {
    private UUID userId;
    private UUID postId;
    private TypeLikes typeLikes;
    private Timestamp creationDate;
}
