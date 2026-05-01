package com.cesar.livraria.controllers;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cesar.livraria.entities.Genre;
import com.cesar.livraria.request.BookRequest;
import com.cesar.livraria.response.BookResponse;
import com.cesar.livraria.response.PageResponse;
import com.cesar.livraria.services.BookService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/book")
@Tag(name = "Livros", description = "Operações de gerenciamento do catálogo de livros")
@RequiredArgsConstructor
public class BookController implements IBookController {

    private final BookService bookService;

    @Override
    public ResponseEntity<BookResponse> createBook(BookRequest req) {
        BookResponse created = bookService.save(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    public ResponseEntity<PageResponse<BookResponse>> listBooks(Genre genre, Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(bookService.findAll(genre, pageable)));
    }

    @Override
    public ResponseEntity<BookResponse> getBookById(String id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    @Override
    public ResponseEntity<Void> deleteBookById(String id) {
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<BookResponse> updateBookById(String id, BookRequest req) {
        return ResponseEntity.ok(bookService.update(id, req));
    }
}
