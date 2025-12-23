# Repository Guidelines

## Project Structure & Module Organization
- Requirements and design specs live in `.kiro/specs/smart-campus-lost-found/` -- treat them as the source of truth for scope and domain language.
- Backend (Spring Boot, planned path): `src/main/java/com/campus/lostandfound/` with `config/`, `controller/`, `service/`, `repository/`, `model/{entity,dto,vo}/`, `common/`, `exception/`, `util/`, `websocket/`; configs and static assets in `src/main/resources/`.
- Backend tests mirror package layout under `src/test/java/com/campus/lostandfound/`.
- Frontend (Vue 3 + Vite + Element Plus, if/when added): `web/` with `src/components/`, `src/views/`, `src/stores/` (Pinia), assets in `src/assets/`.

## Build, Test, and Development Commands
- `mvn clean verify` - compile and run unit/integration tests for the Spring Boot service.
- `mvn spring-boot:run` - start the API locally using `application.yml` and active profile overrides.
- In `web/`: `npm install`, then `npm run dev -- --host` for local dev; `npm run build` for production bundle.
- Use `SPRING_PROFILES_ACTIVE=local` to load local-only secrets and endpoints; keep cloud keys out of VCS.

## Coding Style & Naming Conventions
- Java 17+, Spring Boot idioms; 4-space indent; classes UpperCamelCase, methods/fields lowerCamelCase; constants UPPER_SNAKE_CASE.
- REST endpoints: nouns, kebab-case paths, versioned prefix (e.g., `/api/v1/items`); use DTO/VO to separate payloads from entities.
- Frontend: follow Vue single-file component style; script setup; PascalCase components; use ESLint/Prettier defaults if configured.

## Testing Guidelines
- Prefer JUnit 5 + Spring Boot Test; name classes `*Test`. Mock external calls (Baidu AI, AMap LBS, OSS) and cover service-layer matching/recommendation rules.
- Integration tests should seed in-memory DB data and assert security (JWT required), soft deletes, and rate limits.
- Frontend: add component tests (Vitest) for forms, validation, and request/response handling.

## Commit & Pull Request Guidelines
- No git history yet: use Conventional Commits (`feat:`, `fix:`, `chore:`, `docs:`, `test:`, `build:`, `refactor:`).
- PRs should include: a concise summary, linked issue/requirement ID from `.kiro` when applicable, test evidence (`mvn clean verify`, `npm test`/`npm run build`), and UI screenshots/GIFs for visible changes.

## Security & Configuration Tips
- Keep Baidu AI, AMap (LBS), and OSS credentials in environment variables or an untracked `application-local.yml`; never commit keys.
- Enforce JWT on sensitive routes, validate file types/sizes (images only, <=10 MB), and keep soft deletes for item records.
- Log errors without leaking tokens or PII; rate-limit per IP as specified (e.g., 100 req/min) and cache hotspots in Redis.
