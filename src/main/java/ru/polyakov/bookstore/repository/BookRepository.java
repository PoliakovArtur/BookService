package ru.polyakov.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.polyakov.bookstore.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByCategoryName(String name);

    Optional<Book> findByNameAndAuthor(String name, String author);
}
