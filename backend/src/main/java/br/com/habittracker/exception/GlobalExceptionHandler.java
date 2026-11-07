package br.com.habittracker.exception;

import br.com.habittracker.dto.ApiDtos.ApiError;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  ResponseEntity<ApiError> api(ApiException e) {
    return ResponseEntity.status(e.getStatus()).body(
      new ApiError(e.getMessage())
    );
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e) {
    return ResponseEntity.badRequest().body(
      new ApiError("Dados inválidos. Verifique os campos informados.")
    );
  }

  @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
    org.springframework.http.converter.HttpMessageNotReadableException.class,
  })
  ResponseEntity<ApiError> malformed(Exception e) {
    return ResponseEntity.badRequest().body(
      new ApiError("Dados ou data em formato inválido.")
    );
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ApiError> conflict(DataIntegrityViolationException e) {
    return ResponseEntity.status(409).body(
      new ApiError("Registro duplicado ou conflito de dados.")
    );
  }
}
