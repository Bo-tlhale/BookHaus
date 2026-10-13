package com.bookhaus.book.external;

import com.bookhaus.book.dto.BookSearchResult;
import com.bookhaus.book.external.dto.OpenLibrarySearchResponse;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OpenLibraryClient implements BookProvider {
    private static final String COVER_BASE_URL = "https://covers.openlibrary.org";
    private static final String SEARCH_FIELDS = String.join(",",
            "key", "title", "author_name", "author_key", "first_publish_year", "isbn",
            "publisher", "subject", "cover_i", "number_of_pages_median", "first_sentence");
    private static final Pattern WORK_ID = Pattern.compile("(?:/works/)?(OL\\d+W)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ISBN_10 = Pattern.compile("\\d{9}[\\dX]");
    private static final Pattern ISBN_13 = Pattern.compile("\\d{13}");

    private final RestClient restClient;

    @Autowired
    public OpenLibraryClient(
            RestClient.Builder builder,
            @Value("${bookhaus.open-library.base-url:https://openlibrary.org}") String baseUrl,
            @Value("${bookhaus.open-library.connect-timeout:3s}") Duration connectTimeout,
            @Value("${bookhaus.open-library.read-timeout:30s}") Duration readTimeout
    ) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.USER_AGENT, "BookHaus/0.0.1")
                .requestFactory(requestFactory)
                .build();
    }

    OpenLibraryClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<BookSearchResult> search(String query) {
        try {
            OpenLibrarySearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search.json")
                            .queryParam("q", query)
                            .queryParam("fields", SEARCH_FIELDS)
                            .queryParam("limit", 20)
                            .build())
                    .retrieve()
                    .body(OpenLibrarySearchResponse.class);

            if (response == null || response.docs() == null) {
                throw new BookProviderException("Open Library returned a malformed response");
            }
            return response.docs().stream()
                    .filter(Objects::nonNull)
                    .map(this::map)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (BookProviderException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BookProviderException("Open Library is currently unavailable", exception);
        }
    }

    private BookSearchResult map(OpenLibrarySearchResponse.Document document) {
        String externalId = normalizeWorkId(document.key());
        String title = clean(document.title());
        if (externalId == null || title == null) {
            return null;
        }

        return new BookSearchResult(
                externalId,
                title,
                cleanDistinct(document.authorNames(), 20),
                extractDescription(document.firstSentence()),
                selectIsbn(document.isbn()),
                document.coverId() == null ? null : COVER_BASE_URL + "/b/id/" + document.coverId() + "-L.jpg",
                cleanDistinct(document.subject(), 10),
                firstClean(document.publisher()),
                positive(document.firstPublishYear()),
                positive(document.pageCount())
        );
    }

    public static String normalizeWorkId(String value) {
        if (value == null) {
            return null;
        }
        var matcher = WORK_ID.matcher(value.trim());
        return matcher.matches() ? "/works/" + matcher.group(1).toUpperCase(Locale.ROOT) : null;
    }

    private String selectIsbn(List<String> values) {
        if (values == null) {
            return null;
        }
        List<String> normalized = values.stream().map(OpenLibraryClient::normalizeIsbn).filter(Objects::nonNull).toList();
        return normalized.stream().filter(value -> ISBN_13.matcher(value).matches()).findFirst()
                .orElseGet(() -> normalized.stream().filter(value -> ISBN_10.matcher(value).matches()).findFirst().orElse(null));
    }

    public static String normalizeIsbn(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replaceAll("[-\\s]", "").toUpperCase(Locale.ROOT);
        return ISBN_10.matcher(normalized).matches() || ISBN_13.matcher(normalized).matches() ? normalized : null;
    }

    private String extractDescription(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return clean(node.textValue());
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                if (item.isTextual() && clean(item.textValue()) != null) {
                    return clean(item.textValue());
                }
            }
        }
        return null;
    }

    private static List<String> cleanDistinct(List<String> values, int limit) {
        if (values == null) {
            return List.of();
        }
        Map<String, String> unique = new LinkedHashMap<>();
        for (String value : values) {
            String cleaned = clean(value);
            if (cleaned != null) {
                unique.putIfAbsent(cleaned.toLowerCase(Locale.ROOT), cleaned);
            }
            if (unique.size() == limit) {
                break;
            }
        }
        return new ArrayList<>(unique.values());
    }

    private static String firstClean(List<String> values) {
        return values == null ? null : values.stream().map(OpenLibraryClient::clean).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim().replaceAll("\\s+", " ");
        return cleaned.isEmpty() ? null : cleaned;
    }

    private static Integer positive(Integer value) {
        return value != null && value > 0 ? value : null;
    }
}
