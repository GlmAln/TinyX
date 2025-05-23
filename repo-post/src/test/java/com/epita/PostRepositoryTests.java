package com.epita;

import com.epita.repository.PostRepository;
import com.epita.repository.entity.Post;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;


import java.util.List;
import java.util.UUID;

@QuarkusTest
public class PostRepositoryTests {
    @Inject
    PostRepository postRepository;

    private final UUID authorId = UUID.randomUUID();
    private final UUID postId = UUID.randomUUID();


    @Test
    void addPost() {
        Post post = new Post(authorId, "Hello World", null, null, null);
        post.setId(postId);
        UUID returned_postId = postRepository.createPost(post);
        assertNotNull(returned_postId);
        assertEquals(returned_postId, post.getId());
    }

    @Test
    void getPost() {
        Post post = new Post(authorId, "Hello World", null, null, null);
        post.setId(postId);
        postRepository.createPost(post);
        List<Post> posts = postRepository.getUserPosts(authorId);
        assertNotNull(posts);
        assertFalse(posts.isEmpty());
        assertEquals(posts
                .get(0)
                .getAuthorId(), authorId);
        assertEquals(posts
                .get(0)
                .getId(), postId);

    }

    @Test
    void getPostById() {
        Post post = new Post(authorId, "Hello World", null, null, null);
        post.setId(postId);
        postRepository.createPost(post);
        Post retrievedPost = postRepository.getPostById(postId);
        assertNotNull(retrievedPost);
        assertEquals(retrievedPost.getId(), postId);
        assertEquals(retrievedPost.getAuthorId(), authorId);
    }

    @Test
    void getPostReplies() {
        Post post = new Post(authorId, "Hello World", null, null, null);
        post.setId(postId);
        postRepository.createPost(post);
        List<Post> replies = postRepository.getPostReplies(postId);
        assertNotNull(replies);
        assertTrue(replies.isEmpty());

        Post reply = new Post(authorId, "Hello World", null, null, postId);
        reply.setId(UUID.randomUUID());
        postRepository.createPost(reply);
        replies = postRepository.getPostReplies(postId);
        assertNotNull(replies);
        assertFalse(replies.isEmpty());
        assertEquals(replies
                .get(0)
                .getReplyId(), postId);
    }


    @Test
    void deletePost() {
        Post post = new Post(authorId, "Hello World", null, null, null);
        post.setId(postId);
        postRepository.createPost(post);
        boolean deleted = postRepository.deletePost(postId);
        assertTrue(deleted);
        Post retrievedPost = postRepository.getPostById(postId);
        assertNull(retrievedPost);
    }

}
