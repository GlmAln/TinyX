package com.epita.convertor;

import com.epita.common.api.response.PostIdResponse;
import com.epita.service.entity.PostEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class PostConvertor {
    public PostEntity responseToEntity(PostIdResponse postIdResponse) {
        return new PostEntity(
                postIdResponse.getPostId()
        );
    }

    public List<PostEntity> responseListToEntityList(List<PostIdResponse> storedPostResponses) {
        return storedPostResponses
                .stream()
                .map(this::responseToEntity)
                .toList();
    }
}
