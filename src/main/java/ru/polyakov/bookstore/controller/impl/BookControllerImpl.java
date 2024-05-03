package ru.polyakov.bookstore.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.polyakov.bookstore.controller.BookController;
import ru.polyakov.bookstore.dto.request.CreateBookRequest;
import ru.polyakov.bookstore.dto.request.UpdateBookRequest;
import ru.polyakov.bookstore.dto.response.BookResponse;
import ru.polyakov.bookstore.dto.response.BooksResponse;
import ru.polyakov.bookstore.mapper.BookMapper;
import ru.polyakov.bookstore.service.BookService;

import java.util.Optional;

import static org.springframework.http.HttpStatus.CREATED;

@RequiredArgsConstructor
@RestController
public class BookControllerImpl implements BookController {

    private final BookService bookService;
    private final BookMapper mapper;

    @Override
    public ResponseEntity<BookResponse> findById(Long id) {
        return ResponseEntity.ok(
                mapper.toResponse(bookService.findById(id)));
    }

    @Override
    public ResponseEntity<BookResponse> findByAuthorAndName(String name, String author) {
        return ResponseEntity.ok(
                mapper.toResponse(bookService.findByAuthorAndName(name, author)));
    }

    @Override
    public ResponseEntity<BooksResponse> findByCategory(String category) {
        return ResponseEntity.ok(
                new BooksResponse(mapper.toBookResponseList(bookService.findByCategory(category))));
    }

    @Override
    public ResponseEntity<BookResponse> save(CreateBookRequest request) {
        return ResponseEntity.status(CREATED).body(
                mapper.toResponse(bookService.save(
                        mapper.fromCreateRequest(request), request.getCategoryName())));
    }

    @Override
    public ResponseEntity<BookResponse> updateById(Long id, UpdateBookRequest request) {
        return ResponseEntity.ok(
                mapper.toResponse(bookService.updateById(id,
                        mapper.fromUpdateRequest(request), request.getCategoryName())));
    }

    @Override
    public ResponseEntity<Void> deleteById(Long id) {
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
