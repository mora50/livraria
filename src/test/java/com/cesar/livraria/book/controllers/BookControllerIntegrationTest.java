package com.cesar.livraria.book.controllers;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.cesar.livraria.TestcontainersConfiguration;
import com.cesar.livraria.book.entities.Book;
import com.cesar.livraria.book.entities.Genre;
import com.cesar.livraria.book.repository.BookRepository;
import com.cesar.livraria.book.dto.request.BookRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@DisplayName("BookController - Integration Tests")
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void cleanState() {
        bookRepository.deleteAll();
        if (cacheManager.getCache("books") != null) {
            cacheManager.getCache("books").clear();
        }
    }

    private BookRequest validRequest(String isbn) {
        return new BookRequest(
                "O Senhor dos Anéis",
                "J.R.R. Tolkien",
                isbn,
                LocalDate.of(1954, 7, 29),
                Genre.FANTASIA,
                true);
    }

    private Book persistBook(String isbn, Genre genre) {
        Book book = new Book();
        book.setTitle("O Senhor dos Anéis");
        book.setAuthor("J.R.R. Tolkien");
        book.setIsbn(isbn);
        book.setPublishDate(LocalDate.of(1954, 7, 29));
        book.setGenre(genre);
        book.setAvailable(true);
        return bookRepository.save(book);
    }

    private String uniqueIsbn() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 13);
    }

    @Test
    @DisplayName("POST /book - 201: deve criar um livro válido")
    void shouldCreateBook() throws Exception {
        BookRequest body = validRequest(uniqueIsbn());

        mockMvc.perform(post("/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title").value(body.title()))
                .andExpect(jsonPath("$.isbn").value(body.isbn()))
                .andExpect(jsonPath("$.genre").value(body.genre().name()));
    }

    @Test
    @DisplayName("POST /book - 400: deve falhar quando body é inválido")
    void shouldReturnBadRequestForInvalidBody() throws Exception {
        BookRequest invalid = new BookRequest(
                "no",
                "",
                null,
                LocalDate.now().plusDays(1),
                null,
                true);

        mockMvc.perform(post("/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_fields").exists());
    }

    @Test
    @DisplayName("POST /book - 409: deve falhar quando ISBN duplicado")
    void shouldReturnBadRequestWhenIsbnDuplicated() throws Exception {
        String isbn = uniqueIsbn();
        persistBook(isbn, Genre.FANTASIA);

        BookRequest body = validRequest(isbn);

        mockMvc.perform(post("/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString(isbn)));
    }

    @Test
    @DisplayName("GET /book - 200: deve listar livros paginados")
    void shouldListBooksPaginated() throws Exception {
        persistBook(uniqueIsbn(), Genre.FANTASIA);
        persistBook(uniqueIsbn(), Genre.ROMANCE);
        persistBook(uniqueIsbn(), Genre.TERROR);

        mockMvc.perform(get("/book")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @DisplayName("GET /book?genre=X - 200: deve filtrar por gênero")
    void shouldListBooksFilteredByGenre() throws Exception {
        persistBook(uniqueIsbn(), Genre.FANTASIA);
        persistBook(uniqueIsbn(), Genre.ROMANCE);

        mockMvc.perform(get("/book").param("genre", Genre.FANTASIA.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].genre").value(Genre.FANTASIA.name()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /book?genre=invalido - 400: deve retornar erro para gênero inválido")
    void shouldReturnBadRequestForInvalidGenre() throws Exception {
        mockMvc.perform(get("/book").param("genre", "INVALIDO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    @DisplayName("GET /book/{id} - 200: deve retornar livro existente")
    void shouldGetBookById() throws Exception {
        Book saved = persistBook(uniqueIsbn(), Genre.FANTASIA);

        mockMvc.perform(get("/book/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.isbn").value(saved.getIsbn()));
    }

    @Test
    @DisplayName("GET /book/{id} - 404: deve retornar 404 quando id inexistente")
    void shouldReturnNotFoundOnGetById() throws Exception {
        mockMvc.perform(get("/book/{id}", "id-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("id-inexistente")));
    }

    @Test
    @DisplayName("PUT /book/{id} - 200: deve atualizar livro existente")
    void shouldUpdateBook() throws Exception {
        Book saved = persistBook(uniqueIsbn(), Genre.FANTASIA);

        BookRequest update = new BookRequest(
                "O Hobbit",
                "J.R.R. Tolkien",
                saved.getIsbn(),
                LocalDate.of(1937, 9, 21),
                Genre.FANTASIA,
                false);

        mockMvc.perform(put("/book/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("O Hobbit"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    @DisplayName("PUT /book/{id} - 404: deve retornar 404 quando livro não existe")
    void shouldReturnNotFoundOnUpdate() throws Exception {
        BookRequest update = validRequest(uniqueIsbn());

        mockMvc.perform(put("/book/{id}", "id-inexistente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    @DisplayName("PUT /book/{id} - 400: deve retornar 400 com body inválido")
    void shouldReturnBadRequestOnUpdateInvalidBody() throws Exception {
        Book saved = persistBook(uniqueIsbn(), Genre.FANTASIA);

        BookRequest invalid = new BookRequest(
                null,
                null,
                null,
                null,
                null,
                true);

        mockMvc.perform(put("/book/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_fields").exists());
    }

    @Test
    @DisplayName("DELETE /book/{id} - 204: deve remover livro existente")
    void shouldDeleteBook() throws Exception {
        Book saved = persistBook(uniqueIsbn(), Genre.FANTASIA);

        mockMvc.perform(delete("/book/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/book/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /book/{id} - 404: deve retornar 404 quando id inexistente")
    void shouldReturnNotFoundOnDelete() throws Exception {
        mockMvc.perform(delete("/book/{id}", "id-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    @DisplayName("Cache: GET /book/{id} sucessivos devem usar Redis")
    void shouldCacheGetByIdInRedis() throws Exception {
        Book saved = persistBook(uniqueIsbn(), Genre.FANTASIA);

        MvcResult first = mockMvc.perform(get("/book/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andReturn();

        bookRepository.deleteAll();

        MvcResult second = mockMvc.perform(get("/book/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode firstJson = objectMapper.readTree(first.getResponse().getContentAsString());
        JsonNode secondJson = objectMapper.readTree(second.getResponse().getContentAsString());

        org.assertj.core.api.Assertions.assertThat(secondJson.get("id").asText())
                .isEqualTo(firstJson.get("id").asText());
    }
}
