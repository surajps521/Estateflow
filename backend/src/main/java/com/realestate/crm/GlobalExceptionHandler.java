package com.realestate.crm;
import java.time.Instant;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestControllerAdvice
class GlobalExceptionHandler {
 @ExceptionHandler(ResponseStatusException.class) ResponseEntity<ErrorResponse> status(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getReason(),Instant.now(),null));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e){Map<String,String> errors=new LinkedHashMap<>();e.getBindingResult().getFieldErrors().forEach(x->errors.put(x.getField(),x.getDefaultMessage()));return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed",Instant.now(),errors));}
 @ExceptionHandler(Exception.class) ResponseEntity<ErrorResponse> other(Exception e){return ResponseEntity.status(500).body(new ErrorResponse("Unexpected server error",Instant.now(),null));}
}
