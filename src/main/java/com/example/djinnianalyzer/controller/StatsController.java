package com.example.djinnianalyzer.controller;

import com.example.djinnianalyzer.domain.Profession;
import com.example.djinnianalyzer.dto.AverageProfileResponse;
import com.example.djinnianalyzer.dto.SkillStatResponse;
import com.example.djinnianalyzer.service.StatsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/skills")
    public List<SkillStatResponse> topSkills(@RequestParam Profession profession,
                                             @RequestParam(defaultValue = "10") int top) {
        return statsService.buildTopSkills(profession, top);
    }

    @GetMapping("/profile")
    public AverageProfileResponse averageProfile(@RequestParam Profession profession,
                                                 @RequestParam(defaultValue = "10") int top) {
        return statsService.buildAverageProfile(profession, top);
    }
}