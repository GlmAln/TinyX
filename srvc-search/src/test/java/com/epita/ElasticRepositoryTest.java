package com.epita;

import com.epita.repository.SearchRepository;
import com.epita.repository.entity.PostModel;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@QuarkusTest
class ElasticRepositoryTest {

    @Inject
    SearchRepository elasticSearchRepository;


    @Test
    void testIndex() throws IOException {
        UUID postId = UUID.randomUUID();
        PostModel post = new PostModel(postId, "Test #Hashtag #word table");

        elasticSearchRepository.index(post);
        elasticSearchRepository.refresh();
        elasticSearchRepository.deleteById(postId.toString());
        var allpost = elasticSearchRepository.searchAll();
        assertTrue(allpost
                .stream()
                .anyMatch(p -> p
                        .getId()
                        .equals(postId)));
    }

    @Test
    void testDelete() throws IOException {
        UUID postId = UUID.randomUUID();
        PostModel post = new PostModel(postId, "Post content with #random text");

        elasticSearchRepository.index(post);
        elasticSearchRepository.refresh();
        elasticSearchRepository.deleteById(postId.toString());
        elasticSearchRepository.refresh();
        var allpost = elasticSearchRepository.searchAll();

        assertFalse(allpost
                .stream()
                .anyMatch(p -> p
                        .getId()
                        .equals(postId)));
    }

    @Test
    void testSearchHashtag() throws IOException {
        UUID postId = UUID.randomUUID();
        PostModel post = new PostModel(postId, "Post content with #random #word text");

        elasticSearchRepository.index(post);
        elasticSearchRepository.refresh();
        List<PostModel> response = elasticSearchRepository.searchInField("hashtags", "#random");
        elasticSearchRepository.deleteById(postId.toString());

        assertTrue(response
                .stream()
                .anyMatch(p -> p
                        .getId()
                        .equals(postId)));
    }

    @Test
    void testSearchHashtag2() throws IOException {
        UUID postId = UUID.randomUUID();
        PostModel post = new PostModel(postId, "Post content with #random #word text");

        elasticSearchRepository.index(post);
        elasticSearchRepository.refresh();
        List<PostModel> response = elasticSearchRepository.searchInField("hashtags", "#random #dzdz");
        elasticSearchRepository.deleteById(postId.toString());

        assertEquals(0, response.size());
    }

    @Test
    void testSearchWord() throws IOException {
        UUID postId = UUID.randomUUID();
        PostModel post = new PostModel(postId, "Post content with #random #word text");

        elasticSearchRepository.index(post);
        elasticSearchRepository.refresh();
        List<PostModel> response = elasticSearchRepository.searchInField("words", "Post");
        elasticSearchRepository.deleteById(postId.toString());

        assertTrue(response
                .stream()
                .anyMatch(p -> p
                        .getId()
                        .equals(postId)));
    }

    @Test
    void testSearchWord2() throws IOException {
        UUID postId = UUID.randomUUID();
        PostModel post = new PostModel(postId, "Post content with #random #word text");

        elasticSearchRepository.index(post);
        elasticSearchRepository.refresh();
        List<PostModel> response = elasticSearchRepository.searchInField("words", "Post dzdadazdzda");
        elasticSearchRepository.deleteById(postId.toString());

        assertTrue(response
                .stream()
                .anyMatch(p -> p
                        .getId()
                        .equals(postId)));
    }

    @Test
    void testSearchWordAndHashtags() throws IOException {
        UUID postId = UUID.randomUUID();
        PostModel post = new PostModel(postId, "Post content with #random #word text");

        elasticSearchRepository.index(post);
        elasticSearchRepository.refresh();
        List<PostModel> response = elasticSearchRepository.searchInField("words", "Post #random");
        elasticSearchRepository.deleteById(postId.toString());

        assertTrue(response
                .stream()
                .anyMatch(p -> p
                        .getId()
                        .equals(postId)));
    }

    @Test
    void testSearchWordAndHashtags2() throws IOException {
        UUID postId = UUID.randomUUID();
        PostModel post = new PostModel(postId, "Post content with #random #word text");

        elasticSearchRepository.index(post);
        elasticSearchRepository.refresh();
        List<PostModel> response = elasticSearchRepository.searchInField("words", "Po #random");
        elasticSearchRepository.deleteById(postId.toString());

        assertFalse(response
                .stream()
                .anyMatch(p -> p
                        .getId()
                        .equals(postId)));
    }
}