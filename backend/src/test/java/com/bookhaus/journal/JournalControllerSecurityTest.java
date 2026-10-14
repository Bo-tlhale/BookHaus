package com.bookhaus.journal;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhaus.auth.JwtAuthenticationFilter;
import com.bookhaus.auth.JwtService;
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

@WebMvcTest({ReadingJournalController.class, QuoteController.class})
@Import({SecurityConfig.class, PasswordConfig.class, JwtAuthenticationFilter.class, JwtService.class})
@TestPropertySource(properties = {
        "bookhaus.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "bookhaus.jwt.expiration=60000"
})
class JournalControllerSecurityTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean ReadingJournalService readingJournalService;
    @MockitoBean QuoteService quoteService;
    @MockitoBean UserDetailsServiceImpl userDetailsService;

    @Test
    void journalAndQuoteEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/library/books/12/journal"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/library/books/12/journal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/library/books/12/quotes"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/library/books/12/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quoteText\":\"Quote\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/library/books/12/quotes/30")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":null}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/library/books/12/quotes/30"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsPartialJournalWithoutUserIdOwnershipField() throws Exception {
        when(readingJournalService.upsertJournal(eq(12L), org.mockito.ArgumentMatchers.any())).thenReturn(null);

        mockMvc.perform(put("/api/library/books/12/journal")
                        .with(user("reader@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reflection\":\"My reflection\",\"userId\":999}"))
                .andExpect(status().isOk());

        verify(readingJournalService).upsertJournal(eq(12L),
                argThat(request -> request.reflection().equals("My reflection")));
    }

    @Test
    void rejectsBlankQuote() throws Exception {
        mockMvc.perform(post("/api/library/books/12/quotes")
                        .with(user("reader@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quoteText\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvalidPageNumber() throws Exception {
        mockMvc.perform(post("/api/library/books/12/quotes")
                        .with(user("reader@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quoteText\":\"A quote\",\"pageNumber\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsBlankQuoteTextWhenPatchSuppliesIt() throws Exception {
        mockMvc.perform(patch("/api/library/books/12/quotes/30")
                        .with(user("reader@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quoteText\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchCanExplicitlyClearOptionalQuoteFields() throws Exception {
        when(quoteService.updateQuote(eq(12L), eq(30L), org.mockito.ArgumentMatchers.any())).thenReturn(null);

        mockMvc.perform(patch("/api/library/books/12/quotes/30")
                        .with(user("reader@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pageNumber\":null,\"note\":null,\"userId\":999}"))
                .andExpect(status().isOk());

        verify(quoteService).updateQuote(eq(12L), eq(30L), argThat(request ->
                request.isPageNumberSupplied() && request.pageNumber() == null
                        && request.isNoteSupplied() && request.note() == null));
    }
}
