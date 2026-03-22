package com.example.djinnianalyzer.dto;

import com.example.djinnianalyzer.domain.Profession;

import java.time.LocalDateTime;
import java.util.Set;

public record VacancyResponse(
        Long id,
        String externalId,
        String title,
        String company,
        String city,
        String salary,
        String experience,
        String employmentType,
        String publishedAtText,
        String url,
        String description,
        String rawRequirements,
        Profession profession,
        Double extractedExperienceYears,
        LocalDateTime parsedAt,
        Set<String> skills
) {
}
