package com.epita.controller.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class FollowContract {
    private UUID userId;
    private TypeFollow typeFollow;
    private UUID followeeId;
}