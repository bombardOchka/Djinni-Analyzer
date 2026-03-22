package com.example.djinnianalyzer.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parse_runs")
public class ParseRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Profession profession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParseStatus status;

    @Column(nullable = false)
    private String sourceUrl;

    private Integer pagesRequested;
    private Integer vacanciesFound;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @OneToMany(mappedBy = "parseRun", cascade = CascadeType.ALL)
    private List<Vacancy> vacancies = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Profession getProfession() {
        return profession;
    }

    public void setProfession(Profession profession) {
        this.profession = profession;
    }

    public ParseStatus getStatus() {
        return status;
    }

    public void setStatus(ParseStatus status) {
        this.status = status;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Integer getPagesRequested() {
        return pagesRequested;
    }

    public void setPagesRequested(Integer pagesRequested) {
        this.pagesRequested = pagesRequested;
    }

    public Integer getVacanciesFound() {
        return vacanciesFound;
    }

    public void setVacanciesFound(Integer vacanciesFound) {
        this.vacanciesFound = vacanciesFound;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public List<Vacancy> getVacancies() {
        return vacancies;
    }
}
