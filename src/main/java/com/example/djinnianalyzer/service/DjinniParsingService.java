package com.example.djinnianalyzer.service;

import com.example.djinnianalyzer.domain.ParseRun;
import com.example.djinnianalyzer.domain.ParseStatus;
import com.example.djinnianalyzer.domain.Profession;
import com.example.djinnianalyzer.domain.Skill;
import com.example.djinnianalyzer.domain.Vacancy;
import com.example.djinnianalyzer.dto.ParserResultResponse;
import com.example.djinnianalyzer.repository.ParseRunRepository;
import com.example.djinnianalyzer.repository.VacancyRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DjinniParsingService {

    private static final String USER_AGENT = "Mozilla/5.0 (compatible; DjinniAnalyzerLab/1.0; +https://localhost)";
    private static final Pattern VACANCY_URL_PATTERN = Pattern.compile("https://(?:www\\.)?djinni\\.co/jobs/(\\d+)(?:-[^/?#]+)?/?(?:\\?.*)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern TITLE_FROM_PAGE_TITLE_PATTERN = Pattern.compile("^(.*?)\\s+at\\s+(.*?)\\s+[–-]\\s*Djinni$", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile("(\\d+)(?:\\+)?\\s*(?:years?|рок(?:ів|и|у)|года|лет|yrs?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SALARY_PATTERN = Pattern.compile("(?:[$€£])\\s?\\d[\\d, ]*(?:\\s*[–-]\\s*(?:[$€£])?\\s?\\d[\\d, ]*)?");

    private final VacancyRepository vacancyRepository;
    private final ParseRunRepository parseRunRepository;
    private final SkillService skillService;
    private final SkillNormalizerService normalizerService;

    public DjinniParsingService(VacancyRepository vacancyRepository,
                                ParseRunRepository parseRunRepository,
                                SkillService skillService,
                                SkillNormalizerService normalizerService) {
        this.vacancyRepository = vacancyRepository;
        this.parseRunRepository = parseRunRepository;
        this.skillService = skillService;
        this.normalizerService = normalizerService;
    }

    @Transactional
    public ParserResultResponse run(Profession profession, int pages) {
        String sourceUrl = baseUrl(profession);
        ParseRun parseRun = new ParseRun();
        parseRun.setProfession(profession);
        parseRun.setStatus(ParseStatus.STARTED);
        parseRun.setSourceUrl(sourceUrl);
        parseRun.setPagesRequested(pages);
        parseRun.setStartedAt(LocalDateTime.now());
        parseRun = parseRunRepository.save(parseRun);

        List<String> savedUrls = new ArrayList<>();
        try {
            Set<String> vacancyUrls = collectVacancyLinks(sourceUrl, pages);
            System.out.println("SOURCE PAGE: " + sourceUrl);
            System.out.println("VACANCIES FOUND: " + vacancyUrls.size());
            for (String url : vacancyUrls) {

                Vacancy vacancy = parseVacancy(url, profession, parseRun);

                if (vacancy == null) {
                    continue;
                }

                vacancyRepository.save(vacancy);
                savedUrls.add(url);
            }
            parseRun.setStatus(ParseStatus.FINISHED);
            parseRun.setVacanciesFound(savedUrls.size());
            parseRun.setFinishedAt(LocalDateTime.now());
            parseRunRepository.save(parseRun);
            return new ParserResultResponse(profession, pages, savedUrls.size(), savedUrls, parseRun.getId());
        } catch (Exception ex) {
            parseRun.setStatus(ParseStatus.FAILED);
            parseRun.setNotes(ex.getMessage());
            parseRun.setFinishedAt(LocalDateTime.now());
            parseRunRepository.save(parseRun);
            throw new RuntimeException("Parsing failed: " + ex.getMessage(), ex);
        }
    }

    private Set<String> collectVacancyLinks(String sourceUrl, int pages) throws IOException {
        Set<String> urls = new LinkedHashSet<>();

        for (int page = 1; page <= Math.max(1, pages); page++) {
            String pageUrl = withPage(sourceUrl, page);
            Document document = connect(pageUrl);

            System.out.println("PAGE TITLE = " + document.title());

            Elements links = document.select("a[href]");

            int rawMatches = 0;

            for (Element link : links) {
                String href = link.attr("href");
                String absHref = link.absUrl("href");

                String candidate = absHref != null && !absHref.isBlank() ? absHref : href;
                String normalized = normalizeVacancyUrl(candidate);

                if (normalized != null) {
                    rawMatches++;
                    urls.add(normalized);
                    System.out.println("VACANCY LINK = " + normalized);
                }
            }

            System.out.println("RAW MATCHES ON PAGE = " + rawMatches);
        }

        return urls;
    }

    private Vacancy parseVacancy(String url, Profession profession, ParseRun parseRun) throws IOException {
        Document document = connect(url);

        String externalId = extractExternalId(url);

        if (vacancyRepository.findByExternalId(externalId).isPresent()) {
            return null;
        }

        Vacancy vacancy = new Vacancy();
        vacancy.setExternalId(externalId);
        vacancy.setUrl(url);
        vacancy.setProfession(profession);
        vacancy.setParseRun(parseRun);
        vacancy.setParsedAt(LocalDateTime.now());
        vacancy.setTitle(extractTitle(document));
        vacancy.setCompany(extractCompany(document));
        vacancy.setCity(extractCity(document));
        vacancy.setSalary(extractSalary(document));
        vacancy.setExperience(extractExperienceText(document));
        vacancy.setEmploymentType(extractEmploymentType(document));
        vacancy.setPublishedAtText(extractPublishedText(document));

        String description = firstNonBlank(
                extractSection(document, List.of(
                        "Job Description",
                        "Description",
                        "Responsibilities",
                        "Role Focus",
                        "Department/Project Description",
                        "About the role",
                        "Обов'язки",
                        "Обовʼязки",
                        "Опис вакансії"
                )),
                extractIntro(document)
        );

        String requirements = firstNonBlank(
                extractSection(document, List.of(
                        "Required skills",
                        "Required skills experience",
                        "Requirements",
                        "Core Requirements",
                        "Must have experience",
                        "Qualifications",
                        "Required",
                        "Вимоги",
                        "Наші очікування"
                )),
                extractRequirementsFallback(document)
        );

        String combinedText = String.join("\n\n",
                defaultString(vacancy.getTitle()),
                defaultString(vacancy.getCompany()),
                defaultString(description),
                defaultString(requirements),
                document.text());

        vacancy.setDescription(description.isBlank() ? document.text() : description);
        vacancy.setRawRequirements(requirements.isBlank() ? combinedText : requirements);
        vacancy.setExtractedExperienceYears(extractYears(combinedText));
        vacancy.setSkills(extractSkills(combinedText));

        return vacancy;
    }

    private Document connect(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .referrer("https://djinni.co/")
                .header("Accept-Language", "en-US,en;q=0.9,uk;q=0.8")
                .timeout(20000)
                .followRedirects(true)
                .get();
    }

    private String withPage(String sourceUrl, int page) {
        if (page <= 1) {
            return sourceUrl;
        }
        return sourceUrl + (sourceUrl.contains("?") ? "&" : "?") + "page=" + page;
    }

    private String normalizeVacancyUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        url = url.trim();

        if (url.startsWith("/jobs/")) {
            url = "https://djinni.co" + url;
        } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return null;
        }

        int queryIndex = url.indexOf('?');
        if (queryIndex >= 0) {
            url = url.substring(0, queryIndex);
        }

        int anchorIndex = url.indexOf('#');
        if (anchorIndex >= 0) {
            url = url.substring(0, anchorIndex);
        }

        Matcher matcher = VACANCY_URL_PATTERN.matcher(url);
        if (!matcher.matches()) {
            return null;
        }

        return url.endsWith("/") ? url : url + "/";
    }

    private Set<Skill> extractSkills(String text) {
        Set<Skill> result = new HashSet<>();
        List<String> candidates = List.of(
                "Java", "Python", "Spring Boot", "Spring Framework", "Spring MVC", "Spring Security", "Hibernate", "JDBC",
                "REST API", "RESTful Web Services", "PostgreSQL", "MongoDB", "MySQL", "MariaDB", "Oracle", "SQL",
                "Docker", "Kubernetes", "AWS", "GCP", "Azure", "CI/CD", "Jenkins", "Maven", "Gradle",
                "Django", "FastAPI", "Flask", "SQLAlchemy", "Alembic", "PyTest", "OpenAI", "Linux",
                "Git", "Kafka", "RabbitMQ", "Redis", "Elasticsearch", "Cassandra", "GraphQL",
                "Angular", "React", "Next.js", "TypeScript", "JavaScript", "HTML", "CSS", "JSON", "XML",
                "OOP", "Design Patterns", "Microservices", "TDD", "DevOps"
        );

        String source = text.toLowerCase(Locale.ROOT);
        for (String candidate : candidates) {
            Optional<String> normalized = normalizerService.normalize(candidate);
            if (normalized.isPresent() && source.contains(candidate.toLowerCase(Locale.ROOT))) {
                result.add(skillService.getOrCreate(candidate));
            }
        }
        return result;
    }

    private String extractTitle(Document document) {
        String title = cleanInlineText(text(document.selectFirst("h1")));
        if (!title.isBlank()) {
            return title;
        }
        Matcher matcher = TITLE_FROM_PAGE_TITLE_PATTERN.matcher(document.title());
        if (matcher.find()) {
            return cleanInlineText(matcher.group(1));
        }
        return "";
    }

    private String extractCompany(Document document) {
        Matcher matcher = TITLE_FROM_PAGE_TITLE_PATTERN.matcher(document.title());
        if (matcher.find()) {
            return cleanInlineText(matcher.group(2));
        }

        for (Element link : document.select("a[href]")) {
            String href = link.absUrl("href");
            if (href.contains("/jobs/company/") || href.contains("/companies/") || href.contains("/jobs/?company=")) {
                return cleanInlineText(link.text());
            }
        }
        return "";
    }

    private String extractCity(Document document) {
        String source = document.text();
        Matcher officeMatcher = Pattern.compile("Office Work in\\s+([A-Za-zА-Яа-яІіЇїЄє' .-]+)", Pattern.CASE_INSENSITIVE).matcher(source);
        if (officeMatcher.find()) {
            return cleanInlineText(officeMatcher.group(1));
        }

        Matcher officeTagMatcher = Pattern.compile("Office:\\s*([^*·]+?)(?:Countries where|Apply for the job|Employment:|Domain:|Test task)", Pattern.CASE_INSENSITIVE).matcher(source);
        if (officeTagMatcher.find()) {
            return cleanInlineText(officeTagMatcher.group(1));
        }

        Matcher remoteMatcher = Pattern.compile("(Full Remote|Worldwide|Countries of Europe or Ukraine|Ukraine|EU)", Pattern.CASE_INSENSITIVE).matcher(source);
        if (remoteMatcher.find()) {
            return cleanInlineText(remoteMatcher.group(1));
        }
        return "";
    }

    private String extractSalary(Document document) {
        Matcher matcher = SALARY_PATTERN.matcher(document.text());
        return matcher.find() ? cleanInlineText(matcher.group()) : "";
    }

    private String extractExperienceText(Document document) {
        String source = document.text();
        List<Pattern> patterns = List.of(
                Pattern.compile("Only from\\s+\\d+\\s+years? of experience", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\d+\\+?\\s+years? of [^.]{0,80}?experience", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\b[A-Za-z+#./-]+\\s+\\d+\\s+years\\b", Pattern.CASE_INSENSITIVE)
        );
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(source);
            if (matcher.find()) {
                return cleanInlineText(matcher.group());
            }
        }
        return "";
    }

    private String extractEmploymentType(Document document) {
        String source = document.text();
        Matcher matcher = Pattern.compile("Employment:\\s*(Fulltime|Part[- ]?time|Contract|Internship)", Pattern.CASE_INSENSITIVE).matcher(source);
        if (matcher.find()) {
            return cleanInlineText(matcher.group(1));
        }

        Matcher fallback = Pattern.compile("(Full[- ]?time|Part[- ]?time)", Pattern.CASE_INSENSITIVE).matcher(source);
        return fallback.find() ? cleanInlineText(fallback.group(1)) : "";
    }

    private String extractPublishedText(Document document) {
        Matcher matcher = Pattern.compile("Published\\s+[^·]+", Pattern.CASE_INSENSITIVE).matcher(document.text());
        return matcher.find() ? cleanInlineText(matcher.group()) : "";
    }

    private String extractSection(Document document, List<String> headings) {
        for (Element header : document.select("h1, h2, h3, h4, strong, b, p")) {
            String headerText = cleanInlineText(header.text());
            boolean matches = headings.stream().anyMatch(heading -> headerText.equalsIgnoreCase(heading));
            if (!matches) {
                continue;
            }
            StringBuilder builder = new StringBuilder();
            Element current = header.nextElementSibling();
            int steps = 0;
            while (current != null && steps < 40) {
                String tag = current.tagName();
                if (("h1".equals(tag) || "h2".equals(tag) || "h3".equals(tag) || "h4".equals(tag)) && !current.text().isBlank()) {
                    break;
                }
                String value = cleanInlineText(current.text());
                if (!value.isBlank()) {
                    builder.append(value).append("\n");
                }
                current = current.nextElementSibling();
                steps++;
            }
            String result = builder.toString().trim();
            if (!result.isBlank()) {
                return result;
            }
        }
        return "";
    }

    private String extractIntro(Document document) {
        StringBuilder builder = new StringBuilder();
        int taken = 0;
        for (Element paragraph : document.select("p")) {
            String value = cleanInlineText(paragraph.text());
            if (value.isBlank()) {
                continue;
            }
            String lower = value.toLowerCase(Locale.ROOT);
            if (lower.startsWith("required skills") || lower.startsWith("requirements") || lower.startsWith("published ")) {
                break;
            }
            builder.append(value).append("\n");
            taken++;
            if (taken >= 5) {
                break;
            }
        }
        return builder.toString().trim();
    }

    private String extractRequirementsFallback(Document document) {
        String source = document.text();
        List<Pattern> patterns = List.of(
                Pattern.compile("Required skills:?\\s*(.+?)(?:We offer:|Required languages|Published|Apply for the job|Employment:)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("Requirements:?\\s*(.+?)(?:Responsibilities:|Published|Apply for the job|Employment:)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("Core Requirements:?\\s*(.+?)(?:Current Tech Stack:|Role Focus|Published|Apply for the job)", Pattern.CASE_INSENSITIVE)
        );
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(source);
            if (matcher.find()) {
                return cleanInlineText(matcher.group(1));
            }
        }
        return "";
    }

    private Double extractYears(String source) {
        Matcher matcher = EXPERIENCE_PATTERN.matcher(source);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        return null;
    }

    private String extractExternalId(String url) {
        Matcher matcher = VACANCY_URL_PATTERN.matcher(url);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot extract external id from url: " + url);
        }
        return matcher.group(1);
    }

    private String baseUrl(Profession profession) {
        return switch (profession) {
            case JAVA -> "https://djinni.co/jobs/?primary_keyword=Java";
            case PYTHON -> "https://djinni.co/jobs/?primary_keyword=Python";
        };
    }

    private String cleanInlineText(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\u00A0', ' ')
                .replace(" Offline", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String text(Element element) {
        return element == null ? "" : element.text().trim();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
