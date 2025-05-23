package com.epita.service.entity;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class PostEntity {

    @Id
    private UUID id;
    @FullTextField
    private String rawText;
    @FullTextField
    private String words;
    @FullTextField
    private String hashtags;

    public PostEntity(UUID id, String raw_text) {
        this.id = id;
        this.rawText = raw_text;

        StringBuilder hashtagsBuilder = new StringBuilder();
        StringBuilder wordsBuilder = new StringBuilder();

        for (String word : raw_text.split("\\s+")) {
            if (word.startsWith("#")) {
                hashtagsBuilder
                        .append(word)
                        .append(" ");
            } else {
                wordsBuilder
                        .append(word)
                        .append(" ");
            }
        }

        this.hashtags = hashtagsBuilder
                .toString()
                .trim();
        this.words = wordsBuilder
                .toString()
                .trim();
    }
}
