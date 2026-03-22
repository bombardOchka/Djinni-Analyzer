package com.example.djinnianalyzer.service;

import com.example.djinnianalyzer.domain.Skill;
import com.example.djinnianalyzer.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final SkillNormalizerService normalizerService;

    public SkillService(SkillRepository skillRepository, SkillNormalizerService normalizerService) {
        this.skillRepository = skillRepository;
        this.normalizerService = normalizerService;
    }

    @Transactional
    public Skill getOrCreate(String rawSkill) {
        String normalized = normalizerService.normalize(rawSkill)
                .orElseThrow(() -> new IllegalArgumentException("Cannot normalize skill: " + rawSkill));

        return skillRepository.findByNormalizedName(normalized)
                .orElseGet(() -> skillRepository.save(new Skill(normalizerService.displayName(normalized), normalized)));
    }
}
