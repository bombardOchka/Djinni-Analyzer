package com.example.djinnianalyzer.repository;

import com.example.djinnianalyzer.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByNormalizedName(String normalizedName);
}
