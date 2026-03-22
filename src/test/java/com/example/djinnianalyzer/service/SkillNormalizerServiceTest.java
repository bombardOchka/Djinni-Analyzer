package com.example.djinnianalyzer.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillNormalizerServiceTest {

    private final SkillNormalizerService service = new SkillNormalizerService();

    @Test
    void shouldNormalizeSpringBoot() {
        assertEquals("spring boot", service.normalize("Spring Boot").orElseThrow());
    }

    @Test
    void shouldNormalizeRestfulServices() {
        assertEquals("rest api", service.normalize("RESTful Web Services").orElseThrow());
    }

    @Test
    void shouldIgnoreBlank() {
        assertTrue(service.normalize("   ").isEmpty());
    }
}
