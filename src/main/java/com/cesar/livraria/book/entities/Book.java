package com.cesar.livraria.book.entities;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document("books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "genre_created_idx", def = "{'genre': 1, 'createdDate': -1}")
public class Book {

    @Id
    private String id;

    private String title;

    private String author;

    @Indexed(unique = true)
    private String isbn;

    private LocalDate publishDate;

    private Genre genre;

    private boolean available = true;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
}
