package com.example.djinnianalyzer.dto;

import com.example.djinnianalyzer.domain.Profession;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record VacancyRequest(
        @NotBlank String externalId,
        @NotBlank String title,
        String company,
        String city,
        String salary,
        String experience,
        String employmentType,
        String publishedAtText,
        @NotBlank String url,
        String description,
        String rawRequirements,
        @NotNull Profession profession,
        Set<String> skills
) {
}
