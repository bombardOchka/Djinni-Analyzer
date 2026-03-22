package com.example.djinnianalyzer.dto;

import com.example.djinnianalyzer.domain.ParseStatus;
import com.example.djinnianalyzer.domain.Profession;

import java.time.LocalDateTime;

public record ParseRunResponse(
        Long id,
        Profession profession,
        ParseStatus status,
        String sourceUrl,
        Integer pagesRequested,
        Integer vacanciesFound,
        String notes,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
}
