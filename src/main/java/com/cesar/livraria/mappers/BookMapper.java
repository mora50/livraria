package com.cesar.livraria.mappers;

import com.cesar.livraria.entities.Book;
import com.cesar.livraria.request.BookRequest;
import com.cesar.livraria.response.BookResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper extends EntityMapper<Book, BookRequest, BookResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    Book toEntity(BookRequest request);

    @Override
    BookResponse toResponse(Book book);
}
