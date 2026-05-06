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
import org.mapstruct.factory.Mappers;
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
import com.cesar.livraria.mappers.BookMapper;
import com.cesar.livraria.repository.BookRepository;
import com.cesar.livraria.request.BookRequest;
import com.cesar.livraria.response.BookResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService - Unit Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    private BookService bookService;

    private static final String SAMPLE_ID = "64fa1b2c3e4a5f6b7c8d9e0f";

    private Book sampleBook;
    private BookRequest sampleRequest;

    @BeforeEach
    void setUp() {
        BookMapper bookMapper = Mappers.getMapper(BookMapper.class);
        bookService = new BookService(bookRepository, bookMapper);

        sampleBook = new Book();
        sampleBook.setId(SAMPLE_ID);
        sampleBook.setTitle("O Senhor dos Anéis");
        sampleBook.setAuthor("J.R.R. Tolkien");
        sampleBook.setIsbn("9788533613379");
        sampleBook.setPublishDate(LocalDate.of(1954, 7, 29));
        sampleBook.setGenre(Genre.FANTASIA);
        sampleBook.setAvailable(true);

        sampleRequest = new BookRequest(
                sampleBook.getTitle(),
                sampleBook.getAuthor(),
                sampleBook.getIsbn(),
                sampleBook.getPublishDate(),
                sampleBook.getGenre(),
                sampleBook.isAvailable());
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("deve salvar e retornar o livro quando dados são válidos")
        void shouldSaveBookSuccessfully() {
            when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

            BookResponse result = bookService.save(sampleRequest);

            assertThat(result.id()).isEqualTo(SAMPLE_ID);
            assertThat(result.isbn()).isEqualTo(sampleRequest.isbn());
            assertThat(result.title()).isEqualTo(sampleRequest.title());
            verify(bookRepository, times(1)).save(any(Book.class));
        }

        @Test
        @DisplayName("deve lançar IsbnAlreadyExistsException quando ISBN duplicado")
        void shouldThrowIsbnAlreadyExistsWhenDuplicateKey() {
            when(bookRepository.save(any(Book.class)))
                    .thenThrow(new DuplicateKeyException("dup key"));

            assertThatThrownBy(() -> bookService.save(sampleRequest))
                    .isInstanceOf(IsbnAlreadyExistsException.class)
                    .hasMessageContaining(sampleRequest.isbn());

            verify(bookRepository).save(any(Book.class));
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("deve retornar livro quando encontrado")
        void shouldReturnBookWhenFound() {
            when(bookRepository.findById(SAMPLE_ID)).thenReturn(Optional.of(sampleBook));

            BookResponse result = bookService.findById(SAMPLE_ID);

            assertThat(result.id()).isEqualTo(SAMPLE_ID);
            assertThat(result.isbn()).isEqualTo(sampleBook.getIsbn());
            verify(bookRepository).findById(SAMPLE_ID);
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

            Page<BookResponse> result = bookService.findAll(null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).id()).isEqualTo(SAMPLE_ID);
            verify(bookRepository).findAll(pageable);
            verify(bookRepository, never()).findByGenre(any(), any());
        }

        @Test
        @DisplayName("deve usar findByGenre quando gênero é informado")
        void shouldCallFindByGenreWhenGenreProvided() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Book> page = new PageImpl<>(List.of(sampleBook));
            when(bookRepository.findByGenre(Genre.FANTASIA, pageable)).thenReturn(page);

            Page<BookResponse> result = bookService.findAll(Genre.FANTASIA, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).genre()).isEqualTo(Genre.FANTASIA);
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
            when(bookRepository.findById(SAMPLE_ID)).thenReturn(Optional.of(sampleBook));
            when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

            BookResponse result = bookService.update(SAMPLE_ID, sampleRequest);

            assertThat(result.id()).isEqualTo(SAMPLE_ID);
            verify(bookRepository).findById(SAMPLE_ID);
            verify(bookRepository).save(any(Book.class));
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando id é nulo")
        void shouldThrowWhenIdIsNull() {
            assertThatThrownBy(() -> bookService.update(null, sampleRequest))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(bookRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando livro não existe")
        void shouldThrowWhenBookDoesNotExist() {
            when(bookRepository.findById(SAMPLE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.update(SAMPLE_ID, sampleRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(SAMPLE_ID);

            verify(bookRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar IsbnAlreadyExistsException quando update colide com ISBN existente")
        void shouldThrowIsbnAlreadyExistsOnUpdateDuplicateKey() {
            when(bookRepository.findById(SAMPLE_ID)).thenReturn(Optional.of(sampleBook));
            when(bookRepository.save(any(Book.class)))
                    .thenThrow(new DuplicateKeyException("dup key"));

            assertThatThrownBy(() -> bookService.update(SAMPLE_ID, sampleRequest))
                    .isInstanceOf(IsbnAlreadyExistsException.class)
                    .hasMessageContaining(sampleRequest.isbn());

            verify(bookRepository).save(any(Book.class));
        }
    }

    @Nested
    @DisplayName("deleteById()")
    class DeleteById {

        @Test
        @DisplayName("deve remover livro existente")
        void shouldDeleteExistingBook() {
            when(bookRepository.existsById(SAMPLE_ID)).thenReturn(true);

            bookService.deleteById(SAMPLE_ID);

            verify(bookRepository).existsById(SAMPLE_ID);
            verify(bookRepository).deleteById(SAMPLE_ID);
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
