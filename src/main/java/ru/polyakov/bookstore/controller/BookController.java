package ru.polyakov.bookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.polyakov.bookstore.dto.request.CreateBookRequest;
import ru.polyakov.bookstore.dto.request.UpdateBookRequest;
import ru.polyakov.bookstore.dto.response.BookResponse;
import ru.polyakov.bookstore.dto.response.BooksResponse;

@RequestMapping("/api/v1")
public interface BookController {

    @Operation(
            summary = "Нахождение книги по id",
            description = "Нахождение книги по id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Некорректный id"),
                    @ApiResponse(responseCode = "404", description = "Книга не найдена")
            }
    )
    @GetMapping("/book/{id}")
    ResponseEntity<BookResponse> findById(@PathVariable Long id);

    @Operation(
            summary = "Нахождение книги по автору и названию",
            description = "Нахождение книги по автору и названию",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "404", description = "Книга не найдена")
            }
    )
    @GetMapping("/book")
    ResponseEntity<BookResponse> findByAuthorAndName(@RequestParam String name, @RequestParam String author);

    @Operation(
            summary = "Нахождение книг по категории",
            description = "Нахождение книг по категории",
            responses = @ApiResponse(responseCode = "200", description = "OK")
    )
    @GetMapping("{category}/book")
    ResponseEntity<BooksResponse> findByCategory(@PathVariable String category);

    @Operation(
            summary = "Создание книги",
            description = "Создание книги",
            responses = @ApiResponse(responseCode = "201", description = "Книга создана")
    )
    @PostMapping("/book")
    ResponseEntity<BookResponse> save(@Valid @RequestBody CreateBookRequest request);

    @Operation(
            summary = "Редактирование книги по id",
            description = "Редактирование книги по id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Некорректный id"),
                    @ApiResponse(responseCode = "404", description = "Книга не найдена"),
            }
    )
    @PutMapping("/book/{id}")
    ResponseEntity<BookResponse> updateById(@PathVariable Long id, @Valid @RequestBody UpdateBookRequest request);

    @Operation(
            summary = "Удаление книги по id",
            description = "Удаление книги по id",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Книга удалена"),
                    @ApiResponse(responseCode = "400", description = "Некорректный id"),
                    @ApiResponse(responseCode = "404", description = "Книга не найдена"),
            }
    )
    @DeleteMapping("/book/{id}")
    ResponseEntity<Void> deleteById(@PathVariable Long id);


}
