package com.epita.repository;

import com.epita.repository.entity.User;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheMongoRepositoryBase<User, UUID> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);

    public User getUserById(UUID userId) {
        return find("_id", userId).firstResult();
    }

    public void createUser(User user) {
        try {
            persist(user);
            LOGGER.info("User created");
        } catch (Exception e) {
            LOGGER.error("Error while creating user: {}", e.getMessage());
            throw new RuntimeException("Error while creating user: " + e.getMessage(), e);
        }
    }

    public void deleteUser(UUID userId) {
        deleteById(userId);
    }
}
