package com.example.djinnianalyzer.service;

import com.example.djinnianalyzer.domain.Vacancy;
import com.example.djinnianalyzer.dto.VacancyRequest;
import com.example.djinnianalyzer.repository.VacancyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class VacancyService {

    private final VacancyRepository vacancyRepository;
    private final SkillService skillService;

    public VacancyService(VacancyRepository vacancyRepository, SkillService skillService) {
        this.vacancyRepository = vacancyRepository;
        this.skillService = skillService;
    }

    public List<Vacancy> findAll() {
        return vacancyRepository.findAll();
    }

    public Vacancy findById(Long id) {
        return vacancyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vacancy not found: " + id));
    }

    @Transactional
    public Vacancy create(VacancyRequest request) {
        Vacancy vacancy = new Vacancy();
        apply(vacancy, request);
        vacancy.setParsedAt(LocalDateTime.now());
        return vacancyRepository.save(vacancy);
    }

    @Transactional
    public Vacancy update(Long id, VacancyRequest request) {
        Vacancy vacancy = findById(id);
        apply(vacancy, request);
        return vacancyRepository.save(vacancy);
    }

    @Transactional
    public void delete(Long id) {
        vacancyRepository.deleteById(id);
    }

    private void apply(Vacancy vacancy, VacancyRequest request) {
        vacancy.setExternalId(request.externalId());
        vacancy.setTitle(request.title());
        vacancy.setCompany(request.company());
        vacancy.setCity(request.city());
        vacancy.setSalary(request.salary());
        vacancy.setExperience(request.experience());
        vacancy.setEmploymentType(request.employmentType());
        vacancy.setPublishedAtText(request.publishedAtText());
        vacancy.setUrl(request.url());
        vacancy.setDescription(request.description());
        vacancy.setRawRequirements(request.rawRequirements());
        vacancy.setProfession(request.profession());

        Set<com.example.djinnianalyzer.domain.Skill> skills = new HashSet<>();
        if (request.skills() != null) {
            request.skills().stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(skillService::getOrCreate)
                    .forEach(skills::add);
        }
        vacancy.setSkills(skills);
    }
}
