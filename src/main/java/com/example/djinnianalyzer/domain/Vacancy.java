package com.example.djinnianalyzer.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vacancies", uniqueConstraints = {
        @UniqueConstraint(columnNames = "external_id"),
        @UniqueConstraint(columnNames = "url")
})
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(nullable = false)
    private String title;

    private String company;
    private String city;
    private String salary;
    private String experience;
    private String employmentType;
    private String publishedAtText;

    @Column(nullable = false, length = 700)
    private String url;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String rawRequirements;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Profession profession;

    private Double extractedExperienceYears;

    @Column(nullable = false)
    private LocalDateTime parsedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parse_run_id")
    private ParseRun parseRun;

    @ManyToMany
    @JoinTable(
            name = "vacancy_skills",
            joinColumns = @JoinColumn(name = "vacancy_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public String getPublishedAtText() {
        return publishedAtText;
    }

    public void setPublishedAtText(String publishedAtText) {
        this.publishedAtText = publishedAtText;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRawRequirements() {
        return rawRequirements;
    }

    public void setRawRequirements(String rawRequirements) {
        this.rawRequirements = rawRequirements;
    }

    public Profession getProfession() {
        return profession;
    }

    public void setProfession(Profession profession) {
        this.profession = profession;
    }

    public Double getExtractedExperienceYears() {
        return extractedExperienceYears;
    }

    public void setExtractedExperienceYears(Double extractedExperienceYears) {
        this.extractedExperienceYears = extractedExperienceYears;
    }

    public LocalDateTime getParsedAt() {
        return parsedAt;
    }

    public void setParsedAt(LocalDateTime parsedAt) {
        this.parsedAt = parsedAt;
    }

    public ParseRun getParseRun() {
        return parseRun;
    }

    public void setParseRun(ParseRun parseRun) {
        this.parseRun = parseRun;
    }

    public Set<Skill> getSkills() {
        return skills;
    }

    public void setSkills(Set<Skill> skills) {
        this.skills = skills;
    }
}
