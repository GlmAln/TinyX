package com.epita.repository;

import com.epita.repository.entity.PostsSocial;
import com.epita.repository.entity.UsersSocial;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class Neo4jRepository {
    private static final Logger LOG = LoggerFactory.getLogger(Neo4jRepository.class);

    @Inject
    Driver neo4jDriver;

    public Neo4jRepository() {
        LOG.info("Initializing Neo4jRepository.");
        this.neo4jDriver = GraphDatabase.driver("bolt://localhost:7687");
        LOG.info("Neo4jRepository initialized successfully.");
    }

    public PostsSocial addPost(PostsSocial post) {
        LOG.info("Adding post with ID: {}", post.getId());
        try (var session = neo4jDriver.session()) {
            String createCypher = "CREATE (n:PostsSocial {id: $post_id}) RETURN n";
            var createdNode = session.executeWrite(tx -> tx
                    .run(createCypher, Map.of("post_id", post
                            .getId()
                            .toString()))
                    .single()
                    .get("n"));
            if (createdNode == null) {
                LOG.error("Failed to create post node for ID: {}", post.getId());
                throw new InternalServerErrorException("Neo4J error: Post Node could not be created");
            }
            LOG.info("Post with ID: {} added successfully.", post.getId());
            return post;
        } catch (Exception e) {
            LOG.error("Error while adding post with ID: {}", post.getId(), e);
            throw e;
        }
    }

    public UsersSocial addUser(UsersSocial user) {
        LOG.info("Adding user with ID: {}", user.getId());
        try (var session = neo4jDriver.session()) {
            String createCypher = "CREATE (n:UsersSocial {id: $user_id}) RETURN n";
            var createdNode = session.executeWrite(tx -> tx
                    .run(createCypher, Map.of("user_id", user
                            .getId()
                            .toString()))
                    .single()
                    .get("n"));
            if (createdNode == null) {
                LOG.error("Failed to create user node for ID: {}", user.getId());
                throw new InternalServerErrorException("Neo4J error: User Node could not be created");
            }
            LOG.info("User with ID: {} added successfully.", user.getId());
            return user;
        } catch (Exception e) {
            LOG.error("Error while adding user with ID: {}", user.getId(), e);
            throw e;
        }
    }

    public UsersSocial deleteUser(UsersSocial user) {
        LOG.info("Attempting to delete user with ID: {}", user.getId());
        try (var session = neo4jDriver.session()) {
            String deleteCypher = "MATCH (u:UsersSocial {id: $user_id}) DETACH DELETE u RETURN count(u) as deleted";
            Integer deleted = session.executeWrite(tx -> {
                var result = tx.run(deleteCypher, Map.of("user_id", user
                        .getId()
                        .toString()));
                var record = result.single();
                return record
                        .get("deleted")
                        .asInt();
            });

            if (deleted == 0) {
                LOG.error("User with ID: {} not found for deletion.", user.getId());
                throw new NotFoundException("User with id " + user.getId() + " not found");
            }

            LOG.info("Successfully deleted user with ID: {}", user.getId());
            return user;
        } catch (Exception e) {
            LOG.error("Error while deleting user with ID: {}", user.getId(), e);
            throw e;
        }
    }

    public PostsSocial deletePost(PostsSocial post) {
        LOG.info("Attempting to delete post with ID: {}", post.getId());
        try (var session = neo4jDriver.session()) {
            String deleteCypher = "MATCH (p:PostsSocial {id: $post_id}) DETACH DELETE p RETURN count(p) as deleted";
            Integer deleted = session.executeWrite(tx -> {
                var result = tx.run(deleteCypher, Map.of("post_id", post
                        .getId()
                        .toString()));
                var record = result.single();
                return record
                        .get("deleted")
                        .asInt();
            });

            if (deleted == 0) {
                LOG.error("Post with ID: {} not found for deletion.", post.getId());
                throw new NotFoundException("Post with id " + post.getId() + " not found");
            }

            LOG.info("Successfully deleted post with ID: {}", post.getId());
            return post;
        } catch (Exception e) {
            LOG.error("Error while deleting post with ID: {}", post.getId(), e);
            throw e;
        }
    }

    public boolean userExists(UsersSocial user) {
        LOG.debug("Checking if user exists with ID: {}", user.getId());
        try (var session = neo4jDriver.session()) {
            String existsCypher = "MATCH (u:UsersSocial {id: $user_id}) RETURN count(u) > 0 as exists";
            boolean exists = session.executeRead(tx -> tx
                    .run(existsCypher, Map.of("user_id", user
                            .getId()
                            .toString()))
                    .single()
                    .get("exists")
                    .asBoolean());
            LOG.debug("User existence check for ID: {} returned: {}", user.getId(), exists);
            return exists;
        } catch (Exception e) {
            LOG.error("Error while checking user existence for ID: {}", user.getId(), e);
            throw e;
        }
    }

    public boolean postExists(PostsSocial post) {
        LOG.debug("Checking if post exists with ID: {}", post.getId());
        try (var session = neo4jDriver.session()) {
            String existsCypher = "MATCH (p:PostsSocial {id: $post_id}) RETURN count(p) > 0 as exists";
            boolean exists = session.executeRead(tx -> tx
                    .run(existsCypher, Map.of("post_id", post
                            .getId()
                            .toString()))
                    .single()
                    .get("exists")
                    .asBoolean());
            LOG.debug("Post existence check for ID: {} returned: {}", post.getId(), exists);
            return exists;
        } catch (Exception e) {
            LOG.error("Error while checking post existence for ID: {}", post.getId(), e);
            throw e;
        }
    }

    public boolean createFollowRelation(UsersSocial user1, UsersSocial user2) {
        LOG.info("Creating follow relationship from User ID: {} to User ID: {}", user1.getId(), user2.getId());
        try (var session = neo4jDriver.session()) {
            String createRelationCypher =
                    "MATCH (user1:UsersSocial {id: $user1_id}), " +
                            "(user2:UsersSocial {id: $user2_id}) " +
                            "MERGE (user1)-[r:FOLLOW]->(user2) " +
                            "RETURN r";
            boolean result = session.executeWrite(tx -> {
                try {
                    var record = tx
                            .run(createRelationCypher, Map.of(
                                    "user1_id", user1
                                            .getId()
                                            .toString(),
                                    "user2_id", user2
                                            .getId()
                                            .toString()))
                            .single();
                    return record != null;
                } catch (Exception e) {
                    LOG.error("Error creating follow relationship from User ID: {} to User ID: {}", user1.getId(), user2.getId(), e);
                    return false;
                }
            });
            if (result) {
                LOG.info("Follow relationship created successfully from User ID: {} to User ID: {}", user1.getId(), user2.getId());
            } else {
                LOG.warn("Failed to create follow relationship from User ID: {} to User ID: {}", user1.getId(), user2.getId());
            }
            return result;
        }
    }

    public boolean createBlockRelation(UsersSocial user1, UsersSocial user2) {
        LOG.info("Creating block relationship from User ID: {} to User ID: {}", user1.getId(), user2.getId());
        try (var session = neo4jDriver.session()) {
            String createRelationCypher =
                    "MATCH (user1:UsersSocial {id: $user1_id}), " +
                            "(user2:UsersSocial {id: $user2_id}) " +
                            "CREATE (user1)-[r:BLOCK]->(user2) " +
                            "RETURN r";
            boolean result = session.executeWrite(tx -> {
                try {
                    var record = tx
                            .run(createRelationCypher, Map.of(
                                    "user1_id", user1
                                            .getId()
                                            .toString(),
                                    "user2_id", user2
                                            .getId()
                                            .toString()))
                            .single();
                    return record != null;
                } catch (Exception e) {
                    LOG.error("Error creating block relationship from User ID: {} to User ID: {}", user1.getId(), user2.getId(), e);
                    return false;
                }
            });
            if (result) {
                LOG.info("Block relationship created successfully from User ID: {} to User ID: {}", user1.getId(), user2.getId());
            } else {
                LOG.warn("Failed to create block relationship from User ID: {} to User ID: {}", user1.getId(), user2.getId());
            }
            return result;
        }
    }

    public boolean createLikeRelation(UsersSocial user, PostsSocial post) {
        LOG.info("Creating like relationship from User ID: {} to Post ID: {}", user.getId(), post.getId());
        try (var session = neo4jDriver.session()) {
            String createRelationCypher =
                    "MATCH (user:UsersSocial {id: $user_id}), " +
                            "(post:PostsSocial {id: $post_id}) " +
                            "CREATE (user)-[r:LIKE]->(post) " +
                            "RETURN r";
            boolean result = session.executeWrite(tx -> {
                try {
                    var record = tx
                            .run(createRelationCypher, Map.of(
                                    "user_id", user
                                            .getId()
                                            .toString(),
                                    "post_id", post
                                            .getId()
                                            .toString()))
                            .single();
                    return record != null;
                } catch (Exception e) {
                    LOG.error("Error creating like relationship from User ID: {} to Post ID: {}", user.getId(), post.getId(), e);
                    return false;
                }
            });
            if (result) {
                LOG.info("Like relationship created successfully from User ID: {} to Post ID: {}", user.getId(), post.getId());
            } else {
                LOG.warn("Failed to create like relationship from User ID: {} to Post ID: {}", user.getId(), post.getId());
            }
            return result;
        }
    }

    public boolean removeFollowRelation(UsersSocial user1, UsersSocial user2) {
        LOG.info("Removing follow relationship from User ID: {} to User ID: {}", user1.getId(), user2.getId());
        try (var session = neo4jDriver.session()) {
            String deleteRelationCypher =
                    "MATCH (user1:UsersSocial {id: $user1_id})-[r:FOLLOW]->(user2:UsersSocial {id: $user2_id}) " +
                            "DELETE r RETURN count(r) as deleted";
            boolean deleted = session.executeWrite(tx -> tx
                    .run(deleteRelationCypher, Map.of(
                            "user1_id", user1
                                    .getId()
                                    .toString(),
                            "user2_id", user2
                                    .getId()
                                    .toString()))
                    .single()
                    .get("deleted")
                    .asInt() > 0);
            if (deleted) {
                LOG.info("Successfully removed follow relationship from User ID: {} to User ID: {}", user1.getId(), user2.getId());
            } else {
                LOG.warn("No follow relationship found to remove from User ID: {} to User ID: {}", user1.getId(), user2.getId());
            }
            return deleted;
        } catch (Exception e) {
            LOG.error("Error while removing follow relationship from User ID: {} to User ID: {}", user1.getId(), user2.getId(), e);
            throw e;
        }
    }


    public boolean removeBlockRelation(UsersSocial user1, UsersSocial user2) {
        LOG.info("Removing block relationship from User ID: {} to User ID: {}", user1.getId(), user2.getId());
        try (var session = neo4jDriver.session()) {
            String deleteRelationCypher =
                    "MATCH (user1:UsersSocial {id: $user1_id})-[r:BLOCK]->(user2:UsersSocial {id: $user2_id}) " +
                            "DELETE r RETURN count(r) as deleted";
            boolean deleted = session.executeWrite(tx -> tx
                    .run(deleteRelationCypher, Map.of(
                            "user1_id", user1
                                    .getId()
                                    .toString(),
                            "user2_id", user2
                                    .getId()
                                    .toString()))
                    .single()
                    .get("deleted")
                    .asInt() > 0);
            if (deleted) {
                LOG.info("Successfully removed block relationship from User ID: {} to User ID: {}", user1.getId(), user2.getId());
            } else {
                LOG.warn("No block relationship found to remove from User ID: {} to User ID: {}", user1.getId(), user2.getId());
            }
            return deleted;
        } catch (Exception e) {
            LOG.error("Error while removing block relationship from User ID: {} to User ID: {}", user1.getId(), user2.getId(), e);
            throw e;
        }
    }

    public boolean removeLikeRelation(UsersSocial user, PostsSocial post) {
        LOG.info("Removing like relationship from User ID: {} to Post ID: {}", user.getId(), post.getId());
        try (var session = neo4jDriver.session()) {
            String deleteRelationCypher =
                    "MATCH (user:UsersSocial {id: $user_id})-[r:LIKE]->(post:PostsSocial {id: $post_id}) " +
                            "DELETE r RETURN count(r) as deleted";
            boolean deleted = session.executeWrite(tx -> tx
                    .run(deleteRelationCypher, Map.of(
                            "user_id", user
                                    .getId()
                                    .toString(),
                            "post_id", post
                                    .getId()
                                    .toString()))
                    .single()
                    .get("deleted")
                    .asInt() > 0);
            if (deleted) {
                LOG.info("Successfully removed like relationship from User ID: {} to Post ID: {}", user.getId(), post.getId());
            } else {
                LOG.warn("No like relationship found to remove from User ID: {} to Post ID: {}", user.getId(), post.getId());
            }
            return deleted;
        } catch (Exception e) {
            LOG.error("Error while removing like relationship from User ID: {} to Post ID: {}", user.getId(), post.getId(), e);
            throw e;
        }
    }

    public boolean followRelationExists(UsersSocial user1, UsersSocial user2) {
        LOG.debug("Checking if follow relationship exists from User ID: {} to User ID: {}", user1.getId(), user2.getId());
        try (var session = neo4jDriver.session()) {
            String checkRelationCypher =
                    "MATCH (user1:UsersSocial {id: $user1_id})-[r:FOLLOW]->(user2:UsersSocial {id: $user2_id}) " +
                            "RETURN count(r) > 0 as exists";
            boolean exists = session.executeRead(tx -> tx
                    .run(checkRelationCypher, Map.of(
                            "user1_id", user1
                                    .getId()
                                    .toString(),
                            "user2_id", user2
                                    .getId()
                                    .toString()))
                    .single()
                    .get("exists")
                    .asBoolean());
            LOG.debug("Follow relationship existence check returned: {}", exists);
            return exists;
        } catch (Exception e) {
            LOG.error("Error while checking follow relationship existence from User ID: {} to User ID: {}", user1.getId(), user2.getId(), e);
            throw e;
        }
    }

    public boolean blockRelationExists(UsersSocial user1, UsersSocial user2) {
        LOG.debug("Checking if block relationship exists from User ID: {} to User ID: {}", user1.getId(), user2.getId());
        try (var session = neo4jDriver.session()) {
            String checkRelationCypher =
                    "MATCH (user1:UsersSocial {id: $user1_id})-[r:BLOCK]->(user2:UsersSocial {id: $user2_id}) " +
                            "RETURN count(r) > 0 as exists";
            boolean exists = session.executeRead(tx -> tx
                    .run(checkRelationCypher, Map.of(
                            "user1_id", user1
                                    .getId()
                                    .toString(),
                            "user2_id", user2
                                    .getId()
                                    .toString()))
                    .single()
                    .get("exists")
                    .asBoolean());
            LOG.debug("Block relationship existence check returned: {}", exists);
            return exists;
        } catch (Exception e) {
            LOG.error("Error while checking block relationship existence from User ID: {} to User ID: {}", user1.getId(), user2.getId(), e);
            throw e;
        }
    }


    public boolean likeRelationExists(UsersSocial user, PostsSocial post) {
        LOG.debug("Checking if like relationship exists from User ID: {} to Post ID: {}", user.getId(), post.getId());
        try (var session = neo4jDriver.session()) {
            String checkRelationCypher =
                    "MATCH (user:UsersSocial {id: $user_id})-[r:LIKE]->(post:PostsSocial {id: $post_id}) " +
                            "RETURN count(r) > 0 as exists";
            boolean exists = session.executeRead(tx -> tx
                    .run(checkRelationCypher, Map.of(
                            "user_id", user
                                    .getId()
                                    .toString(),
                            "post_id", post
                                    .getId()
                                    .toString()))
                    .single()
                    .get("exists")
                    .asBoolean());
            LOG.debug("Like relationship existence check returned: {}", exists);
            return exists;
        } catch (Exception e) {
            LOG.error("Error while checking like relationship existence from User ID: {} to Post ID: {}", user.getId(), post.getId(), e);
            throw e;
        }
    }

    public List<UsersSocial> getAllFollowersOfUser(UsersSocial user) {
        LOG.info("Fetching all followers of User ID: {}", user.getId());
        try (var session = neo4jDriver.session()) {
            String getFollowersCypher =
                    "MATCH (follower:UsersSocial)-[:FOLLOW]->(user:UsersSocial {id: $user_id}) " +
                            "RETURN follower";
            return session.executeRead(tx -> {
                var result = tx.run(getFollowersCypher, Map.of("user_id", user
                        .getId()
                        .toString()));
                List<UsersSocial> followers = new ArrayList<>();
                while (result.hasNext()) {
                    var record = result.next();
                    var node = record
                            .get("follower")
                            .asNode();
                    UsersSocial follower = new UsersSocial(UUID.fromString(node
                            .get("id")
                            .asString()));
                    followers.add(follower);
                }
                LOG.info("Found {} followers for User ID: {}", followers.size(), user.getId());
                return followers;
            });
        } catch (Exception e) {
            LOG.error("Error while fetching followers for User ID: {}", user.getId(), e);
            throw e;
        }
    }

    public List<UsersSocial> getAllBlockersOfUser(UsersSocial user) {
        LOG.info("Fetching all blockers of User ID: {}", user.getId());
        try (var session = neo4jDriver.session()) {
            String getBlockersCypher =
                    "MATCH (blocker:UsersSocial)-[:BLOCK]->(user:UsersSocial {id: $user_id}) " +
                            "RETURN blocker";
            return session.executeRead(tx -> {
                var result = tx.run(getBlockersCypher, Map.of("user_id", user
                        .getId()
                        .toString()));
                List<UsersSocial> blockers = new ArrayList<>();
                while (result.hasNext()) {
                    var record = result.next();
                    var node = record
                            .get("blocker")
                            .asNode();
                    UsersSocial blocker = new UsersSocial(UUID.fromString(node
                            .get("id")
                            .asString()));
                    blockers.add(blocker);
                }
                LOG.info("Found {} blockers for User ID: {}", blockers.size(), user.getId());
                return blockers;
            });
        } catch (Exception e) {
            LOG.error("Error while fetching blockers for User ID: {}", user.getId(), e);
            throw e;
        }
    }

    public List<UsersSocial> getAllLikersFromPost(PostsSocial post) {
        LOG.info("Fetching all likers of Post ID: {}", post.getId());
        try (var session = neo4jDriver.session()) {
            String getLikersCypher =
                    "MATCH (liker:UsersSocial)-[:LIKE]->(post:PostsSocial {id: $post_id}) " +
                            "RETURN liker";
            return session.executeRead(tx -> {
                var result = tx.run(getLikersCypher, Map.of("post_id", post
                        .getId()
                        .toString()));
                List<UsersSocial> likers = new ArrayList<>();
                while (result.hasNext()) {
                    var record = result.next();
                    var node = record
                            .get("liker")
                            .asNode();
                    UsersSocial liker = new UsersSocial(UUID.fromString(node
                            .get("id")
                            .asString()));
                    likers.add(liker);
                }
                LOG.info("Found {} likers for Post ID: {}", likers.size(), post.getId());
                return likers;
            });
        } catch (Exception e) {
            LOG.error("Error while fetching likers for Post ID: {}", post.getId(), e);
            throw e;
        }
    }

    public List<UsersSocial> getAllFollowsOfUser(UsersSocial user) {
        LOG.info("Fetching all users followed by User ID: {}", user.getId());
        try (var session = neo4jDriver.session()) {
            String getFollowsCypher =
                    "MATCH (user:UsersSocial {id: $user_id})-[:FOLLOW]->(followee:UsersSocial) " +
                            "RETURN followee";
            return session.executeRead(tx -> {
                var result = tx.run(getFollowsCypher, Map.of("user_id", user
                        .getId()
                        .toString()));
                List<UsersSocial> followees = new ArrayList<>();
                while (result.hasNext()) {
                    var record = result.next();
                    var node = record
                            .get("followee")
                            .asNode();
                    UsersSocial followee = new UsersSocial(UUID.fromString(node
                            .get("id")
                            .asString()));
                    followees.add(followee);
                }
                LOG.info("Found {} followees for User ID: {}", followees.size(), user.getId());
                return followees;
            });
        } catch (Exception e) {
            LOG.error("Error while fetching followees for User ID: {}", user.getId(), e);
            throw e;
        }
    }

    public List<UsersSocial> getAllBlockedOfUser(UsersSocial user) {
        LOG.info("Fetching all users blocked by User ID: {}", user.getId());
        try (var session = neo4jDriver.session()) {
            String getBlockedCypher =
                    "MATCH (user:UsersSocial {id: $user_id})-[:BLOCK]->(blocked:UsersSocial) " +
                            "RETURN blocked";
            return session.executeRead(tx -> {
                var result = tx.run(getBlockedCypher, Map.of("user_id", user
                        .getId()
                        .toString()));
                List<UsersSocial> blockedUsers = new ArrayList<>();
                while (result.hasNext()) {
                    var record = result.next();
                    var node = record
                            .get("blocked")
                            .asNode();
                    UsersSocial blockedUser = new UsersSocial(UUID.fromString(node
                            .get("id")
                            .asString()));
                    blockedUsers.add(blockedUser);
                }
                LOG.info("Found {} blocked users for User ID: {}", blockedUsers.size(), user.getId());
                return blockedUsers;
            });
        } catch (Exception e) {
            LOG.error("Error while fetching blocked users for User ID: {}", user.getId(), e);
            throw e;
        }
    }

    public List<PostsSocial> getAllLikedPost(UsersSocial user) {
        LOG.info("Fetching all posts liked by User ID: {}", user.getId());
        try (var session = neo4jDriver.session()) {
            String getLikedPostsCypher =
                    "MATCH (user:UsersSocial {id: $user_id})-[:LIKE]->(post:PostsSocial) " +
                            "RETURN post";
            return session.executeRead(tx -> {
                var result = tx.run(getLikedPostsCypher, Map.of("user_id", user
                        .getId()
                        .toString()));
                List<PostsSocial> likedPosts = new ArrayList<>();
                while (result.hasNext()) {
                    var record = result.next();
                    var node = record
                            .get("post")
                            .asNode();
                    PostsSocial post = new PostsSocial(UUID.fromString(node
                            .get("id")
                            .asString()));
                    likedPosts.add(post);
                }
                LOG.info("Found {} liked posts for User ID: {}", likedPosts.size(), user.getId());
                return likedPosts;
            });
        } catch (Exception e) {
            LOG.error("Error while fetching liked posts for User ID: {}", user.getId(), e);
            throw e;
        }
    }
}