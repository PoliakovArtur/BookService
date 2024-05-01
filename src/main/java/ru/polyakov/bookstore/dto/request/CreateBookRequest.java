package ru.polyakov.bookstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateBookRequest {

    @NotBlank(message = "Название книги не может быть пустым")
    private String name;

    @NotBlank(message = "Имя автора не может быть пустым")
    private String author;

    @NotBlank(message = "Название категории не может быть пустым")
    private String categoryName;
}
