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
import com.cesar.livraria.repository.BookRepository;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Page<Book> findAll(Genre genre, Pageable pageable) {
        if (genre == null) {
            return bookRepository.findAll(pageable);
        }
        return bookRepository.findByGenre(genre, pageable);
    }

    @Cacheable(value = "books", key = "#id")
    public Book findById(String id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    @CacheEvict(value = "books", key = "#id")
    public void deleteById(String id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }

    @CachePut(value = "books", key = "#book.id")
    public Book update(Book book) {
        if (book.getId() == null || !bookRepository.existsById(book.getId())) {
            throw new ResourceNotFoundException("Book not found with id: " + book.getId());
        }
        return bookRepository.save(book);
    }

    public Book save(Book book) {
        try {
            return bookRepository.save(book);
        } catch (DuplicateKeyException e) {
            throw new IsbnAlreadyExistsException("Book with ISBN " + book.getIsbn() + " already exists");
        }
    }
}
