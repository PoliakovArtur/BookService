package ru.polyakov.bookstore.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookRequest {

    private String name;

    private String author;

    private String categoryName;
}
