package com.cesar.livraria.book.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(basePackages = "com.cesar.livraria.book.controllers")
public class BookExceptionHandler {

  @ExceptionHandler(IsbnAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handleIsbnAlreadyExistsException(IsbnAlreadyExistsException ex) {
    log.warn("ISBN conflito: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage()));
  }
}
