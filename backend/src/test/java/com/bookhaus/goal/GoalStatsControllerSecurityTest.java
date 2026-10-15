package com.bookhaus.goal;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhaus.auth.JwtAuthenticationFilter;
import com.bookhaus.auth.JwtService;
import com.bookhaus.config.PasswordConfig;
import com.bookhaus.config.SecurityConfig;
import com.bookhaus.stats.StatsController;
import com.bookhaus.stats.StatsService;
import com.bookhaus.user.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ReadingGoalController.class, StatsController.class})
@Import({SecurityConfig.class, PasswordConfig.class, JwtAuthenticationFilter.class, JwtService.class})
@TestPropertySource(properties = {
        "bookhaus.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "bookhaus.jwt.expiration=60000"
})
class GoalStatsControllerSecurityTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean ReadingGoalService readingGoalService;
    @MockitoBean StatsService statsService;
    @MockitoBean UserDetailsServiceImpl userDetailsService;

    @Test
    void goalAndStatisticsEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"year\":2026,\"targetBooks\":12}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/stats/year/2026"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidGoalYearAndTarget() throws Exception {
        mockMvc.perform(post("/api/goals")
                        .with(user("reader@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"year\":1899,\"targetBooks\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authenticatedCreateCannotAssignGoalToSuppliedUserId() throws Exception {
        when(readingGoalService.createGoal(org.mockito.ArgumentMatchers.any())).thenReturn(null);

        mockMvc.perform(post("/api/goals")
                        .with(user("reader@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"year\":2026,\"targetBooks\":12,\"userId\":999}"))
                .andExpect(status().isCreated());

        verify(readingGoalService).createGoal(argThat(request ->
                request.year() == 2026 && request.targetBooks() == 12));
    }

    @Test
    void rejectsInvalidYearPath() throws Exception {
        mockMvc.perform(get("/api/stats/year/1000")
                        .with(user("reader@example.com")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authenticatedYearlyStatsUsesOnlyPathYear() throws Exception {
        when(statsService.getYearlyStats(2025)).thenReturn(null);

        mockMvc.perform(get("/api/stats/year/2025")
                        .with(user("reader@example.com")))
                .andExpect(status().isOk());

        verify(statsService).getYearlyStats(eq(2025));
    }
}
