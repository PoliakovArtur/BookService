package ru.polyakov.bookstore.utils;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static java.text.MessageFormat.format;

public class TestUtils {

    private TestUtils() {}

    private final static ResourceLoader resourceLoader = new DefaultResourceLoader();

    public static String readStringFromResource(String resourcePath) {
        Resource resource = resourceLoader.getResource(format("classpath:{0}", resourcePath));

        try(Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
