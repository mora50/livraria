package com.cesar.livraria.book.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.cesar.livraria.book.entities.Book;
import com.cesar.livraria.book.entities.Genre;

public interface BookRepository extends MongoRepository<Book, String> {

    Page<Book> findByGenre(Genre genre, Pageable pageable);
}
