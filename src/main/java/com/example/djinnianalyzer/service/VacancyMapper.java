package com.example.djinnianalyzer.service;

import com.example.djinnianalyzer.domain.Skill;
import com.example.djinnianalyzer.domain.Vacancy;
import com.example.djinnianalyzer.dto.VacancyResponse;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component
public class VacancyMapper {

    public VacancyResponse toResponse(Vacancy vacancy) {
        Set<String> skills = vacancy.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toCollection(TreeSet::new));

        return new VacancyResponse(
                vacancy.getId(),
                vacancy.getExternalId(),
                vacancy.getTitle(),
                vacancy.getCompany(),
                vacancy.getCity(),
                vacancy.getSalary(),
                vacancy.getExperience(),
                vacancy.getEmploymentType(),
                vacancy.getPublishedAtText(),
                vacancy.getUrl(),
                vacancy.getDescription(),
                vacancy.getRawRequirements(),
                vacancy.getProfession(),
                vacancy.getExtractedExperienceYears(),
                vacancy.getParsedAt(),
                skills
        );
    }
}
