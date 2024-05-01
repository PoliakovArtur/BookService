package ru.polyakov.bookstore.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.polyakov.bookstore.dto.request.CreateBookRequest;
import ru.polyakov.bookstore.dto.request.UpdateBookRequest;
import ru.polyakov.bookstore.dto.response.BookResponse;
import ru.polyakov.bookstore.dto.response.BookShortResponse;
import ru.polyakov.bookstore.model.Book;

import java.util.List;

@MapperConfig(unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Mapper(componentModel = "spring")
public interface BookMapper {

    List<BookShortResponse> toBookResponseList(List<Book> books);

    Book fromCreateRequest(CreateBookRequest request);

    Book fromUpdateRequest(UpdateBookRequest request);

    @Mapping(source = "category.name", target = "categoryName")
    BookResponse toResponse(Book book);
}
