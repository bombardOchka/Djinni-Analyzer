package com.example.djinnianalyzer.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class SkillNormalizerService {

    private final Map<String, String> patterns = new LinkedHashMap<>();

    public SkillNormalizerService() {
        patterns.put("spring boot", "spring boot");
        patterns.put("spring framework", "spring");
        patterns.put("spring mvc", "spring mvc");
        patterns.put("spring", "spring");
        patterns.put("hibernate", "hibernate");
        patterns.put("jdbc", "jdbc");
        patterns.put("maven", "maven");
        patterns.put("gradle", "gradle");
        patterns.put("docker compose", "docker compose");
        patterns.put("docker", "docker");
        patterns.put("kubernetes", "kubernetes");
        patterns.put("aws", "aws");
        patterns.put("jenkins", "jenkins");
        patterns.put("ci/cd", "ci/cd");
        patterns.put("graphql", "graphql");
        patterns.put("microservice", "microservices");
        patterns.put("microservices", "microservices");
        patterns.put("oop", "oop");
        patterns.put("design pattern", "design patterns");
        patterns.put("sqlalchemy", "sqlalchemy");
        patterns.put("alembic", "alembic");
        patterns.put("pytest", "pytest");
        patterns.put("django", "django");
        patterns.put("fastapi", "fastapi");
        patterns.put("flask", "flask");
        patterns.put("mongodb", "mongodb");
        patterns.put("postgresql", "postgresql");
        patterns.put("postgres", "postgresql");
        patterns.put("mysql", "mysql");
        patterns.put("oracle", "oracle");
        patterns.put("nosql", "nosql");
        patterns.put("restful web services", "rest api");
        patterns.put("rest api", "rest api");
        patterns.put("rest ", "rest api");
        patterns.put("git", "git");
        patterns.put("linux", "linux");
        patterns.put("unix", "unix/linux");
        patterns.put("json", "json");
        patterns.put("xml", "xml");
        patterns.put("sql", "sql");
        patterns.put("openai", "openai api");
        patterns.put("angular", "angular");
        patterns.put("typescript", "typescript");
        patterns.put("javascript", "javascript");
        patterns.put("html", "html/css");
        patterns.put("css", "html/css");
        patterns.put("java", "java");
        patterns.put("python", "python");
    }

    public Optional<String> normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String source = ascii(raw).toLowerCase(Locale.ROOT).trim();
        for (Map.Entry<String, String> entry : patterns.entrySet()) {
            if (source.contains(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        if (source.length() < 2 || source.length() > 40) {
            return Optional.empty();
        }
        return Optional.of(source);
    }

    public String displayName(String normalized) {
        return switch (normalized) {
            case "ci/cd" -> "CI/CD";
            case "html/css" -> "HTML/CSS";
            case "rest api" -> "REST API";
            case "sql" -> "SQL";
            case "aws" -> "AWS";
            case "java" -> "Java";
            case "python" -> "Python";
            case "graphql" -> "GraphQL";
            case "postgresql" -> "PostgreSQL";
            case "mongodb" -> "MongoDB";
            case "json" -> "JSON";
            case "xml" -> "XML";
            default -> {
                String[] words = normalized.split(" ");
                StringBuilder builder = new StringBuilder();
                for (String word : words) {
                    if (word.isBlank()) {
                        continue;
                    }
                    builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
                }
                yield builder.toString().trim();
            }
        };
    }

    private String ascii(String raw) {
        return Normalizer.normalize(raw, Normalizer.Form.NFKC);
    }
}
