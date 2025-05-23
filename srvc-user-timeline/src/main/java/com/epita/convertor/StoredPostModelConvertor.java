package com.epita.convertor;

import com.epita.common.api.response.PostIdResponse;
import com.epita.repository.model.StoredPostModel;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class StoredPostModelConvertor {
    public PostIdResponse modelToResponse(StoredPostModel storedPostModel) {
        return new PostIdResponse(
                storedPostModel.getPost_id()
        );
    }

    public List<PostIdResponse> modelListToResponseList(List<StoredPostModel> storedPostModels) {
        return storedPostModels
                .stream()
                .map(this::modelToResponse)
                .toList();
    }
}
