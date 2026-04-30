package com.cesar.livraria.response;

import java.time.LocalDate;

import com.cesar.livraria.entities.Genre;

public record BookResponse(
        String id,
        String title,
        String author,
        String isbn,
        LocalDate publishDate,
        Genre genre,
        boolean available) {
}
