package com.example.djinnianalyzer.service;

import com.example.djinnianalyzer.domain.Profession;
import com.example.djinnianalyzer.dto.AverageProfileResponse;
import com.example.djinnianalyzer.dto.ParserResultResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TelegramBotService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final DjinniParsingService parsingService;
    private final StatsService statsService;
    private final VacancyService vacancyService;

    private final String token;
    private final boolean enabled;
    private final String username;

    private final AtomicLong updateOffset = new AtomicLong(0);

    public TelegramBotService(RestClient restClient,
                              ObjectMapper objectMapper,
                              DjinniParsingService parsingService,
                              StatsService statsService,
                              VacancyService vacancyService,
                              @Value("${telegram.bot.token:}") String token,
                              @Value("${telegram.bot.enabled:false}") boolean enabled,
                              @Value("${telegram.bot.username:djinni_lab_bot}") String username) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.parsingService = parsingService;
        this.statsService = statsService;
        this.vacancyService = vacancyService;
        this.token = token;
        this.enabled = enabled;
        this.username = username;
    }

    @Scheduled(fixedDelayString = "${telegram.bot.poll-delay-ms:5000}")
    public void poll() {
        if (!enabled || token == null || token.isBlank()) {
            return;
        }

        try {
            String body = restClient.get()
                    .uri("https://api.telegram.org/bot" + token + "/getUpdates?timeout=0&offset=" + updateOffset.get())
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(body);

            for (JsonNode update : root.path("result")) {
                long updateId = update.path("update_id").asLong();

                try {
                    JsonNode message = update.path("message");
                    if (message.isMissingNode()) {
                        updateOffset.set(updateId + 1);
                        continue;
                    }

                    long chatId = message.path("chat").path("id").asLong();
                    String text = message.path("text").asText("").trim();

                    if (!text.isBlank()) {
                        System.out.println("TG COMMAND: " + text);
                        handleCommand(chatId, text);
                    }

                    updateOffset.set(updateId + 1);

                } catch (Exception e) {
                    System.out.println("ERROR while handling update " + updateId + ": " + e.getMessage());
                    e.printStackTrace();

                    try {
                        long chatId = update.path("message").path("chat").path("id").asLong();
                        if (chatId != 0) {
                            sendMessage(chatId, "Помилка під час обробки команди: " + e.getMessage());
                        }
                    } catch (Exception ignored) {
                    }

                    updateOffset.set(updateId + 1);
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR while polling Telegram: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleCommand(long chatId, String command) {
        switch (command) {
            case "/start" -> sendMessage(chatId,
                    "Команди:\n" +
                            "/java - середній профіль Java\n" +
                            "/python - середній профіль Python\n" +
                            "/refresh_java - оновити Java вакансії\n" +
                            "/refresh_python - оновити Python вакансії\n" +
                            "/latest_java - 5 останніх Java вакансій\n" +
                            "/latest_python - 5 останніх Python вакансій");

            case "/java" -> {
                System.out.println("BUILD JAVA PROFILE");
                sendMessage(chatId, formatProfile(statsService.buildAverageProfile(Profession.JAVA, 10)));
            }

            case "/python" -> {
                System.out.println("BUILD PYTHON PROFILE");
                sendMessage(chatId, formatProfile(statsService.buildAverageProfile(Profession.PYTHON, 10)));
            }

            case "/refresh_java" -> {
                System.out.println("START JAVA PARSING");
                ParserResultResponse result = parsingService.run(Profession.JAVA, 1);
                sendMessage(chatId,
                        "Java вакансії оновлено.\n" +
                                "Знайдено: " + result.savedVacancies() + "\n\n" +
                                formatProfile(statsService.buildAverageProfile(Profession.JAVA, 10)));
            }

            case "/refresh_python" -> {
                System.out.println("START PYTHON PARSING");
                ParserResultResponse result = parsingService.run(Profession.PYTHON, 1);
                sendMessage(chatId,
                        "Python вакансії оновлено.\n" +
                                "Знайдено: " + result.savedVacancies() + "\n\n" +
                                formatProfile(statsService.buildAverageProfile(Profession.PYTHON, 10)));
            }

            case "/latest_java" -> sendMessage(chatId, latestVacancies(Profession.JAVA));
            case "/latest_python" -> sendMessage(chatId, latestVacancies(Profession.PYTHON));
            default -> sendMessage(chatId, "Невідома команда. Використай /start");
        }
    }

    private String latestVacancies(Profession profession) {
        List<String> lines = new ArrayList<>();
        vacancyService.findAll().stream()
                .filter(v -> v.getProfession() == profession)
                .sorted((a, b) -> b.getParsedAt().compareTo(a.getParsedAt()))
                .limit(5)
                .forEach(v -> lines.add("- " + v.getTitle() + " | " + v.getCompany() + " | " + v.getUrl()));

        if (lines.isEmpty()) {
            return "Даних поки немає.";
        }
        return String.join("\n", lines);
    }

    private String formatProfile(AverageProfileResponse profile) {
        StringBuilder builder = new StringBuilder();
        builder.append("Профіль: ").append(profile.profession()).append("\n")
                .append("Кількість вакансій: ").append(profile.totalVacancies()).append("\n");
        if (profile.averageExperienceYears() != null) {
            builder.append("Середній досвід: ")
                    .append(String.format(java.util.Locale.US, "%.2f", profile.averageExperienceYears()))
                    .append(" років\n");
        }
        builder.append("Топ навички:\n");
        profile.topSkills().forEach(skill -> builder.append("- ")
                .append(skill.skill())
                .append(" — ")
                .append(skill.sharePercent())
                .append("%\n"));
        return builder.toString();
    }

    private void sendMessage(long chatId, String text) {
        String response = restClient.post()
                .uri("https://api.telegram.org/bot" + token + "/sendMessage")
                .body(java.util.Map.of(
                        "chat_id", chatId,
                        "text", text,
                        "disable_web_page_preview", true
                ))
                .retrieve()
                .body(String.class);

        System.out.println("SEND MESSAGE RESPONSE: " + response);
    }

    public String getUsername() {
        return username;
    }
}
