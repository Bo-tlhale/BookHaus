package com.bookhaus.shelf;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

@WebMvcTest(ShelfController.class)
@Import({SecurityConfig.class, PasswordConfig.class, JwtAuthenticationFilter.class, JwtService.class})
@TestPropertySource(properties = {
        "bookhaus.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "bookhaus.jwt.expiration=60000"
})
class ShelfControllerSecurityTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean ShelfService shelfService;
    @MockitoBean UserDetailsServiceImpl userDetailsService;

    @Test
    void rejectsUnauthenticatedShelfRequests() throws Exception {
        mockMvc.perform(get("/api/shelves"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/shelves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Favorites\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedCreateCannotAssignShelfToSuppliedUserId() throws Exception {
        when(shelfService.createShelf(org.mockito.ArgumentMatchers.any())).thenReturn(null);

        mockMvc.perform(post("/api/shelves")
                        .with(user("reader@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Favorites\",\"userId\":999}"))
                .andExpect(status().isCreated());

        verify(shelfService).createShelf(argThat(request -> request.name().equals("Favorites")));
    }

    @Test
    void rejectsEmptyReorderList() throws Exception {
        mockMvc.perform(patch("/api/shelves/reorder")
                        .with(user("reader@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shelfIds\":[]}"))
                .andExpect(status().isBadRequest());
    }
}
