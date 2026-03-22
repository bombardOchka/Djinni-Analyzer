package com.example.djinnianalyzer.service;

import com.example.djinnianalyzer.domain.Profession;
import com.example.djinnianalyzer.domain.Skill;
import com.example.djinnianalyzer.domain.Vacancy;
import com.example.djinnianalyzer.dto.AverageProfileResponse;
import com.example.djinnianalyzer.dto.SkillStatResponse;
import com.example.djinnianalyzer.repository.VacancyRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final VacancyRepository vacancyRepository;

    public StatsService(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    @Transactional(readOnly = true)
    public AverageProfileResponse buildAverageProfile(Profession profession, int top) {

        List<Vacancy> vacancies = vacancyRepository.findAllByProfessionWithSkills(profession);

        long totalVacancies = vacancies.size();

        Map<String, Long> skillCounts = new HashMap<>();
        List<Double> experienceValues = new ArrayList<>();

        for (Vacancy vacancy : vacancies) {

            if (vacancy.getExtractedExperienceYears() != null) {
                experienceValues.add(vacancy.getExtractedExperienceYears());
            }

            for (Skill skill : vacancy.getSkills()) {
                skillCounts.merge(skill.getName(), 1L, Long::sum);
            }
        }

        Double avgExperience = experienceValues.isEmpty()
                ? null
                : experienceValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        List<SkillStatResponse> topSkills = skillCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(top)
                .map(entry -> new SkillStatResponse(
                        entry.getKey(),
                        entry.getValue(),
                        totalVacancies == 0 ? 0.0 : Math.round(entry.getValue() * 10000.0 / totalVacancies) / 100.0
                ))
                .toList();

        return new AverageProfileResponse(profession, totalVacancies, avgExperience, topSkills);
    }

    @Transactional(readOnly = true)
    public List<SkillStatResponse> buildTopSkills(Profession profession, int top) {
        return buildAverageProfile(profession, top).topSkills();
    }
}