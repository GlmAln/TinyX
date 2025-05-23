package com.epita.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.epita.repository.entity.PostModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class SearchRepository {

    private static final Logger LOG = LoggerFactory.getLogger(SearchRepository.class);

    @Inject
    ElasticsearchClient elasticsearchClient;

    public void index(final PostModel post) throws IOException {
        LOG.info("Indexing post with ID: {}", post.getId());
        try {
            Map<String, Object> doc = new HashMap<>();
            doc.put("id", post
                    .getId()
                    .toString());
            doc.put("raw_text", post.getRaw_text());
            doc.put("words", post.getWords());
            doc.put("hashtags", post.getHashtags());

            elasticsearchClient.index(i -> i
                    .index("es_posts")
                    .id(post
                            .getId()
                            .toString())
                    .document(doc)
            );

            LOG.info("Successfully indexed post with ID: {}", post.getId());
        } catch (Exception e) {
            LOG.error("Error while indexing post with ID: {}", post.getId(), e);
            throw e;
        }
    }

    public void deleteById(String id) throws IOException {
        LOG.info("Deleting post with ID: {}", id);
        try {
            elasticsearchClient.delete(i -> i
                    .index("es_posts")
                    .id(id));
            LOG.info("Successfully deleted post with ID: {}", id);
        } catch (Exception e) {
            LOG.error("Error while deleting post with ID: {}", id, e);
            throw e;
        }
    }

    public void deleteAll() throws IOException {
        LOG.info("Deleting all posts from the index.");
        try {
            elasticsearchClient.deleteByQuery(req -> req
                    .index("es_posts")
                    .query(q -> q.matchAll(m -> m))
            );
            LOG.info("Successfully deleted all posts from the index.");
        } catch (Exception e) {
            LOG.error("Error while deleting all posts from the index.", e);
            throw e;
        }
    }

    public void refresh() throws IOException {
        LOG.info("Refreshing the index.");
        try {
            elasticsearchClient
                    .indices()
                    .refresh(r -> r.index("es_posts"));
            LOG.info("Successfully refreshed the index.");
        } catch (Exception e) {
            LOG.error("Error while refreshing the index.", e);
            throw e;
        }
    }

    public List<PostModel> searchInField(String field, String term) throws IOException {
        LOG.info("Searching in field '{}' for term: {}", field, term);
        try {
            SearchResponse<PostModel> response = null;
            if (field.equals("words")) {
                response = elasticsearchClient.search(s -> s
                                .index("es_posts")
                                .query(q -> q.match(m -> m
                                        .field(field)
                                        .query(term))),
                        PostModel.class
                );
            } else if (field.equals("hashtags")) {
                response = elasticsearchClient.search(s -> s
                                .index("es_posts")
                                .query(q -> q.bool(b -> b
                                        .must(m -> m.match(mm -> mm
                                                .field(field)
                                                .query(term)
                                                .operator(Operator.And)
                                        ))
                                )),
                        PostModel.class
                );
            }

            if (response == null) {
                LOG.warn("No response received for search in field '{}' with term: {}", field, term);
                return Collections.emptyList();
            }

            List<PostModel> results = response
                    .hits()
                    .hits()
                    .stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            LOG.info("Found {} results for search in field '{}' with term: {}", results.size(), field, term);
            return results;
        } catch (Exception e) {
            LOG.error("Error while searching in field '{}' with term: {}", field, term, e);
            throw e;
        }
    }

    public List<PostModel> searchInBothFields(String strictField, String strictTerm, String vagueField, String vagueTerm) throws IOException {
        LOG.info("Searching in both fields. Strict field: '{}', Strict term: '{}', Vague field: '{}', Vague term: '{}'",
                strictField, strictTerm, vagueField, vagueTerm);
        try {
            var response = elasticsearchClient.search(s -> s
                            .index("es_posts")
                            .query(q -> q.bool(b -> b
                                    .must(m -> m.match(mm -> mm
                                            .field(strictField)
                                            .query(strictTerm)
                                            .operator(Operator.And)
                                    ))
                                    .must(m -> m.match(mm -> mm
                                            .field(vagueField)
                                            .query(vagueTerm)
                                    ))
                            )),
                    PostModel.class
            );

            List<PostModel> results = response
                    .hits()
                    .hits()
                    .stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            LOG.info("Found {} results for combined search.", results.size());
            return results;
        } catch (Exception e) {
            LOG.error("Error while searching in both fields. Strict field: '{}', Strict term: '{}', Vague field: '{}', Vague term: '{}'",
                    strictField, strictTerm, vagueField, vagueTerm, e);
            throw e;
        }
    }

    public List<PostModel> searchAll() throws IOException {
        LOG.info("Searching all posts.");
        try {
            var response = elasticsearchClient.search(s -> s
                            .index("es_posts")
                            .query(q -> q.matchAll(m -> m)),
                    PostModel.class
            );

            List<PostModel> results = response
                    .hits()
                    .hits()
                    .stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            LOG.info("Found {} posts in total.", results.size());
            return results;
        } catch (Exception e) {
            LOG.error("Error while searching all posts.", e);
            throw e;
        }
    }
}
