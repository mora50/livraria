package com.cesar.livraria.controllers;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cesar.livraria.entities.Book;
import com.cesar.livraria.entities.Genre;
import com.cesar.livraria.mappers.BookMapper;
import com.cesar.livraria.request.BookRequest;
import com.cesar.livraria.response.BookResponse;
import com.cesar.livraria.response.PageResponse;
import com.cesar.livraria.services.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/book")
@Tag(name = "Livros", description = "Operações de gerenciamento do catálogo de livros")
@RequiredArgsConstructor
public class BookController implements IBookController{

    private final BookService bookService;
    private final BookMapper bookMapper;

    public ResponseEntity<BookResponse> createBook(BookRequest req) {
        Book saved = bookService.save(bookMapper.toEntity(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(bookMapper.toResponse(saved));
    }

    public ResponseEntity<PageResponse<BookResponse>> listBooks(
       Genre genre,
       Pageable pageable
    ) {
        Page<Book> page = bookService.findAll(genre, pageable);
        return ResponseEntity.ok(PageResponse.from(page, bookMapper::toResponse));
    }


    public ResponseEntity<BookResponse> getBookById(
       String id
    ) {
        Book book = bookService.findById(id);
        return ResponseEntity.ok(bookMapper.toResponse(book));
    }


    public ResponseEntity<Void> deleteBookById(
        String id
    ) {
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    public ResponseEntity<BookResponse> updateBookById(
       String id,
       BookRequest req
    ) {
        Book entity = bookMapper.toEntity(req);
        entity.setId(id);
        Book book = bookService.update(entity);
        return ResponseEntity.ok(bookMapper.toResponse(book));
    }
}
