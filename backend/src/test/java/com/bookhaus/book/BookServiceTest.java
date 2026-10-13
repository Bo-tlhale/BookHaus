package com.bookhaus.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhaus.book.external.BookProvider;
import com.bookhaus.book.external.BookProviderException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock BookProvider provider;
    @Mock BookRepository repository;
    private BookService service;

    @BeforeEach
    void setUp() {
        service = new BookService(provider, repository);
    }

    @Test
    void rejectsBlankQueryWithoutCallingProvider() {
        assertThatThrownBy(() -> service.search("   "))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(provider, never()).search(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void trimsQueryAndDoesNotPersistSearchResults() {
        when(provider.search("Dune")).thenReturn(List.of());
        assertThat(service.search("  Dune  ")).isEmpty();
        verify(provider).search("Dune");
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void mapsProviderFailureToBadGateway() {
        when(provider.search("Dune")).thenThrow(new BookProviderException("Unavailable"));
        assertThatThrownBy(() -> service.search("Dune"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void returnsBookDetailsAndReportsMissingBook() {
        Book book = new Book();
        book.setId(9L);
        book.setTitle("Dune");
        when(repository.findById(9L)).thenReturn(Optional.of(book));
        when(repository.findById(10L)).thenReturn(Optional.empty());

        assertThat(service.getById(9L).title()).isEqualTo("Dune");
        assertThatThrownBy(() -> service.getById(10L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
