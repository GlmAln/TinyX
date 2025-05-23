#!/bin/bash

max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
  if curl -s http://localhost:9200 > /dev/null; then
    echo "Elasticsearch is ready!"
    break
  else
    echo "Waiting for Elasticsearch to start... (attempt $attempt)"
    sleep 10
    ((attempt++))
  fi
done

if [ $attempt -eq $max_attempts ]; then
  echo "Elasticsearch did not start in time"
  exit 1
fi

INDEX_EXISTS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9200/es_posts)

if [ $INDEX_EXISTS -eq 404 ]; then
  echo "Creating index es_posts..."
  curl -X PUT "http://localhost:9200/es_post" -H 'Content-Type: application/json' -d'
  {
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0,
      "analysis": {
        "analyzer": {
          "text_analyzer": {
            "type": "custom",
            "tokenizer": "standard",
            "filter": ["lowercase", "stop", "asciifolding"]
          }
        }
      }
    },
    "mappings": {
      "properties": {
        "id": { "type": "keyword" },
        "raw_text": { "type": "text" },
        "words": { "type": "text", "analyzer": "text_analyzer" },
        "hashtags": { "type": "text" }
      }
    }
  }'
else
  echo "Index es_post already exists."
fi

echo "Elasticsearch initialization completed."
