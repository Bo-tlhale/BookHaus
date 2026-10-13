package com.bookhaus.library;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhaus.auth.JwtAuthenticationFilter;
import com.bookhaus.auth.JwtService;
import com.bookhaus.book.dto.AddBookRequest;
import com.bookhaus.config.PasswordConfig;
import com.bookhaus.config.SecurityConfig;
import com.bookhaus.user.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LibraryController.class)
@Import({SecurityConfig.class, PasswordConfig.class, JwtAuthenticationFilter.class, JwtService.class})
@TestPropertySource(properties = {
        "bookhaus.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "bookhaus.jwt.expiration=60000"
})
class LibraryControllerSecurityTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean LibraryService libraryService;
    @MockitoBean UserDetailsServiceImpl userDetailsService;

    @Test
    void rejectsUnauthenticatedAdd() throws Exception {
        mockMvc.perform(post("/api/library/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsAuthenticatedAddAndIgnoresAttemptedUserId() throws Exception {
        when(libraryService.addBook(org.mockito.ArgumentMatchers.any())).thenReturn(null);

        mockMvc.perform(post("/api/library/books")
                        .with(user("reader@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isCreated());

        verify(libraryService).addBook(argThat(request -> request.externalId().equals("/works/OL893415W")));
    }

    private String validJson() {
        return """
                {
                  "externalId":"/works/OL893415W",
                  "title":"Dune",
                  "authors":["Frank Herbert"],
                  "userId":999
                }
                """;
    }
}
