package com.example.djinnianalyzer.controller;

import com.example.djinnianalyzer.domain.Profession;
import com.example.djinnianalyzer.service.StatsService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final StatsService statsService;

    public DashboardController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("javaProfile", statsService.buildAverageProfile(Profession.JAVA, 10));
        model.addAttribute("pythonProfile", statsService.buildAverageProfile(Profession.PYTHON, 10));
        return "dashboard";
    }

}
