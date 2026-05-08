package com.cesar.livraria.book.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.cesar.livraria.book.entities.Genre;

public record BookResponse(
        String id,
        String title,
        String author,
        String isbn,
        LocalDate publishDate,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate,
        Genre genre,
        Boolean available) {
}
