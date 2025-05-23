package com.epita.service;

import com.epita.common.api.response.SearchPostResponse;
import com.epita.repository.SearchRepository;
import com.epita.repository.entity.PostModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Service for managing search functionality in the Repo-Search service.
 * Provides methods to fetch posts based on search queries containing hashtags and/or words.
 */
@ApplicationScoped
public class SearchService {

    private static final Logger LOG = LoggerFactory.getLogger(SearchService.class);

    @Inject
    SearchRepository searchRepository;

    /**
     * Fetches posts matching the given search query.
     * The query can include hashtags (prefixed with '#') and/or words.
     * Posts are searched in the respective fields and combined if both hashtags and words are present.
     *
     * @param query the search query containing hashtags and/or words.
     * @return a list of search post responses containing the IDs of matching posts.
     * @throws IOException if an error occurs during the search operation.
     */
    public List<SearchPostResponse> fetchPosts(final String query) throws IOException {
        LOG.info("Fetching posts for query: {}", query);

        Set<PostModel> posts = new HashSet<>();
        StringBuilder hashtagsBuilder = new StringBuilder();
        StringBuilder wordsBuilder = new StringBuilder();

        try {
            for (String word : query.split("\\s+")) {
                if (word.startsWith("#")) {
                    hashtagsBuilder
                            .append(word)
                            .append(" ");
                } else {
                    wordsBuilder
                            .append(word)
                            .append(" ");
                }
            }

            String hashtags = hashtagsBuilder
                    .toString()
                    .trim();
            String words = wordsBuilder
                    .toString()
                    .trim();

            LOG.debug("Extracted hashtags: '{}', words: '{}'", hashtags, words);

            if (!hashtags.isEmpty() && !words.isEmpty()) {
                LOG.debug("Searching in both fields: hashtags and words.");
                posts.addAll(searchRepository.searchInBothFields("hashtags", hashtags, "words", words));
            } else {
                if (words.isEmpty()) {
                    LOG.debug("Searching in hashtags field only.");
                    posts.addAll(searchRepository.searchInField("hashtags", hashtags));
                } else {
                    LOG.debug("Searching in words field only.");
                    posts.addAll(searchRepository.searchInField("words", words));
                }
            }

            List<UUID> postIds = posts
                    .stream()
                    .map(PostModel::getId)
                    .toList();
            LOG.debug("Found post IDs: {}", postIds);

            List<SearchPostResponse> searchPostResponses = new ArrayList<>();
            for (UUID postId : postIds) {
                searchPostResponses.add(new SearchPostResponse(postId));
            }

            LOG.info("Successfully fetched posts for query: {}", query);
            return searchPostResponses;

        } catch (Exception e) {
            LOG.error("Error while fetching posts for query: {}", query, e);
            throw e;
        }
    }
}
