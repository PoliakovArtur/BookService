package ru.polyakov.bookstore.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.polyakov.bookstore.AbstractIntegrationTest;
import ru.polyakov.bookstore.model.Book;
import ru.polyakov.bookstore.repository.BookRepository;
import ru.polyakov.bookstore.service.BookService;

import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static ru.polyakov.bookstore.utils.TestUtils.readStringFromResource;

@Sql("classpath:sql/init.sql")
class BookIntegrationTest extends AbstractIntegrationTest {

    @SpyBean
    BookService bookService;

    @Autowired
    BookRepository bookRepository;

    private final String BOOK_ENDPOINT = "/api/v1/book";
    private final String BOOKS_BY_CATEGORY_ENDPOINT = "/api/v1/{categoryName}/book";
    private final String BOOK_BY_ID_ENDPOINT = "/api/v1/book/{id}";

    @BeforeEach
    void cleanCache() {
        redisTemplate.delete(redisTemplate.keys("*"));
    }

    @Test
    void findById_shouldReturn200() throws Exception {
        assertJsonEquals(
                expectedFrom(get(BOOK_BY_ID_ENDPOINT, "1"), OK),
                readStringFromResource("/json/response/some_book_about_music_1.json")
        );
    }

    @Test
    void findById_shouldCacheValue() throws Exception {
        expectedFrom(get(BOOK_BY_ID_ENDPOINT, "2"), OK);
        verify(bookService, times(1)).findById(2L);
        assertTrue(redisTemplate.hasKey("book::2"));

        mockMvc.perform(get(BOOK_BY_ID_ENDPOINT, "2"));
        verify(bookService, times(1)).findById(2L);
    }

    @Test
    void findById_shouldReturn404() throws Exception {
        expectedMessageAndStatusFrom(
                get(BOOK_BY_ID_ENDPOINT, "100"),
                NOT_FOUND, "Книга с id 100 не найдена"
        );
    }

    @Test
    void findByAuthorAndName_shouldReturn200() throws Exception {
        String author = "some musician 1", name = "some book about music 1";
        assertJsonEquals(
                readStringFromResource("/json/response/some_book_about_music_1.json"),
                expectedFrom(get(BOOK_ENDPOINT).params(createParams("name", name, "author", author)), OK)
        );
    }

    @Test
    void findByAuthorAndName_shouldCacheValue() throws Exception {
        String author = "some musician 2", name = "some book about music 2";

        expectedFrom(get(BOOK_ENDPOINT).params(createParams("name", name, "author", author)), OK);
        verify(bookService, times(1)).findByAuthorAndName(name, author);
        assertTrue(redisTemplate.hasKey(format("bookByNameAndAuthor::%s%s", name, author)));

        mockMvc.perform(get(BOOK_ENDPOINT).params(createParams("name", name, "author", author)));
        verify(bookService, times(1)).findByAuthorAndName(name, author);
    }

    @Test
    void findByAuthorAndName_shouldReturn404() throws Exception {
        expectedMessageAndStatusFrom(
                get(BOOK_ENDPOINT).params(createParams("name", "name", "author", "author")),
                NOT_FOUND, "Книга под названием name автора author не найдена"
        );
    }

    @Test
    void findByCategory_shouldReturn200() throws Exception {
        assertJsonEquals(
                readStringFromResource("/json/response/music_books.json"),
                expectedFrom(get(BOOKS_BY_CATEGORY_ENDPOINT, "music"), OK)
        );
    }

    @Test
    void findByCategory_shouldCacheValues() throws Exception {
        expectedFrom(get(BOOKS_BY_CATEGORY_ENDPOINT, "sport"), OK);
        verify(bookService, times(1)).findByCategory("sport");
        assertTrue(redisTemplate.hasKey("books::sport"));

        mockMvc.perform(get(BOOKS_BY_CATEGORY_ENDPOINT, "sport"));
        verify(bookService, times(1)).findByCategory("sport");
    }

    @Test
    void save_shouldReturn201() throws Exception {
        expectedFieldsAndStatusFrom(post(BOOK_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(readStringFromResource("/json/request/new_computer_book.json")),
                CREATED,
                "name", "some book about computer",
                "author", "programmer",
                "categoryName", "computer science");
    }

    @Test
    void save_shouldCleanBooksCache() throws Exception {
        expectedFrom(get(BOOKS_BY_CATEGORY_ENDPOINT, "music"), OK);
        verify(bookService, times(1)).findByCategory("music");
        assertTrue(redisTemplate.hasKey("books::music"));

        expectedFrom(post(BOOK_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(readStringFromResource("/json/request/new_music_book.json")), CREATED);

        assertFalse(redisTemplate.hasKey("books::music"));
        mockMvc.perform(get(BOOKS_BY_CATEGORY_ENDPOINT, "music"));
        verify(bookService, times(2)).findByCategory("music");
    }

    @Test
    void saveWithEmptyCategory_shouldReturn400() throws Exception {
        expectedMessageAndStatusFrom(post(BOOK_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(readStringFromResource("/json/request/invalid/new_music_book_without_category.json")),
                        BAD_REQUEST, "Название категории не может быть пустым");
        verify(bookService, times(0)).save(any(), any());
    }

    @Test
    void saveWithEmptyName_shouldReturn400() throws Exception {
        expectedMessageAndStatusFrom(post(BOOK_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readStringFromResource("/json/request/invalid/new_music_book_without_name.json")),
                BAD_REQUEST, "Название книги не может быть пустым");
        verify(bookService, times(0)).save(any(), any());
    }

    @Test
    void saveWithEmptyAuthor_shouldReturn400() throws Exception {
        expectedMessageAndStatusFrom(post(BOOK_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readStringFromResource("/json/request/invalid/new_music_book_without_author.json")),
                BAD_REQUEST, "Имя автора не может быть пустым");
        verify(bookService, times(0)).save(any(), any());
    }

    @Test
    void updateById_shouldReturn200AndUpdateBookInDB() throws Exception {
        assertJsonEquals(
                readStringFromResource("/json/response/updated_sport_book.json"),
                expectedFrom(put(BOOK_BY_ID_ENDPOINT, 3).contentType(MediaType.APPLICATION_JSON)
                        .content(readStringFromResource("/json/request/update_sport_book.json")), OK)
        );

        Optional<Book> fromDB = bookRepository.findById(3L);
        assertFalse(fromDB.isEmpty());
        assertEquals(fromDB.get().getName(), "some new book about sport 1");
    }

    @Test
    void updateById_shouldCleanCaches() throws Exception {
        String author = "some sportsman 2", name = "some book about sport 2";
        expectedOkFromRequests(
                get(BOOK_BY_ID_ENDPOINT, "4"),
                get(BOOK_BY_ID_ENDPOINT, "1"),
                get(BOOKS_BY_CATEGORY_ENDPOINT, "sport"),
                get(BOOKS_BY_CATEGORY_ENDPOINT, "music"),
                get(BOOK_ENDPOINT).params(createParams("name", name, "author", author)));

        checkCachePresentations(Map.of(
                "book::4", true,
                "book::1", true,
                format("bookByNameAndAuthor::%s%s", name, author), true,
                "books::sport", true,
                "books::music", true));

        expectedFrom(put(BOOK_BY_ID_ENDPOINT, 4).contentType(MediaType.APPLICATION_JSON)
                .content(readStringFromResource("/json/request/update_sport_book.json")), OK);

        checkCachePresentations(Map.of(
                "book::4", false,
                "book::1", true,
                format("bookByNameAndAuthor::%s%s", name, author), false,
                "books::sport", false,
                "books::music", true));

        expectedOkFromRequests(
                get(BOOK_BY_ID_ENDPOINT, "4"),
                get(BOOK_BY_ID_ENDPOINT, "1"),
                get(BOOKS_BY_CATEGORY_ENDPOINT, "sport"),
                get(BOOKS_BY_CATEGORY_ENDPOINT, "music"));

        verify(bookService, times(2)).findById(4L);
        verify(bookService, times(1)).findById(1L);
        verify(bookService, times(2)).findByCategory("sport");
        verify(bookService, times(1)).findByCategory("music");
    }

    @Test
    void deleteById_shouldReturn204() throws Exception {
        expectedFrom(delete(BOOK_BY_ID_ENDPOINT, "1"), NO_CONTENT);
        assertTrue(bookRepository.findById(1L).isEmpty());
    }

    @Test
    void deleteById_shouldCleanCaches() throws Exception {
        String author = "some sportsman 2", name = "some book about sport 2";
        expectedOkFromRequests(
                get(BOOK_BY_ID_ENDPOINT, "4"),
                get(BOOK_BY_ID_ENDPOINT, "1"),
                get(BOOKS_BY_CATEGORY_ENDPOINT, "sport"),
                get(BOOKS_BY_CATEGORY_ENDPOINT, "music"),
                get(BOOK_ENDPOINT).params(createParams("name", name, "author", author)));

        checkCachePresentations(Map.of(
                "book::4", true,
                "book::1", true,
                format("bookByNameAndAuthor::%s%s", name, author), true,
                "books::sport", true,
                "books::music", true));

        expectedFrom(delete(BOOK_BY_ID_ENDPOINT, 4), NO_CONTENT);

        checkCachePresentations(Map.of(
                "book::4", false,
                "book::1", true,
                format("bookByNameAndAuthor::%s%s", name, author), false,
                "books::sport", false,
                "books::music", true));

        expectedOkFromRequests(
                get(BOOK_BY_ID_ENDPOINT, "1"),
                get(BOOKS_BY_CATEGORY_ENDPOINT, "sport"),
                get(BOOKS_BY_CATEGORY_ENDPOINT, "music"));

        verify(bookService, times(1)).findById(1L);
        verify(bookService, times(2)).findByCategory("sport");
        verify(bookService, times(1)).findByCategory("music");
    }

    @Test
    void deleteById_shouldReturn404() throws Exception {
        expectedMessageAndStatusFrom(
                delete(BOOK_BY_ID_ENDPOINT, "100"),
                NOT_FOUND, "Книга с id 100 не найдена"
        );
    }

    private void expectedOkFromRequests(MockHttpServletRequestBuilder... requests) throws Exception {
        for(MockHttpServletRequestBuilder request : requests) {
            expectedFrom(request, OK);
        }
    }

    private void checkCachePresentations(Map<String, Boolean> hasKeys) {
        hasKeys.forEach((key, expected) -> assertEquals(expected, redisTemplate.hasKey(key)));
    }



}
