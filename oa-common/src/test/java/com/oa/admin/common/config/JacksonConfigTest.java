package com.oa.admin.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonConfigTest {

    @Test
    void localDateTimeCustomizer_formatsResponseDateTimeAsExpected() throws Exception {
        JacksonConfig config = new JacksonConfig();
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.modules(new JavaTimeModule());
        config.localDateTimeCustomizer().customize(builder);
        ObjectMapper objectMapper = builder.build();

        String json = objectMapper.writeValueAsString(new TimePayload(LocalDateTime.of(2026, 5, 17, 18, 32, 40)));

        assertEquals("{\"createdAt\":\"2026-05-17 18:32:40\"}", json);
    }

    private record TimePayload(LocalDateTime createdAt) {
    }
}
