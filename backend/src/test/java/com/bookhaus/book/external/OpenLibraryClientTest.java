package com.bookhaus.book.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OpenLibraryClientTest {
    private MockRestServiceServer server;
    private OpenLibraryClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://openlibrary.test");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new OpenLibraryClient(builder.build());
    }

    @Test
    void mapsValidSearchResponseToInternalDto() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith("https://openlibrary.test/search.json")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("q", "The%20Hobbit"))
                .andExpect(queryParam("limit", "20"))
                .andRespond(withSuccess("""
                        {"docs":[{
                          "key":"/works/OL262758W",
                          "title":"The Hobbit",
                          "author_name":["J. R. R. Tolkien"],
                          "author_key":["OL26320A"],
                          "first_publish_year":1937,
                          "isbn":["0-261-10221-4","9780261102217"],
                          "publisher":["George Allen & Unwin"],
                          "subject":["Fantasy","Adventure"],
                          "cover_i":14627265,
                          "number_of_pages_median":310,
                          "first_sentence":["In a hole in the ground there lived a hobbit."]
                        }]}
                        """, MediaType.APPLICATION_JSON));

        var results = client.search("The Hobbit");

        assertThat(results).hasSize(1);
        var result = results.getFirst();
        assertThat(result.externalId()).isEqualTo("/works/OL262758W");
        assertThat(result.title()).isEqualTo("The Hobbit");
        assertThat(result.authors()).containsExactly("J. R. R. Tolkien");
        assertThat(result.isbn()).isEqualTo("9780261102217");
        assertThat(result.genres()).containsExactly("Fantasy", "Adventure");
        assertThat(result.coverUrl()).isEqualTo("https://covers.openlibrary.org/b/id/14627265-L.jpg");
        assertThat(result.description()).startsWith("In a hole");
        server.verify();
    }

    @Test
    void safelyMapsMissingOptionalMetadata() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith("https://openlibrary.test/search.json")))
                .andRespond(withSuccess("{\"docs\":[{\"key\":\"OL1W\",\"title\":\"Minimal Book\"}]}", MediaType.APPLICATION_JSON));

        var result = client.search("minimal").getFirst();

        assertThat(result.externalId()).isEqualTo("/works/OL1W");
        assertThat(result.authors()).isEmpty();
        assertThat(result.genres()).isEmpty();
        assertThat(result.isbn()).isNull();
        assertThat(result.coverUrl()).isNull();
    }

    @Test
    void translatesProviderFailureToControlledException() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith("https://openlibrary.test/search.json")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.search("book"))
                .isInstanceOf(BookProviderException.class)
                .hasMessageContaining("unavailable");
    }
}
