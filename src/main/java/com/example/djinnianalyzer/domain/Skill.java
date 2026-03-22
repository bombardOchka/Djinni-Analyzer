package com.example.djinnianalyzer.domain;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "skills", uniqueConstraints = @UniqueConstraint(columnNames = "normalized_name"))
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "normalized_name", nullable = false)
    private String normalizedName;

    @ManyToMany(mappedBy = "skills")
    private Set<Vacancy> vacancies = new HashSet<>();

    public Skill() {
    }

    public Skill(String name, String normalizedName) {
        this.name = name;
        this.normalizedName = normalizedName;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public Set<Vacancy> getVacancies() {
        return vacancies;
    }
}
