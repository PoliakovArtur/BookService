package ru.polyakov.bookstore.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;
import ru.polyakov.bookstore.model.Book;

import java.util.List;

public interface BookService {

    @Cacheable(cacheNames = "book", key = "#id")
    @Transactional(readOnly = true)
    Book findById(Long id);

    @Cacheable(cacheNames = "bookByNameAndAuthor", key = "#name + #author")
    @Transactional(readOnly = true)
    Book findByAuthorAndName(String name, String author);

    @Cacheable(cacheNames = "books", key = "#categoryName")
    @Transactional(readOnly = true)
    List<Book> findByCategory(String name);

    @CacheEvict(cacheNames = "books", key = "#categoryName")
    @Transactional
    Book save(Book request, String categoryName);

    @Caching(evict = {
            @CacheEvict(cacheNames = "book", key = "#id"),
            @CacheEvict(cacheNames = "bookByNameAndAuthor"),
            @CacheEvict(cacheNames = "books", key = "#categoryName")
    })
    @Transactional
    Book updateById(Long id, Book request, String categoryName);

    @Caching(evict = {
            @CacheEvict(cacheNames = "book", key = "#id"),
            @CacheEvict(cacheNames = "bookByNameAndAuthor"),
            @CacheEvict(cacheNames = "books")
    })
    @Transactional
    void deleteById(Long id);
}
