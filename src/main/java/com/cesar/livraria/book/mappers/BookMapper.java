package com.cesar.livraria.book.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.cesar.livraria.book.entities.Book;
import com.cesar.livraria.book.dto.request.BookRequest;
import com.cesar.livraria.book.dto.response.BookResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper extends EntityMapper<Book, BookRequest, BookResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    Book toEntity(BookRequest request);

    @Override
    BookResponse toResponse(Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    Book updateEntity(@MappingTarget Book entity, BookRequest request);

}
