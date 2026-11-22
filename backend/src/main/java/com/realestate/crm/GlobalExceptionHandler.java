package com.realestate.crm;
import java.time.Instant;
import java.util.*;
import org.springframework.http.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestControllerAdvice
class GlobalExceptionHandler {
 private static final Logger log=LoggerFactory.getLogger(GlobalExceptionHandler.class);
 @ExceptionHandler(ResponseStatusException.class) ResponseEntity<ErrorResponse> status(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason(),Instant.now(),null));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e){Map<String,String> errors=new LinkedHashMap<>();e.getBindingResult().getFieldErrors().forEach(x->errors.put(x.getField(),x.getDefaultMessage()));return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed",Instant.now(),errors));}
 @ExceptionHandler(HttpMessageNotReadableException.class) ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException e){return ResponseEntity.badRequest().body(new ErrorResponse("The request contains invalid JSON or field values.",Instant.now(),null));}
 @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<ErrorResponse> dataConflict(DataIntegrityViolationException e){log.warn("Database constraint violation",e);return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("This account could not be created because the details are already in use.",Instant.now(),null));}
 @ExceptionHandler(Exception.class) ResponseEntity<ErrorResponse> other(Exception e){log.error("Unhandled request error",e);return ResponseEntity.status(500).body(new ErrorResponse("Unexpected server error",Instant.now(),null));}
}
