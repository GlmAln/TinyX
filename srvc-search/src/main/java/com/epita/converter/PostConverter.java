package com.epita.converter;

import com.epita.repository.entity.PostModel;
import com.epita.service.entity.PostEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PostConverter {
    public PostModel entityToModel(PostEntity postEntity) {
        return new PostModel(postEntity.getId(), postEntity.getRawText());
    }
}
