package ru.polyakov.bookstore.service;

import ru.polyakov.bookstore.model.Book;

import java.util.List;

public interface BookService {

    Book findById(Long id);

    Book findByAuthorAndName(String name, String author);

    List<Book> findByCategory(String name);

    Book save(Book request, String categoryName);

    Book updateById(Long id, Book request, String categoryName);

    void deleteById(Long id);
}
