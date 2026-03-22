package com.example.djinnianalyzer.controller;

import com.example.djinnianalyzer.domain.Profession;
import com.example.djinnianalyzer.dto.ParseRunResponse;
import com.example.djinnianalyzer.dto.ParserResultResponse;
import com.example.djinnianalyzer.repository.ParseRunRepository;
import com.example.djinnianalyzer.service.DjinniParsingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/parser")
public class ParserController {

    private final DjinniParsingService parsingService;
    private final ParseRunRepository parseRunRepository;

    public ParserController(DjinniParsingService parsingService, ParseRunRepository parseRunRepository) {
        this.parsingService = parsingService;
        this.parseRunRepository = parseRunRepository;
    }

    @PostMapping("/run")
    public ParserResultResponse run(@RequestParam Profession profession,
                                    @RequestParam(defaultValue = "1") int pages) {
        return parsingService.run(profession, pages);
    }

    @GetMapping("/runs")
    public List<ParseRunResponse> runs() {
        return parseRunRepository.findAll().stream()
                .map(run -> new ParseRunResponse(
                        run.getId(),
                        run.getProfession(),
                        run.getStatus(),
                        run.getSourceUrl(),
                        run.getPagesRequested(),
                        run.getVacanciesFound(),
                        run.getNotes(),
                        run.getStartedAt(),
                        run.getFinishedAt()
                ))
                .toList();
    }
}
