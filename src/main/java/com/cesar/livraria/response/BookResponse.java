package com.cesar.livraria.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.cesar.livraria.entities.Genre;

public record BookResponse(
        String id,
        String title,
        String author,
        String isbn,
        LocalDate publishDate,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate,
        Genre genre,
        boolean available) {
}
