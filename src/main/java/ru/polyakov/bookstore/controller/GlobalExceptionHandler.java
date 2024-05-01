package ru.polyakov.bookstore.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.polyakov.bookstore.dto.response.ResponseMessageDto;
import ru.polyakov.bookstore.exception.BadRequestException;
import ru.polyakov.bookstore.exception.NotFoundException;

import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseMessageDto> handleNotFoundException(NotFoundException ex) {
        log.info("Not found exception handled with message: {}", ex.getMessage());
        return ResponseEntity.status(NOT_FOUND)
                .body(new ResponseMessageDto(ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseMessageDto> handleBadRequestException(BadRequestException ex) {
        log.info("Not found exception handled with message: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new ResponseMessageDto(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseMessageDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.info(message);
        return ResponseEntity.badRequest().body(new ResponseMessageDto(message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseMessageDto> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String message = "Некорректное значение параметра";
        log.info(message);
        return ResponseEntity.badRequest().body(new ResponseMessageDto(message));
    }
}
