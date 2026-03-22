package com.example.djinnianalyzer.dto;

import com.example.djinnianalyzer.domain.Profession;

import java.util.List;

public record AverageProfileResponse(
        Profession profession,
        long totalVacancies,
        Double averageExperienceYears,
        List<SkillStatResponse> topSkills
) {
}
