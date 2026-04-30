package com.cesar.livraria.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.cesar.livraria.entities.Book;
import com.cesar.livraria.entities.Genre;
import com.cesar.livraria.exception.IsbnAlreadyExistsException;
import com.cesar.livraria.exception.ResourceNotFoundException;
import com.cesar.livraria.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService - Unit Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book sampleBook;

    @BeforeEach
    void setUp() {
        sampleBook = new Book();
        sampleBook.setId("64fa1b2c3e4a5f6b7c8d9e0f");
        sampleBook.setTitle("O Senhor dos Anéis");
        sampleBook.setAuthor("J.R.R. Tolkien");
        sampleBook.setIsbn("9788533613379");
        sampleBook.setPublishDate(LocalDate.of(1954, 7, 29));
        sampleBook.setGenre(Genre.FANTASIA);
        sampleBook.setAvailable(true);
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("deve salvar e retornar o livro quando dados são válidos")
        void shouldSaveBookSuccessfully() {
            when(bookRepository.save(sampleBook)).thenReturn(sampleBook);

            Book result = bookService.save(sampleBook);

            assertThat(result).isSameAs(sampleBook);
            verify(bookRepository, times(1)).save(sampleBook);
        }

        @Test
        @DisplayName("deve lançar IsbnAlreadyExistsException quando ISBN duplicado")
        void shouldThrowIsbnAlreadyExistsWhenDuplicateKey() {
            when(bookRepository.save(sampleBook))
                    .thenThrow(new DuplicateKeyException("dup key"));

            assertThatThrownBy(() -> bookService.save(sampleBook))
                    .isInstanceOf(IsbnAlreadyExistsException.class)
                    .hasMessageContaining(sampleBook.getIsbn());

            verify(bookRepository).save(sampleBook);
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("deve retornar livro quando encontrado")
        void shouldReturnBookWhenFound() {
            when(bookRepository.findById(sampleBook.getId())).thenReturn(Optional.of(sampleBook));

            Book result = bookService.findById(sampleBook.getId());

            assertThat(result).isEqualTo(sampleBook);
            verify(bookRepository).findById(sampleBook.getId());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando livro não existe")
        void shouldThrowResourceNotFoundWhenAbsent() {
            when(bookRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.findById("missing"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("missing");
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("deve usar findAll quando gênero é nulo")
        void shouldCallFindAllWhenGenreIsNull() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Book> page = new PageImpl<>(List.of(sampleBook));
            when(bookRepository.findAll(pageable)).thenReturn(page);

            Page<Book> result = bookService.findAll(null, pageable);

            assertThat(result.getContent()).containsExactly(sampleBook);
            verify(bookRepository).findAll(pageable);
            verify(bookRepository, never()).findByGenre(any(), any());
        }

        @Test
        @DisplayName("deve usar findByGenre quando gênero é informado")
        void shouldCallFindByGenreWhenGenreProvided() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Book> page = new PageImpl<>(List.of(sampleBook));
            when(bookRepository.findByGenre(Genre.FANTASIA, pageable)).thenReturn(page);

            Page<Book> result = bookService.findAll(Genre.FANTASIA, pageable);

            assertThat(result.getContent()).containsExactly(sampleBook);
            verify(bookRepository).findByGenre(Genre.FANTASIA, pageable);
            verify(bookRepository, never()).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("deve atualizar livro existente")
        void shouldUpdateExistingBook() {
            when(bookRepository.existsById(sampleBook.getId())).thenReturn(true);
            when(bookRepository.save(sampleBook)).thenReturn(sampleBook);

            Book result = bookService.update(sampleBook);

            assertThat(result).isEqualTo(sampleBook);
            verify(bookRepository).existsById(sampleBook.getId());
            verify(bookRepository).save(sampleBook);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando id é nulo")
        void shouldThrowWhenIdIsNull() {
            sampleBook.setId(null);

            assertThatThrownBy(() -> bookService.update(sampleBook))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(bookRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando livro não existe")
        void shouldThrowWhenBookDoesNotExist() {
            when(bookRepository.existsById(sampleBook.getId())).thenReturn(false);

            assertThatThrownBy(() -> bookService.update(sampleBook))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(sampleBook.getId());

            verify(bookRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteById()")
    class DeleteById {

        @Test
        @DisplayName("deve remover livro existente")
        void shouldDeleteExistingBook() {
            when(bookRepository.existsById(sampleBook.getId())).thenReturn(true);

            bookService.deleteById(sampleBook.getId());

            verify(bookRepository).existsById(sampleBook.getId());
            verify(bookRepository).deleteById(sampleBook.getId());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando livro não existe")
        void shouldThrowWhenBookToDeleteIsAbsent() {
            when(bookRepository.existsById("missing")).thenReturn(false);

            assertThatThrownBy(() -> bookService.deleteById("missing"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("missing");

            verify(bookRepository, never()).deleteById("missing");
        }
    }
}
