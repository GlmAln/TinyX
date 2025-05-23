package com.epita.common.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class FollowerIdResponse {
    private UUID followerId;
}
