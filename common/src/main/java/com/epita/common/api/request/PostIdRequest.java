package com.epita.common.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PostIdRequest {
    private UUID postId;
}
