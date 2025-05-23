const request = require('supertest');

const token = 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwicm9sZSI6ImNvYWNoIiwiaWF0IjoxNzQyNTYzMTU0LCJleHAiOjE3NDI1NjY3NTR9.FF7g36Fge8FFx54U-HDZt6vK3SmIaa-zRJRZBnMf1pU';
const headers = (userId) => ({
    Authorization: token,
    'X-user-id': userId,
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
};

describe('E2E stress Social Flow', () => {
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
        user1_postId1 = res.body.id;
    });

    it('User2 creates 3000 posts', async () => {
        let successfulPosts = 0;
        let failedPosts = 0;
        const batchSize = 3000;

        // Traiter par petits lots pour éviter de surcharger le serveur
        const batchesOf = 150;
        const batches = Math.ceil(batchSize / batchesOf);

        for (let batch = 0; batch < batches; batch++) {
            const startIndex = batch * batchesOf;
            const endIndex = Math.min(startIndex + batchesOf, batchSize);
            const batchPromises = [];

            console.log(`Processing batch ${batch + 1}/${batches}: posts ${startIndex + 1}-${endIndex}`);

            for (let i = startIndex; i < endIndex; i++) {
                const postPromise = request(baseUrls.post)
                    .post('/api/posts')
                    .set(headers(users.user2))
                    .type('form')
                    .field('text', `Hello World ${i + 1}`)
                    .field('replyId', '')
                    .field('repostId', '')
                    .field('media', '')
                    .timeout(10000) // Augmenter le timeout à 10 secondes
                    .then(res => {
                        if (res.statusCode === 201) {
                            successfulPosts++;
                        } else {
                            failedPosts++;
                            console.error(`Post ${i + 1} failed with status: ${res.statusCode}`);
                        }
                    })
                    .catch(err => {
                        failedPosts++;
                        console.error(`Error at post ${i + 1}:`, err.message);
                    });

                batchPromises.push(postPromise);
            }

            // Attendre que le lot actuel soit traité avant de passer au suivant
            await Promise.all(batchPromises);

            // Attendre un peu entre les lots pour donner au serveur le temps de respirer
            if (batch < batches - 1) {
                await new Promise(resolve => setTimeout(resolve, 1000));
            }
        }

        console.log(`Successfully created ${successfulPosts}/${batchSize} posts.`);
        console.log(`Failed to create ${failedPosts}/${batchSize} posts.`);

        // Vous pouvez ajuster cette assertion selon vos besoins
        expect(successfulPosts).toBeGreaterThanOrEqual(batchSize);
    }, 40000);


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

    it('Get UserTimeline for user2', async () => {
        const res = await request(baseUrls.userTimeline)
            .get(`/api/users/timeline?userIds=${users.user2}`);

        expect(res.statusCode).toBe(200);
        console.log(res.body.length);
    });

    it('Get HomeTimeline for User1', async () => {
        const res = await request(baseUrls.homeTimeline)
            .get(`/api/users/${users.user1}/home-timeline`);

        expect(res.statusCode).toBe(200);
        console.log(res.body.length);
    });
});
