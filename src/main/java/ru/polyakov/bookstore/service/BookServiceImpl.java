package ru.polyakov.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.polyakov.bookstore.exception.BadRequestException;
import ru.polyakov.bookstore.exception.NotFoundException;
import ru.polyakov.bookstore.model.Book;
import ru.polyakov.bookstore.model.Category;
import ru.polyakov.bookstore.repository.BookRepository;
import ru.polyakov.bookstore.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService{

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EntityUpdater updater;

    @Cacheable(cacheNames = "book", key = "#id")
    @Transactional(readOnly = true)
    @Override
    public Book findById(Long id) {
        return findByIdAndCheck(id);
    }

    @Cacheable(cacheNames = "bookByNameAndAuthor", key = "#name + #author")
    @Transactional(readOnly = true)
    @Override
    public Book findByAuthorAndName(String name, String author) {
        return bookRepository.findByNameAndAuthor(name, author).orElseThrow(
                () -> new NotFoundException(format("Книга под названием %s автора %s не найдена", name, author)));
    }

    @Cacheable(cacheNames = "books", key = "#categoryName")
    @Transactional(readOnly = true)
    @Override
    public List<Book> findByCategory(String categoryName) {
        return bookRepository.findByCategoryName(categoryName);
    }

    @CacheEvict(cacheNames = "books", key = "#categoryName")
    @Transactional
    @Override
    public Book save(Book request, String categoryName) {
        checkForUniqueBook(request.getName(), request.getAuthor());
        Category category = findOrCreateCategory(categoryName);
        request.setCategory(category);
        return bookRepository.save(request);
    }

    @CacheEvict(cacheNames = "book", key = "#id")
    @Transactional
    @Override
    public Book updateById(Long id, Book request, String categoryName) {
        checkForUniqueBook(request.getName(), request.getAuthor());
        Book fromDb = findByIdAndCheck(id);
        if(categoryName != null) {
            Category category = findOrCreateCategory(categoryName);
            fromDb.setCategory(category);
        } else {
            redisTemplate.delete(format("books::%s", fromDb.getCategory().getName()));
        }
        String key = format("bookByNameAndAuthor::%s%s", fromDb.getName(), fromDb.getAuthor());
        redisTemplate.delete(key);
        updater.update(fromDb, request);
        return bookRepository.save(fromDb);
    }

    @CacheEvict(cacheNames = "book", key = "#id")
    @Transactional
    @Override
    public void deleteById(Long id) {
        Book book = findByIdAndCheck(id);
        redisTemplate.delete(format("bookByNameAndAuthor::%s%s", book.getName(), book.getAuthor()));
        redisTemplate.delete(format("books::%s", book.getCategory().getName()));
        bookRepository.delete(book);
    }

    private Category findOrCreateCategory(String categoryName) {
        return categoryRepository.findByName(categoryName)
                .orElseGet(() -> categoryRepository.save(Category.builder().name(categoryName).build()));
    }

    private Book findByIdAndCheck(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(format("Книга с id %s не найдена", id)));
    }

    private void checkForUniqueBook(String name, String author) {
        Optional<Book> book = bookRepository.findByNameAndAuthor(name, author);
        if(book.isPresent()) {
            throw new BadRequestException(format("Книга под названием %s автора %s уже есть", name, author));
        }
    }
}
