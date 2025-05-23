package com.epita;

import com.epita.repository.FollowEventPublisher;
import com.epita.repository.Neo4jRepository;
import com.epita.repository.RepoPostRestClient;
import com.epita.repository.entity.FollowEvent;
import com.epita.repository.entity.PostsSocial;
import com.epita.repository.entity.UsersSocial;
import com.epita.service.UsersService;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsersServiceTests {

    private UsersService usersService;
    private Neo4jRepository neo4jRepository;
    private FollowEventPublisher followEventPublisher;
    private RepoPostRestClient repoPostRestClient;

    @BeforeEach
    void setUp() {
        neo4jRepository = mock(Neo4jRepository.class);
        followEventPublisher = mock(FollowEventPublisher.class);
        repoPostRestClient = mock(RepoPostRestClient.class);

        usersService = new UsersService();
        usersService.neo4jRepository = neo4jRepository;
        usersService.followEventPublisher = followEventPublisher;
        usersService.repoPostRestClient = repoPostRestClient;
    }

    @Test
    void followUser_shouldCreateFollowRelationAndPublishEvent() {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();

        when(neo4jRepository.userExists(any(UsersSocial.class))).thenReturn(true);
        when(neo4jRepository.blockRelationExists(any(), any())).thenReturn(false);
        when(neo4jRepository.followRelationExists(any(), any())).thenReturn(false);


        usersService.followUser(followerId, followeeId);

        verify(neo4jRepository).createFollowRelation(any(), any());
        verify(followEventPublisher).publish(any(FollowEvent.class));
    }

    @Test
    void followUser_shouldThrowNotFound_ifUserDoesNotExist() {
        when(repoPostRestClient.getUserById(any(UUID.class))).thenReturn(null);
        when(neo4jRepository.userExists(any(UsersSocial.class))).thenReturn(false);


        assertThrows(NotFoundException.class,
                () -> usersService.followUser(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void followUser_shouldThrowForbidden_ifBlocked() {
        when(neo4jRepository.userExists(any(UsersSocial.class))).thenReturn(true);
        when(neo4jRepository.blockRelationExists(any(), any())).thenReturn(true);

        assertThrows(ForbiddenException.class,
                () -> usersService.followUser(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void unfollowUser_shouldRemoveRelationAndPublishEvent() {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();

        when(neo4jRepository.followRelationExists(any(), any())).thenReturn(true);

        usersService.unfollowUser(followerId, followeeId);

        verify(neo4jRepository).removeFollowRelation(any(), any());
        verify(followEventPublisher).publish(any(FollowEvent.class));
    }

    @Test
    void unfollowUser_shouldThrowNotFound_ifRelationDoesNotExist() {
        when(neo4jRepository.followRelationExists(any(), any())).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> usersService.unfollowUser(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void blockUser_shouldCreateBlockAndRemoveFollows() {
        when(neo4jRepository.userExists(any(UsersSocial.class))).thenReturn(true);
        when(neo4jRepository.followRelationExists(any(), any())).thenReturn(true);
        when(neo4jRepository.blockRelationExists(any(), any())).thenReturn(false);

        usersService.blockUser(UUID.randomUUID(), UUID.randomUUID());

        verify(neo4jRepository, times(2)).removeFollowRelation(any(), any());
        verify(neo4jRepository).createBlockRelation(any(), any());
    }

    @Test
    void blockUser_shouldThrowNotFound_ifUserMissing() {
        when(neo4jRepository.userExists(any(UsersSocial.class))).thenReturn(false);
        when(repoPostRestClient.getUserById(any(UUID.class))).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> usersService.blockUser(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void unblockUser_shouldRemoveBlock() {
        when(neo4jRepository.blockRelationExists(any(), any())).thenReturn(true);

        usersService.unblockUser(UUID.randomUUID(), UUID.randomUUID());

        verify(neo4jRepository).removeBlockRelation(any(), any());
    }

    @Test
    void unblockUser_shouldThrowNotFound_ifNoBlock() {
        when(neo4jRepository.blockRelationExists(any(), any())).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> usersService.unblockUser(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void getLikedPosts_shouldReturnList_ifUserExists() {
        UUID userId = UUID.randomUUID();
        when(neo4jRepository.userExists(any(UsersSocial.class))).thenReturn(true);
        when(neo4jRepository.getAllLikedPost(any())).thenReturn(List.of(new PostsSocial()));

        List<PostsSocial> posts = usersService.getLikedPosts(userId);

        assertFalse(posts.isEmpty());
    }

    @Test
    void getFollowees_shouldReturnList_ifUserExists() {
        UUID userId = UUID.randomUUID();
        when(neo4jRepository.userExists(any(UsersSocial.class))).thenReturn(true);
        when(neo4jRepository.getAllFollowsOfUser(any())).thenReturn(List.of(new UsersSocial()));

        List<UsersSocial> result = usersService.getFollowees(userId);

        assertFalse(result.isEmpty());
    }

    @Test
    void getFollowers_shouldReturnList_ifUserExists() {
        UUID userId = UUID.randomUUID();
        when(neo4jRepository.userExists(any(UsersSocial.class))).thenReturn(true);
        when(neo4jRepository.getAllFollowersOfUser(any())).thenReturn(List.of(new UsersSocial()));

        List<UsersSocial> result = usersService.getFollowers(userId);

        assertFalse(result.isEmpty());
    }

    @Test
    void getBlockedUsers_shouldReturnList_ifUserExists() {
        UUID userId = UUID.randomUUID();
        when(neo4jRepository.userExists(any(UsersSocial.class))).thenReturn(true);
        when(neo4jRepository.getAllBlockedOfUser(any())).thenReturn(List.of(new UsersSocial()));

        List<UsersSocial> result = usersService.getBlockedUsers(userId);

        assertFalse(result.isEmpty());
    }

    @Test
    void getUsersWhoBlockedMe_shouldReturnList_ifUserExists() {
        UUID userId = UUID.randomUUID();
        when(neo4jRepository.userExists(any(UsersSocial.class))).thenReturn(true);
        when(neo4jRepository.getAllBlockersOfUser(any())).thenReturn(List.of(new UsersSocial()));

        List<UsersSocial> result = usersService.getUsersWhoBlockedMe(userId);

        assertFalse(result.isEmpty());
    }

    @Test
    void getFollowees_shouldThrowNotFound_ifUserMissing() {
        when(neo4jRepository.userExists(any(UsersSocial.class))).thenReturn(false);
        when(repoPostRestClient.getUserById(any(UUID.class))).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> usersService.getFollowees(UUID.randomUUID()));
    }
}
