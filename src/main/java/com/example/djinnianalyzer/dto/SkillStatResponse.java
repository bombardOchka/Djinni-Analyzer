package com.example.djinnianalyzer.dto;

public record SkillStatResponse(
        String skill,
        long vacancyCount,
        double sharePercent
) {
}
