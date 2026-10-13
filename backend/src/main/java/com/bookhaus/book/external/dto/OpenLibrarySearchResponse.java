package com.bookhaus.book.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenLibrarySearchResponse(List<Document> docs) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Document(
            String key,
            String title,
            @JsonProperty("author_name") List<String> authorNames,
            @JsonProperty("author_key") List<String> authorKeys,
            @JsonProperty("first_publish_year") Integer firstPublishYear,
            List<String> isbn,
            List<String> publisher,
            List<String> subject,
            @JsonProperty("cover_i") Long coverId,
            @JsonProperty("number_of_pages_median") Integer pageCount,
            @JsonProperty("first_sentence") JsonNode firstSentence
    ) {
    }
}
