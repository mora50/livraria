package com.cesar.livraria.shared.exception;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleNotFoundException(ResourceNotFoundException ex) {
    log.warn("Resource not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ProblemDetail> handleNotReadableException(HttpMessageNotReadableException ex) {
    String detail = "Corpo da requisição inválido.";

    if (ex.getCause() instanceof InvalidFormatException ife && !ife.getPath().isEmpty()) {
      String field = ife.getPath().get(ife.getPath().size() - 1).getFieldName();
      detail = "Campo " + field + " inválido.";

      Class<?> targetType = ife.getTargetType();
      if (targetType != null && targetType.isEnum()) {
        detail += " Valores aceitos: " + Arrays.toString(targetType.getEnumConstants());
      }
    }

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ProblemDetail> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
    String detail = "Parâmetro " + ex.getName() + " inválido.";

    Class<?> requiredType = ex.getRequiredType();
    if (requiredType != null && requiredType.isEnum()) {
      detail += " Valores aceitos: " + Arrays.toString(requiredType.getEnumConstants());
    }

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new LinkedHashMap<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      errors.putIfAbsent(
          fieldError.getField(),
          fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "campo inválido");
    }

    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST,
        "Um ou mais campos obrigatórios estão ausentes ou inválidos.");
    problem.setProperty("invalid_fields", errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }
}
