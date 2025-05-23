package com.epita.repository.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.UUID;

@Getter
@Setter
@ApplicationScoped
@AllArgsConstructor
@NoArgsConstructor
@MongoEntity(collection = "Users")
public class User {

    @BsonId
    private UUID id;

    private String name;
}
