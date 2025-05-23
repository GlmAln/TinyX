package com.epita.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
public class UsersSocial {
    private UUID id;

    public UsersSocial() {
        this.id = UUID.randomUUID();
    }
}
