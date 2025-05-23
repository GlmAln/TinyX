package com.epita;

import com.epita.common.api.response.PostIdResponse;
import com.epita.service.UserTimelineService;
import com.epita.service.entity.PostEventEntity;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
public class UserTimelineTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    UserTimelineService userTimelineService;

    @Test
    public void basicAdditionSuccess() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        final PostEventEntity postEventEntity = new PostEventEntity(PostEventEntity.Type.CREATION, postId, userId, LocalDateTime.now());
        userTimelineService.applyPostEvent(postEventEntity);

        assertTimelineContains(List.of(userId), List.of(postId));
    }

    @Test
    public void deleteSingleItemWithOnlyOneInTimeline() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        userTimelineService.applyPostEvent(new PostEventEntity(PostEventEntity.Type.CREATION, postId, userId, LocalDateTime.now()));
        userTimelineService.applyPostEvent(new PostEventEntity(PostEventEntity.Type.DELETION, postId, userId, LocalDateTime.now()));

        assertTimelineContains(List.of(userId), List.of());
    }

    @Test
    public void deleteSingleItemWithMultipleInTimeline() {
        UUID postId1 = UUID.randomUUID();
        UUID postId2 = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        userTimelineService.applyPostEvent(new PostEventEntity(PostEventEntity.Type.CREATION, postId1, userId, LocalDateTime.now()));
        userTimelineService.applyPostEvent(new PostEventEntity(PostEventEntity.Type.CREATION, postId2, userId, LocalDateTime.now()));
        userTimelineService.applyPostEvent(new PostEventEntity(PostEventEntity.Type.DELETION, postId1, userId, LocalDateTime.now()));

        assertTimelineContains(List.of(userId), List.of(postId2));
    }

    @Test
    public void addMultipleItemsForMultipleUsers() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID post1 = UUID.randomUUID();
        UUID post2 = UUID.randomUUID();
        UUID post3 = UUID.randomUUID();

        userTimelineService.applyPostEvent(new PostEventEntity(PostEventEntity.Type.CREATION, post1, userId1, LocalDateTime.now()));
        userTimelineService.applyPostEvent(new PostEventEntity(PostEventEntity.Type.CREATION, post2, userId1, LocalDateTime.now()));
        userTimelineService.applyPostEvent(new PostEventEntity(PostEventEntity.Type.CREATION, post3, userId2, LocalDateTime.now()));

        assertTimelineContains(List.of(userId1), List.of(post1, post2));
        assertTimelineContains(List.of(userId2), List.of(post3));
    }

    @Test
    public void complexTimelineModification() {
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();

        LocalDateTime baseTime = LocalDateTime.now();

        UUID postA1 = UUID.randomUUID();
        UUID postA2 = UUID.randomUUID();
        UUID postA3 = UUID.randomUUID();

        UUID postB1 = UUID.randomUUID();
        UUID postB2 = UUID.randomUUID();
        UUID postB3 = UUID.randomUUID();

        userTimelineService.applyPostEvent(new PostEventEntity(PostEventEntity.Type.CREATION, postA1, userA, baseTime.plusSeconds(1)));
        userTimelineService.applyPostEvent(new PostEventEntity(PostEventEntity.Type.CREATION, postA2, userA, baseTime.plusSeconds(2)));
        userTimelineService.applyPostEvent(new PostEventEntity(PostEventEntity.Type.DELETION, postA2, userA, baseTime.plusSeconds(3)));
        userTimelineService.applyPostEvent(new PostEventEntity(PostEventEntity.Type.CREATION, postA3, userA, baseTime.plusSeconds(4)));

        userTimelineService.applyPostEvent(new PostEventEntity(PostEventEntity.Type.CREATION, postB1, userB, baseTime.plusSeconds(2)));
        userTimelineService.applyPostEvent(new PostEventEntity(PostEventEntity.Type.CREATION, postB2, userB, baseTime.plusSeconds(5)));
        userTimelineService.applyPostEvent(new PostEventEntity(PostEventEntity.Type.CREATION, postB3, userB, baseTime.plusSeconds(3)));
        userTimelineService.applyPostEvent(new PostEventEntity(PostEventEntity.Type.DELETION, postB3, userB, baseTime.plusSeconds(4)));

        assertTimelineContains(List.of(userA), List.of(postA1, postA3));
        assertTimelineContains(List.of(userB), List.of(postB1, postB2));

        assertTimelineContains(List.of(userA, userB), List.of(postA1, postB1, postA3, postB2));
    }


    private void assertTimelineContains(List<UUID> userIds, List<UUID> expectedPostIds) {
        List<PostIdResponse> expectedTimeline = expectedPostIds
                .stream()
                .map(PostIdResponse::new)
                .collect(Collectors.toList());

        String expectedJson;
        try {
            expectedJson = objectMapper.writeValueAsString(expectedTimeline);
        } catch (Exception e) {
            fail("Failed to serialize expected object to JSON: " + e.getMessage());
            return;
        }

        String joinedUserIds = userIds
                .stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));

        RestAssured
                .given()
                .queryParam("userIds", userIds)
                .when()
                .get("/api/users/timeline")
                .then()
                .statusCode(200)
                .body(equalTo(expectedJson));
    }

}
