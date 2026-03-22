package com.example.djinnianalyzer.controller;

import com.example.djinnianalyzer.dto.VacancyRequest;
import com.example.djinnianalyzer.dto.VacancyResponse;
import com.example.djinnianalyzer.service.VacancyMapper;
import com.example.djinnianalyzer.service.VacancyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vacancies")
public class VacancyController {

    private final VacancyService vacancyService;
    private final VacancyMapper vacancyMapper;

    public VacancyController(VacancyService vacancyService, VacancyMapper vacancyMapper) {
        this.vacancyService = vacancyService;
        this.vacancyMapper = vacancyMapper;
    }

    @GetMapping
    public List<VacancyResponse> findAll() {
        return vacancyService.findAll().stream().map(vacancyMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public VacancyResponse findById(@PathVariable Long id) {
        return vacancyMapper.toResponse(vacancyService.findById(id));
    }

    @PostMapping
    public VacancyResponse create(@Valid @RequestBody VacancyRequest request) {
        return vacancyMapper.toResponse(vacancyService.create(request));
    }

    @PutMapping("/{id}")
    public VacancyResponse update(@PathVariable Long id, @Valid @RequestBody VacancyRequest request) {
        return vacancyMapper.toResponse(vacancyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        vacancyService.delete(id);
    }
}
