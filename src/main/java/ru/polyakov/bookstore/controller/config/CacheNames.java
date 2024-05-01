package ru.polyakov.bookstore.controller.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "app")
public class CacheNames {

    private List<String> caches;
}
