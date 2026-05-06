package com.cesar.livraria.book.dto.request;

import java.time.LocalDate;

import com.cesar.livraria.book.entities.Genre;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record BookRequest(
        @NotBlank @Size(min = 3, max = 100) String title,
        @NotBlank @Size(min = 3, max = 100) String author,
        @NotBlank @Size(max = 15) String isbn,
        @NotNull @PastOrPresent LocalDate publishDate,
        @NotNull Genre genre,
        boolean available) {
}
