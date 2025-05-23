package com.epita;

import com.epita.repository.LikeEventPublisher;
import com.epita.repository.Neo4jRepository;
import com.epita.repository.RepoPostRestClient;
import com.epita.repository.entity.LikeEvent;
import com.epita.repository.entity.PostsSocial;
import com.epita.repository.entity.RepoPostResponse;
import com.epita.repository.entity.UsersSocial;
import com.epita.service.PostsService;
import com.epita.service.UsersService;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PostsServiceTest {

    private PostsService postsService;
    private Neo4jRepository neo4jRepository;
    private LikeEventPublisher likeEventPublisher;
    private UsersService usersService;
    private RepoPostRestClient repoPostRestClient;

    @BeforeEach
    void setUp() {
        neo4jRepository = mock(Neo4jRepository.class);
        likeEventPublisher = mock(LikeEventPublisher.class);
        usersService = mock(UsersService.class);
        repoPostRestClient = mock(RepoPostRestClient.class);

        postsService = spy(new PostsService());
        postsService.neo4jRepository = neo4jRepository;
        postsService.likeEventPublisher = likeEventPublisher;
        postsService.repoPostRestClient = repoPostRestClient;
    }

    @Test
    void likePost_shouldThrowNotFound_ifPostDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        // Configure mocks for post not existing
        when(neo4jRepository.postExists(any(PostsSocial.class))).thenReturn(false);
        when(repoPostRestClient.getPostById(any(UUID.class))).thenReturn(null);

        // Execute and verify exception
        assertThrows(NotFoundException.class, () -> postsService.likePost(userId, postId));

        // Verify interactions
        verify(neo4jRepository, never()).createLikeRelation(any(UsersSocial.class), any(PostsSocial.class));
        verify(likeEventPublisher, never()).publish(any(LikeEvent.class));
    }

    @Test
    void likePost_shouldThrowIllegalArgumentException_whenParamsAreNull() {
        // Test null userId
        assertThrows(IllegalArgumentException.class, () -> postsService.likePost(null, UUID.randomUUID()));

        // Test null postId
        assertThrows(IllegalArgumentException.class, () -> postsService.likePost(UUID.randomUUID(), null));

        // Test both null
        assertThrows(IllegalArgumentException.class, () -> postsService.likePost(null, null));

        // Verify no interactions with dependencies
        verifyNoInteractions(neo4jRepository, likeEventPublisher, usersService);
    }

    @Test
    void unlikePost_shouldRemoveLikeRelationAndPublishEvent() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        // Configure mocks
        when(neo4jRepository.likeRelationExists(any(UsersSocial.class), any(PostsSocial.class))).thenReturn(true);

        // Execute the method
        postsService.unlikePost(userId, postId);

        // Verify interactions
        verify(neo4jRepository).removeLikeRelation(any(UsersSocial.class), any(PostsSocial.class));
        verify(likeEventPublisher).publish(any(LikeEvent.class));
    }

    @Test
    void unlikePost_shouldThrowNotFound_ifRelationDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        // Configure mocks
        when(neo4jRepository.likeRelationExists(any(UsersSocial.class), any(PostsSocial.class))).thenReturn(false);

        // Execute and verify exception
        assertThrows(NotFoundException.class, () -> postsService.unlikePost(userId, postId));

        // Verify no removal or publish happens
        verify(neo4jRepository, never()).removeLikeRelation(any(UsersSocial.class), any(PostsSocial.class));
        verify(likeEventPublisher, never()).publish(any(LikeEvent.class));
    }

    @Test
    void unlikePost_shouldThrowIllegalArgumentException_whenParamsAreNull() {
        // Test null userId
        assertThrows(IllegalArgumentException.class, () -> postsService.unlikePost(null, UUID.randomUUID()));

        // Test null postId
        assertThrows(IllegalArgumentException.class, () -> postsService.unlikePost(UUID.randomUUID(), null));

        // Test both null
        assertThrows(IllegalArgumentException.class, () -> postsService.unlikePost(null, null));

        // Verify no interactions with dependencies
        verifyNoInteractions(neo4jRepository, likeEventPublisher);
    }

    @Test
    void getLikesOfPostId_shouldReturnList_ifPostExists() {
        UUID postId = UUID.randomUUID();

        // Configure mocks
        when(neo4jRepository.postExists(any(PostsSocial.class))).thenReturn(true);
        when(neo4jRepository.getAllLikersFromPost(any(PostsSocial.class))).thenReturn(List.of(new UsersSocial()));

        // Execute the method
        List<UsersSocial> result = postsService.getLikesOfPostId(postId);

        // Verify result and interactions
        assertFalse(result.isEmpty());
        verify(neo4jRepository).getAllLikersFromPost(any(PostsSocial.class));
    }

    @Test
    void getLikesOfPostId_shouldThrowNotFound_ifPostDoesNotExist() {
        UUID postId = UUID.randomUUID();

        // Configure mocks for post not existing
        when(neo4jRepository.postExists(any(PostsSocial.class))).thenReturn(false);
        when(repoPostRestClient.getPostById(any(UUID.class))).thenReturn(null);

        // Execute and verify exception
        assertThrows(NotFoundException.class, () -> postsService.getLikesOfPostId(postId));

        // Verify interactions
        verify(neo4jRepository, never()).getAllLikersFromPost(any(PostsSocial.class));
    }

    @Test
    void postExistVerify_shouldReturnTrue_whenPostExistsInNeo4j() {
        PostsSocial post = new PostsSocial(UUID.randomUUID());

        // Configure mocks
        when(neo4jRepository.postExists(any(PostsSocial.class))).thenReturn(true);

        // Execute and verify
        assertTrue(postsService.postExistVerify(post));

        // Verify no call to client
        verify(repoPostRestClient, never()).getPostById(any(UUID.class));
    }

    @Test
    void postExistVerify_shouldReturnTrue_whenPostExistsInRestClient() {
        PostsSocial post = new PostsSocial(UUID.randomUUID());
        RepoPostResponse response = new RepoPostResponse();

        // Configure mocks
        when(neo4jRepository.postExists(any(PostsSocial.class))).thenReturn(false);
        when(repoPostRestClient.getPostById(any(UUID.class))).thenReturn(response);

        // Execute and verify
        assertTrue(postsService.postExistVerify(post));

        // Verify post is added to Neo4j
        verify(neo4jRepository).addPost(post);
    }

    @Test
    void postExistVerify_shouldReturnFalse_whenPostDoesNotExistAnywhere() {
        PostsSocial post = new PostsSocial(UUID.randomUUID());

        // Configure mocks
        when(neo4jRepository.postExists(any(PostsSocial.class))).thenReturn(false);
        when(repoPostRestClient.getPostById(any(UUID.class))).thenReturn(null);

        // Execute and verify
        assertFalse(postsService.postExistVerify(post));

        // Verify post is not added to Neo4j
        verify(neo4jRepository, never()).addPost(any(PostsSocial.class));
    }
}