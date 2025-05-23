const { Client } = require('@elastic/elasticsearch');

// Adjust this to match your Docker Elasticsearch endpoint:
// For example, if you mapped "9200:9200" in Docker Compose and run on localhost:
const client = new Client({ node: 'http://localhost:9200' });

async function createIndex() {
    try {
        const indexName = 'es_posts';

        // Check if the index already exists
        const { body: exists } = await client.indices.exists({ index: indexName });
        if (!exists) {
            // Create the index with the desired mapping
            await client.indices.create({
                index: indexName,
                body: {
                    mappings: {
                        properties: {
                            id: {
                                type: 'keyword'
                            },
                            raw_text: {
                                type: 'text'
                            },
                            words: {
                                type: 'text'
                            },
                            hashtags: {
                                type: 'text'
                            }
                        }
                    }
                }
            });

            console.log(`Index "${indexName}" created successfully.`);
        } else {
            console.log(`Index "${indexName}" already exists.`);
        }
    } catch (error) {
        console.error('Error creating index:', error);
    }
}

// Run the script
createIndex();