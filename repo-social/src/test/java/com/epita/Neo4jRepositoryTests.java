package com.epita;

import com.epita.repository.Neo4jRepository;
import com.epita.repository.entity.PostsSocial;
import com.epita.repository.entity.UsersSocial;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class Neo4jRepositoryTests {

    private Neo4jRepository neo4jRepository = new Neo4jRepository();

    @Test
    void testUserExists() {
        // Create a user and add it to the database
        UsersSocial user = new UsersSocial();
        user.setId(UUID.randomUUID());
        neo4jRepository.addUser(user);

        // Check if the user exists
        boolean exists = neo4jRepository.userExists(user);

        assertTrue(exists);

        // Cleanup
        neo4jRepository.deleteUser(user);
    }

    @Test
    void testPostExists() {
        // Create a post and add it to the database
        PostsSocial post = new PostsSocial();
        post.setId(UUID.randomUUID());
        neo4jRepository.addPost(post);

        // Check if the post exists
        boolean exists = neo4jRepository.postExists(post);

        assertTrue(exists);

        // Cleanup
        neo4jRepository.deletePost(post);
    }

    @Test
    void testRemoveBlockRelation() {
        // Create two users
        UsersSocial user1 = new UsersSocial();
        user1.setId(UUID.randomUUID());
        UsersSocial user2 = new UsersSocial();
        user2.setId(UUID.randomUUID());

        // Add users to the database
        neo4jRepository.addUser(user1);
        neo4jRepository.addUser(user2);

        // Create a block relation
        neo4jRepository.createBlockRelation(user1, user2);

        // Remove the block relation
        boolean removed = neo4jRepository.removeBlockRelation(user1, user2);

        assertTrue(removed);

        // Cleanup
        neo4jRepository.deleteUser(user1);
        neo4jRepository.deleteUser(user2);
    }

    @Test
    void testRemoveLikeRelation() {
        // Create a user and a post
        UsersSocial user = new UsersSocial();
        user.setId(UUID.randomUUID());
        PostsSocial post = new PostsSocial();
        post.setId(UUID.randomUUID());

        // Add user and post to the database
        neo4jRepository.addUser(user);
        neo4jRepository.addPost(post);

        // Create a like relation
        neo4jRepository.createLikeRelation(user, post);

        // Remove the like relation
        boolean removed = neo4jRepository.removeLikeRelation(user, post);

        assertTrue(removed);

        // Cleanup
        neo4jRepository.deleteUser(user);
        neo4jRepository.deletePost(post);
    }

    @Test
    void testBlockRelationExists() {
        // Create two users
        UsersSocial user1 = new UsersSocial();
        user1.setId(UUID.randomUUID());
        UsersSocial user2 = new UsersSocial();
        user2.setId(UUID.randomUUID());

        // Add users to the database
        neo4jRepository.addUser(user1);
        neo4jRepository.addUser(user2);

        // Create a block relation
        neo4jRepository.createBlockRelation(user1, user2);

        // Check if block relation exists
        boolean exists = neo4jRepository.blockRelationExists(user1, user2);

        assertTrue(exists);

        // Cleanup
        neo4jRepository.deleteUser(user1);
        neo4jRepository.deleteUser(user2);
    }

    @Test
    void testLikeRelationExists() {
        // Create a user and a post
        UsersSocial user = new UsersSocial();
        user.setId(UUID.randomUUID());
        PostsSocial post = new PostsSocial();
        post.setId(UUID.randomUUID());

        // Add user and post to the database
        neo4jRepository.addUser(user);
        neo4jRepository.addPost(post);

        // Create a like relation
        neo4jRepository.createLikeRelation(user, post);

        // Check if like relation exists
        boolean exists = neo4jRepository.likeRelationExists(user, post);

        assertTrue(exists);

        // Cleanup
        neo4jRepository.deleteUser(user);
        neo4jRepository.deletePost(post);
    }

    @Test
    void testGetAllLikersFromPost() {
        // Create a post
        PostsSocial post = new PostsSocial();
        post.setId(UUID.randomUUID());
        neo4jRepository.addPost(post);

        // Create two users who like the post
        UsersSocial liker1 = new UsersSocial();
        liker1.setId(UUID.randomUUID());
        UsersSocial liker2 = new UsersSocial();
        liker2.setId(UUID.randomUUID());

        neo4jRepository.addUser(liker1);
        neo4jRepository.addUser(liker2);

        // Create like relations
        neo4jRepository.createLikeRelation(liker1, post);
        neo4jRepository.createLikeRelation(liker2, post);

        // Get all likers of the post
        List<UsersSocial> likers = neo4jRepository.getAllLikersFromPost(post);

        assertNotNull(likers);
        assertEquals(2, likers.size());

        // Cleanup
        neo4jRepository.deleteUser(liker1);
        neo4jRepository.deleteUser(liker2);
        neo4jRepository.deletePost(post);
    }

    @Test
    void testGetAllFollowsOfUser() {
        // Create a user
        UsersSocial user = new UsersSocial();
        user.setId(UUID.randomUUID());
        neo4jRepository.addUser(user);

        // Create two users to be followed
        UsersSocial followed1 = new UsersSocial();
        followed1.setId(UUID.randomUUID());
        UsersSocial followed2 = new UsersSocial();
        followed2.setId(UUID.randomUUID());

        neo4jRepository.addUser(followed1);
        neo4jRepository.addUser(followed2);

        // Create follow relations
        neo4jRepository.createFollowRelation(user, followed1);
        neo4jRepository.createFollowRelation(user, followed2);

        // Get all users followed by user
        List<UsersSocial> follows = neo4jRepository.getAllFollowsOfUser(user);

        assertNotNull(follows);
        assertEquals(2, follows.size());

        // Cleanup
        neo4jRepository.deleteUser(user);
        neo4jRepository.deleteUser(followed1);
        neo4jRepository.deleteUser(followed2);
    }

    @Test
    void testGetAllBlockedOfUser() {
        // Create a user
        UsersSocial user = new UsersSocial();
        user.setId(UUID.randomUUID());
        neo4jRepository.addUser(user);

        // Create two users to be blocked
        UsersSocial blocked1 = new UsersSocial();
        blocked1.setId(UUID.randomUUID());
        UsersSocial blocked2 = new UsersSocial();
        blocked2.setId(UUID.randomUUID());

        neo4jRepository.addUser(blocked1);
        neo4jRepository.addUser(blocked2);

        // Create block relations
        neo4jRepository.createBlockRelation(user, blocked1);
        neo4jRepository.createBlockRelation(user, blocked2);

        // Get all users blocked by user
        List<UsersSocial> blockedUsers = neo4jRepository.getAllBlockedOfUser(user);

        assertNotNull(blockedUsers);
        assertEquals(2, blockedUsers.size());

        // Cleanup
        neo4jRepository.deleteUser(user);
        neo4jRepository.deleteUser(blocked1);
        neo4jRepository.deleteUser(blocked2);
    }

    @Test
    void testGetAllLikedPost() {
        // Create a user
        UsersSocial user = new UsersSocial();
        user.setId(UUID.randomUUID());
        neo4jRepository.addUser(user);

        // Create two posts to be liked
        PostsSocial post1 = new PostsSocial();
        post1.setId(UUID.randomUUID());
        PostsSocial post2 = new PostsSocial();
        post2.setId(UUID.randomUUID());

        neo4jRepository.addPost(post1);
        neo4jRepository.addPost(post2);

        // Create like relations
        neo4jRepository.createLikeRelation(user, post1);
        neo4jRepository.createLikeRelation(user, post2);

        // Get all posts liked by user
        List<PostsSocial> likedPosts = neo4jRepository.getAllLikedPost(user);

        assertNotNull(likedPosts);
        assertEquals(2, likedPosts.size());

        // Cleanup
        neo4jRepository.deleteUser(user);
        neo4jRepository.deletePost(post1);
        neo4jRepository.deletePost(post2);
    }

    @Test
    void testDeleteUserNotFound() {
        // Create a user that is not in the database
        UsersSocial user = new UsersSocial();
        user.setId(UUID.randomUUID());

        // Try to delete the non-existent user
        assertThrows(NotFoundException.class, () -> {
            neo4jRepository.deleteUser(user);
        });
    }

    @Test
    void testDeletePostNotFound() {
        // Create a post that is not in the database
        PostsSocial post = new PostsSocial();
        post.setId(UUID.randomUUID());

        // Try to delete the non-existent post
        assertThrows(NotFoundException.class, () -> {
            neo4jRepository.deletePost(post);
        });
    }

    @Test
    void testNonExistentUserExists() {
        // Create a user that is not in the database
        UsersSocial user = new UsersSocial();
        user.setId(UUID.randomUUID());

        // Check if the user exists
        boolean exists = neo4jRepository.userExists(user);

        assertFalse(exists);
    }

    @Test
    void testNonExistentPostExists() {
        // Create a post that is not in the database
        PostsSocial post = new PostsSocial();
        post.setId(UUID.randomUUID());

        // Check if the post exists
        boolean exists = neo4jRepository.postExists(post);

        assertFalse(exists);
    }
}