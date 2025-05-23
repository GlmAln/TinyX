const request = require('supertest');

const token = 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwicm9sZSI6ImNvYWNoIiwiaWF0IjoxNzQyNTYzMTU0LCJleHAiOjE3NDI1NjY3NTR9.FF7g36Fge8FFx54U-HDZt6vK3SmIaa-zRJRZBnMf1pU';
const headers = (userId) => ({
    Authorization: token,
    'X-user-id': userId,
    'Content-Type': 'application/json',
});
const headers2 = (word) => ({
    Authorization: token,
    'X-Terms': word,
    'Content-Type': 'application/json',
});

const baseUrls = {
    post: 'http://localhost:8081',
    social: 'http://localhost:8082',
    search: 'http://localhost:8083',
    userTimeline: 'http://localhost:8084',
    homeTimeline: 'http://localhost:8085',
};

const users = {
    user1: '3fa85f64-5717-4562-b3fc-2c963f66afa1',
    user2: '3fa85f64-5717-4562-b3fc-2c963f66afa2',
    user3: '3fa85f64-5717-4562-b3fc-2c963f66afa3',
};

describe('E2E Social Flow', () => {
    let user1_postId1;
    let user2_postId1;
    let user3_postId1;
    let user3_postId2;
    let user3_postId3;

    it('User1 creates post 1', async () => {
        const res = await request(baseUrls.post)
            .post('/api/posts')
            .set(headers(users.user1))
            .type('form')
            .field('text', 'ewqew')
            .field('replyId', '')
            .field('repostId', '')
            .field('media', '');

        expect(res.statusCode).toBe(201);
        user1_postId1 = res.body.postId;
    });


    it('User2 creates post 2', async () => {
        const res = await request(baseUrls.post)
            .post('/api/posts')
            .set(headers(users.user2))
            .type('form')
            .field('text', 'ewqew')
            .field('replyId', '')
            .field('repostId', '')
            .field('media', '');

        expect(res.statusCode).toBe(201);
        user2_postId1 = res.body.postId;
    });

    it('User3 creates post 3', async () => {
        const res = await request(baseUrls.post)
            .post('/api/posts')
            .set(headers(users.user3))
            .type('form')
            .field('text', 'ewqew')
            .field('replyId', '')
            .field('repostId', '')
            .field('media', '');

        expect(res.statusCode).toBe(201);
        user3_postId1 = res.body.postId;

    });

    it('User3 replies to User1 post 1', async () => {
        const res = await request(baseUrls.post)
            .post('/api/posts')
            .set(headers(users.user3))
            .type('form')
            .field('text', "replyTo: 'Hello World 2'")
            .field('replyId', user1_postId1)
            .field('repostId', '')
            .field('media', '');

        expect(res.statusCode).toBe(201);
        user3_postId2 = res.body.postId;
    });


    it('User3 reposts to User2 post 1', async () => {
        const res = await request(baseUrls.post)
            .post('/api/posts')
            .set(headers(users.user3))
            .type('form')
            .field('text', "replyTo: 'Hello World 3'")
            .field('replyId', user2_postId1)
            .field('repostId', '')
            .field('media', '');

        expect(res.statusCode).toBe(201);
        user3_postId3 = res.body.postId;
    });

    it('Get UserTimeline of user2', async () => {
        const res = await request(baseUrls.userTimeline)
            .get(`/api/users/timeline?userIds=${users.user2}`);

        expect(res.statusCode).toBe(200);
        expect(Array.isArray(res.body)).toBe(true);

        const postIdsRes = res.body.map(post => post.postId);
        expect(postIdsRes).toContain(user2_postId1);
    });

    it('User1 follows User2', async () => {
        await request(baseUrls.social)
            .delete('/api/repo-social/users/unblock')
            .set(headers(users.user1))
            .send(`"${users.user2}"`);

        const res = await request(baseUrls.social)
            .post('/api/repo-social/users/follow')
            .set(headers(users.user1))
            .send(`"${users.user2}"`);

        expect(res.statusCode).toBe(201);
    });


    it('User1 follows User3', async () => {
        const res = await request(baseUrls.social)
            .post('/api/repo-social/users/follow')
            .set(headers(users.user1))
            .send(`"${users.user3}"`);

        expect(res.statusCode).toBe(201);
    });

    it('Get HomeTimeline for User1', async () => {
        const res = await request(baseUrls.homeTimeline)
            .get(`/api/users/${users.user1}/home-timeline`);

        expect(res.statusCode).toBe(200);
    });

    it('User1 blocks User2', async () => {
        const res = await request(baseUrls.social)
            .post('/api/repo-social/users/block')
            .set(headers(users.user1))
            .send(`"${users.user2}"`);

        expect(res.statusCode).toBe(201);
    });

    it('User1 unfollows User2', async () => {
        const res = await request(baseUrls.social)
            .delete('/api/repo-social/users/unfollow')
            .set(headers(users.user1))
            .send(`"${users.user2}"`);

        expect(res.statusCode).toBe(404);
    });

    it('User1 unfollows User3', async () => {
        const res = await request(baseUrls.social)
            .delete('/api/repo-social/users/unfollow')
            .set(headers(users.user1))
            .send(`"${users.user3}"`);

        expect(res.statusCode).toBe(204);
    });

    it('Get followers of User2', async () => {
        const res = await request(baseUrls.social)
            .get(`/api/repo-social/users/${users.user2}/followers`);

        expect(res.statusCode).toBe(200);
    });

    it('Search for "world"', async () => {
        const res = await request(baseUrls.search)
            .get('/search')
            .set(headers2('world'));

        expect(res.statusCode).toBe(200);
    });

    it('Get UserTimeline for user3', async () => {
        const res = await request(baseUrls.userTimeline)
            .get(`/api/users/timeline?userIds=${users.user3}`);

        expect(res.statusCode).toBe(200);
    });

    it('Get HomeTimeline for user1 again', async () => {
        const res = await request(baseUrls.homeTimeline)
            .get(`/api/users/${users.user1}/home-timeline`);

        expect(res.statusCode).toBe(200);
    });

    it('Get a specific post by postId', async () => {
        const res = await request(baseUrls.post)
            .get(`/api/posts/${user1_postId1}`)
            .set(headers(users.user1));

        expect(res.statusCode).toBe(200);
        expect(res.body.id).toBe(user1_postId1);
    });

    it('User1 deletes a post', async () => {
        const res = await request(baseUrls.post)
            .delete(`/api/posts/${user1_postId1}`)
            .set(headers(users.user1));

        expect(res.statusCode).toBe(204);
    });

    it('Get posts by a specific user', async () => {
        const res = await request(baseUrls.post)
            .get(`/api/users/${users.user1}/posts`);

        expect(res.statusCode).toBe(200);
        expect(Array.isArray(res.body)).toBe(true);
    });

    it('Get replies to a specific post', async () => {
        const res = await request(baseUrls.post)
            .get(`/api/posts/${user2_postId1}/replies`);

        expect(res.statusCode).toBe(200);
        expect(Array.isArray(res.body)).toBe(true);
    });

});
