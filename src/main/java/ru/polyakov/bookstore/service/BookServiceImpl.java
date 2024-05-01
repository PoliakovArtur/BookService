package ru.polyakov.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
    private final EntityUpdater updater;

    @Override
    public Book findById(Long id) {
        log.info("try to find book by id {}", id);
        return findByIdAndCheck(id);
    }

    @Override
    public Book findByAuthorAndName(String name, String author) {
        log.info("try to find book by name {} and author {}", name, author);
        return bookRepository.findByNameAndAuthor(name, author).orElseThrow(
                () -> new NotFoundException(format("Книга под названием %s автора %s не найдена", name, author)));
    }

    @Override
    public List<Book> findByCategory(String categoryName) {
        return bookRepository.findByCategoryName(categoryName);
    }

    @Override
    public Book save(Book request, String categoryName) {
        checkForUniqueBook(request.getName(), request.getAuthor());
        Category category = findOrCreateCategory(categoryName);
        request.setCategory(category);
        return bookRepository.save(request);
    }

    @Override
    public Book updateById(Long id, Book request, String categoryName) {
        checkForUniqueBook(request.getName(), request.getAuthor());
        Book fromDb = findByIdAndCheck(id);
        Category category = findOrCreateCategory(categoryName);
        fromDb.setCategory(category);
        updater.update(fromDb, request);
        return bookRepository.save(fromDb);
    }

    @Override
    public void deleteById(Long id) {
        Book book = findByIdAndCheck(id);
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
