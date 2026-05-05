package com.cesar.livraria.services;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cesar.livraria.entities.Book;
import com.cesar.livraria.entities.Genre;
import com.cesar.livraria.exception.IsbnAlreadyExistsException;
import com.cesar.livraria.exception.ResourceNotFoundException;
import com.cesar.livraria.mappers.BookMapper;
import com.cesar.livraria.repository.BookRepository;
import com.cesar.livraria.request.BookRequest;
import com.cesar.livraria.response.BookResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public Page<BookResponse> findAll(Genre genre, Pageable pageable) {
        Page<Book> page = (genre == null)
                ? bookRepository.findAll(pageable)
                : bookRepository.findByGenre(genre, pageable);
        return page.map(bookMapper::toResponse);
    }

    @Cacheable(value = "books", key = "#id")
    public BookResponse findById(String id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return bookMapper.toResponse(book);
    }

    @CacheEvict(value = "books", key = "#id")
    public void deleteById(String id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
        log.info("Book deleted: id={}", id);
    }

    @CachePut(value = "books", key = "#id")
    public BookResponse update(String id, BookRequest request) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        Book entity = bookMapper.updateEntity(book, request);

        try {
            Book saved = bookRepository.save(entity);
            log.info("Book updated: id={}, isbn={}", saved.getId(), saved.getIsbn());
            return bookMapper.toResponse(saved);
        } catch (DuplicateKeyException e) {
            throw new IsbnAlreadyExistsException("Book with ISBN " + request.isbn() + " already exists");
        }
    }

    public BookResponse save(BookRequest request) {
        Book entity = bookMapper.toEntity(request);
        try {
            Book saved = bookRepository.save(entity);
            log.info("Book created: id={}, isbn={}", saved.getId(), saved.getIsbn());
            return bookMapper.toResponse(saved);
        } catch (DuplicateKeyException e) {
            throw new IsbnAlreadyExistsException("Book with ISBN " + request.isbn() + " already exists");
        }
    }
}
