package com.example.djinnianalyzer.dto;

import com.example.djinnianalyzer.domain.Profession;

import java.util.List;

public record ParserResultResponse(
        Profession profession,
        int requestedPages,
        int savedVacancies,
        List<String> vacancyUrls,
        Long parseRunId
) {
}
