package ru.polyakov.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.polyakov.bookstore.model.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
}
