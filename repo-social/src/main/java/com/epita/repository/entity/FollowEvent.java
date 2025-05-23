package com.epita.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class FollowEvent {
    private UUID userId;
    private TypeFollow typeFollow;
    private UUID followeeId;
}