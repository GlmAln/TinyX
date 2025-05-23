db.createUser(
    {
        user: "admin",
        pwd: "admin",
        roles: [
            {
                role: "readWrite",
                db: "Epitweet"
            }
        ]
    }
);
db.createCollection("Posts");
db.createCollection("Users");
db.createCollection("userTimelines");
db.createCollection("HomeTimeline");
