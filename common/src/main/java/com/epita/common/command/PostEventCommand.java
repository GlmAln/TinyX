package com.epita.common.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a command for post-related events.
 * This class is used to encapsulate information about event types such as creation, deletion, likes, and unlikes.
 * A type is included to factorize the event handling process.
 */
@Getter
@AllArgsConstructor
public class PostEventCommand {

    /**
     * Enum representing the type of post event.
     */
    public enum Type {
        CREATION,
        DELETION,
        LIKE,
        UNLIKE,
    }

    /**
     * The type of the event.
     */
    private final Type type;

    /**
     * The unique identifier of the post associated with the event.
     */
    private final UUID postId;

    /**
     * The unique identifier of the user associated with the event.
     */
    private final UUID userId;

    /**
     * The optional text content associated with the event.
     */
    private final Optional<String> text;

    /**
     * The timestamp of when the event occurred.
     */
    private final LocalDateTime eventTime;
}