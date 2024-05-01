package ru.polyakov.bookstore.service;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.polyakov.bookstore.model.Book;

@Mapper(componentModel = "spring")
public interface EntityUpdater {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Book book, Book request);
}
