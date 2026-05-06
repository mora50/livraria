package com.cesar.livraria.book.mappers;

public interface EntityMapper<E, Q, S> {

    E toEntity(Q request);

    S toResponse(E entity);

    E updateEntity(E entity, Q request);
}
