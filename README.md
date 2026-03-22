# Djinni Analyzer

Лабораторна робота: REST-застосунок на Spring Boot для парсингу вакансій з Djinni, збереження у БД та побудови "середнього профілю" вимог для Java і Python розробників.

## Що реалізовано

- Spring Boot 3 + Java 17
- REST API
- CRUD для сутності `Vacancy`
- 3 пов'язані таблиці: `vacancies`, `skills`, `parse_runs`
- зв'язки `Many-to-Many` (`vacancy_skills`) і `Many-to-One`
- парсер Djinni через Jsoup
- статистика по навичках і середньому досвіду
- Thymeleaf dashboard
- простий Telegram bot wrapper через Telegram Bot HTTP API

## Архітектура

- `controller` — REST та MVC контролери
- `service` — бізнес-логіка, парсинг, агрегація статистики, Telegram bot
- `repository` — доступ до БД через Spring Data JPA
- `domain` — JPA сутності
- `dto` — DTO для REST

## Основні сутності

### Vacancy
Зберігає вакансію: назва, компанія, місто / формат роботи, URL, опис, raw requirements, професія, досвід, час парсингу.

### Skill
Нормалізована навичка, яка прив'язується до багатьох вакансій.

### ParseRun
Окремий запуск парсера: професія, URL джерела, статус, кількість знайдених вакансій, час початку/завершення.

## REST API

### CRUD вакансій
- `GET /api/vacancies`
- `GET /api/vacancies/{id}`
- `POST /api/vacancies`
- `PUT /api/vacancies/{id}`
- `DELETE /api/vacancies/{id}`

### Парсер
- `POST /api/parser/run?profession=JAVA&pages=1`
- `POST /api/parser/run?profession=PYTHON&pages=1`
- `GET /api/parser/runs`

### Статистика
- `GET /api/stats/skills?profession=JAVA&top=10`
- `GET /api/stats/profile?profession=PYTHON&top=10`

### Dashboard
- `GET /dashboard`

## Які сторінки парсяться

- Java: `https://djinni.co/jobs/keyword-java/`
- Python: `https://djinni.co/jobs/keyword-python/`

Для кожної вакансії зберігаються:
- title
- company
- city / remote format
- url
- description
- raw requirements
- employment type
- published text
- extracted experience years
- normalized skills

## Як запустити

### 1. H2 (за замовчуванням)
```bash
mvn spring-boot:run
```

### 2. PostgreSQL
Створи БД `djinni_lab` і запусти:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

## Telegram bot
Увімкнення через env-змінні:
```bash
export TELEGRAM_BOT_TOKEN=your_token
export TELEGRAM_BOT_USERNAME=your_bot_name
```

В `application.yml`:
```yaml
telegram:
  bot:
    enabled: true
```

Команди бота:
- `/start`
- `/java`
- `/python`
- `/refresh_java`
- `/refresh_python`
- `/latest_java`
- `/latest_python`

## Особливості парсера

1. Використовується двоетапний обхід:
   - збір посилань на вакансії зі сторінок keyword search;
   - перехід на detail page кожної вакансії.
2. Парсер не залежить від одного CSS selector-а: є fallback extraction для title, company, intro, requirements, employment type, experience.
3. Модуль агрегації будує середній профіль за частотою навичок і середнім досвідом.

## Що варто доробити

1. Винести словник навичок у конфіг / окрему таблицю.
2. Додати rate limit, retry policy та backoff.
3. Додати логування і unit/integration tests для HTML extraction.
4. Додати планувальник автоматичного оновлення.
5. Додати окремий сервіс для адаптації парсера при зміні HTML-структури.

## Важливе зауваження

HTML-структура Djinni може змінюватися. Також для реального використання потрібно окремо перевірити актуальні умови використання сервісу та запускати парсер з помірною частотою запитів.
