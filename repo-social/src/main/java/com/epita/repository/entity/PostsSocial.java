package com.epita.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
public class PostsSocial {
    private UUID id;

    public PostsSocial() {
        this.id = UUID.randomUUID();
    }
}
