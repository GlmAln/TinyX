package com.epita.repository.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class PostModel {

    @Id
    public UUID id;
    @FullTextField
    private String raw_text;
    @FullTextField
    private String words;
    @FullTextField
    private String hashtags;

    public PostModel(UUID id, String raw_text) {
        this.id = id;
        this.raw_text = raw_text;

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
