package com.example.djinnianalyzer.repository;

import com.example.djinnianalyzer.domain.Profession;
import com.example.djinnianalyzer.domain.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VacancyRepository extends JpaRepository<Vacancy, Long> {
    Optional<Vacancy> findByExternalId(String externalId);
    Optional<Vacancy> findByUrl(String url);
    List<Vacancy> findByProfessionOrderByParsedAtDesc(Profession profession);
    long countByProfession(Profession profession);
    @Query("""
       select distinct v
       from Vacancy v
       left join fetch v.skills
       where v.profession = :profession
       """)
    List<Vacancy> findAllByProfessionWithSkills(@Param("profession") Profession profession);
}
