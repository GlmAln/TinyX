package com.epita.repository;

import com.epita.repository.entity.Post;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PostRepository implements PanacheMongoRepositoryBase<Post, UUID> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostRepository.class);

    public boolean deletePost(UUID postId) {
        return deleteById(postId);
    }

    public UUID createPost(Post post) {
        try {
            persist(post);
            LOGGER.info("Post created");
            return post.getId();
        } catch (Exception e) {
            LOGGER.error("Error while creating post: {}", e.getMessage());
            throw new RuntimeException("Error while creating post: " + e.getMessage(), e);
        }
    }

    public List<Post> getUserPosts(UUID userId) {
        return list("authorId", userId);
    }

    public Post getPostById(UUID postId) {
        return find("_id", postId).firstResult();
    }

    public List<Post> getPostReplies(UUID postId) {
        return list("replyId", postId);
    }

    public List<Post> getPostReposts(UUID postId) {
        return list("repostId", postId);
    }
}
