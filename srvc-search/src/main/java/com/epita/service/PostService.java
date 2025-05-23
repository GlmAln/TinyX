package com.epita.service;

import com.epita.common.command.PostEventCommand;
import com.epita.converter.PostConverter;
import com.epita.repository.SearchRepository;

import com.epita.service.entity.PostEntity;
import jakarta.inject.Inject;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Service for managing posts in the search service.
 * Provides methods to create and delete posts in the search index.
 */
@ApplicationScoped
public class PostService {

    private static final Logger LOG = LoggerFactory.getLogger(PostService.class);

    @Inject
    SearchRepository searchRepository;

    @Inject
    PostConverter postConverter;

    /**
     * Creates a post in the search index.
     *
     * @param post the post event command containing post details.
     */
    public void createPost(PostEventCommand post) {
        LOG.info("Creating post with ID: {}", post.getPostId());
        try {
            searchRepository.index(postConverter.entityToModel(new PostEntity(post.getPostId(), post
                    .getText()
                    .orElse(""))));
            LOG.info("Successfully created post with ID: {}", post.getPostId());
        } catch (IOException e) {
            LOG.error("Error while creating post with ID: {}", post.getPostId(), e);
            throw new RuntimeException("Failed to create post", e);
        }
    }

    /**
     * Deletes a post from the search index.
     *
     * @param post the post event command containing the ID of the post to delete.
     */
    public void deletePost(PostEventCommand post) {
        LOG.info("Deleting post with ID: {}", post.getPostId());
        try {
            searchRepository.deleteById(post
                    .getPostId()
                    .toString());
            LOG.info("Successfully deleted post with ID: {}", post.getPostId());
        } catch (IOException e) {
            LOG.error("Error while deleting post with ID: {}", post.getPostId(), e);
            throw new RuntimeException("Failed to delete post", e);
        }
    }
}
