package com.cesar.livraria.book.exception;

public class IsbnAlreadyExistsException extends RuntimeException {
    public IsbnAlreadyExistsException(String message) {
        super(message);
    }
}
