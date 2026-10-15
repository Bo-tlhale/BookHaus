# BookHaus

BookHaus is a personal reading and library application for discovering books, building a private library, organising shelves, tracking reading activity, saving journal entries and quotes, and working toward reading goals.

## Project status

The backend is implemented through **Phase 8**.

| Phase | Status | Scope |
| --- | --- | --- |
| Backend foundation | Complete | Spring Boot project structure, PostgreSQL configuration and validation |
| Domain model | Complete | Users, books, authors, genres, library books, shelves, journals, quotes and goals |
| Database migrations | Complete | Flyway migrations V1-V6 for the current domain schema |
| Authentication | Complete | Registration, login, BCrypt password hashing, JWT authentication and current-user lookup |
| Phase 5: books | Complete | Open Library search, local book persistence, author/genre reuse and add-to-library flow |
| Phase 6: library and shelves | Complete | Personal library browsing, status/progress/rating updates, shelf management and ordering |
| Phase 7: journals and quotes | Complete | Private reading reflections and favourite quote management |
| Phase 8: goals and statistics | Complete | Annual reading goals, derived progress and personal reading insights |
| Later application features | Planned | API documentation, CI and frontend |

## Technology

- Java 21
- Spring Boot 3.5.16
- Spring Web and synchronous `RestClient`
- Spring Data JPA
- Spring Security with JWT authentication
- Bean Validation
- PostgreSQL
- Flyway
- Lombok
- Maven
- Open Library Search and Covers APIs

## Development environment

BookHaus currently uses a **locally installed PostgreSQL instance**. Docker and Docker Compose are not part of the current development setup.

Required software:

- Java 21
- Maven 3.9+
- PostgreSQL 15 or a compatible supported version

### 1. Create the database

Create a PostgreSQL database named `bookhaus` and grant your chosen PostgreSQL user access to it.

### 2. Configure environment variables

Copy `.env.example` to `.env` in the repository root:

```powershell
Copy-Item .env.example .env
```

Set your local values:

```properties
DB_URL=jdbc:postgresql://localhost:5432/bookhaus
DB_USERNAME=your_postgres_user
DB_PASSWORD=your_postgres_password
JWT_SECRET=your_base64_encoded_secret_of_at_least_32_bytes
JWT_EXPIRATION=86400000
OPEN_LIBRARY_BASE_URL=https://openlibrary.org
```

Generate a suitable JWT secret in PowerShell:

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

The development profile imports the root `.env` file. `.env` is ignored by Git and must never be committed.

### 3. Run the backend

From the backend directory:

```powershell
cd backend
mvn spring-boot:run
```

The API starts on `http://localhost:8080` by default. Flyway validates and applies outstanding migrations automatically during startup, while Hibernate validates the resulting schema.

Stop the application with `Ctrl+C` in the terminal where Maven is running.

## API endpoints

Authentication endpoints are public. All other application endpoints require:

```http
Authorization: Bearer <jwt-token>
```

| Method | Endpoint | Authentication | Description |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Public | Create an account and receive a JWT |
| `POST` | `/api/auth/login` | Public | Authenticate and receive a JWT |
| `GET` | `/api/users/me` | Required | Return the authenticated user |
| `GET` | `/api/books/search?q={query}` | Required | Search Open Library without persisting results |
| `GET` | `/api/books/{id}` | Required | Return a locally persisted book |
| `POST` | `/api/library/books` | Required | Add an Open Library result to the authenticated user's library |
| `GET` | `/api/library/books` | Required | List the user's library, optionally filtered by reading status |
| `GET` | `/api/library/books/{id}` | Required | Return one owned library item |
| `PATCH` | `/api/library/books/{id}/status` | Required | Update reading status |
| `PATCH` | `/api/library/books/{id}/progress` | Required | Update the current page |
| `PATCH` | `/api/library/books/{id}/rating` | Required | Set or clear a personal rating |
| `DELETE` | `/api/library/books/{id}` | Required | Remove a book from the user's library |
| `GET` | `/api/shelves` | Required | List the user's shelves in display order |
| `GET` | `/api/shelves/{id}` | Required | Return an owned shelf and its ordered books |
| `POST` | `/api/shelves` | Required | Create a shelf |
| `PATCH` | `/api/shelves/{id}` | Required | Rename a shelf |
| `DELETE` | `/api/shelves/{id}` | Required | Delete a shelf without deleting library books |
| `POST` | `/api/shelves/{id}/books` | Required | Add an owned library book to a shelf |
| `DELETE` | `/api/shelves/{id}/books/{libraryBookId}` | Required | Remove a book from a shelf only |
| `PATCH` | `/api/shelves/reorder` | Required | Reorder all shelves using an ordered ID list |
| `PATCH` | `/api/shelves/{id}/books/reorder` | Required | Reorder all books on a shelf |
| `GET` | `/api/library/books/{id}/journal` | Required | Return the journal for an owned library item |
| `PUT` | `/api/library/books/{id}/journal` | Required | Create or replace the journal content for an owned library item |
| `GET` | `/api/library/books/{id}/quotes` | Required | List an owned library item's quotes oldest first |
| `POST` | `/api/library/books/{id}/quotes` | Required | Add a favourite quote to an owned library item |
| `PATCH` | `/api/library/books/{id}/quotes/{quoteId}` | Required | Update a quote belonging to the selected library item |
| `DELETE` | `/api/library/books/{id}/quotes/{quoteId}` | Required | Delete only the selected quote |
| `POST` | `/api/goals` | Required | Create one annual reading goal per year |
| `GET` | `/api/goals` | Required | List the user's reading goals by year descending |
| `GET` | `/api/goals/{year}` | Required | Return the user's goal and derived progress for a year |
| `PATCH` | `/api/goals/{year}` | Required | Update an owned goal's target |
| `DELETE` | `/api/goals/{year}` | Required | Delete an owned goal only |
| `GET` | `/api/stats` | Required | Return the user's library totals, reading metrics, genres and current goal |
| `GET` | `/api/stats/year/{year}` | Required | Return completion statistics restricted to the selected year |

Personal-resource ownership is always derived from the authenticated user. Client requests cannot assign library data to an arbitrary user ID.

Journal retrieval returns `404 Not Found` until a journal has been created. Journal text preserves user-entered casing, punctuation, whitespace and line breaks. Quotes are returned in ascending creation order, and quote text is trimmed only at its leading and trailing edges.

Goal completion counts only `COMPLETED` books with a `finishedDate` in the goal year. Goal percentages, remaining books and pace are calculated at request time rather than persisted. Current-year pace compares completed-target progress with the elapsed portion of the year; past years require the final target to have been met, while future goals are considered on pace before progress is expected.

Statistics are derived from the authenticated user's library. Total pages count known full page counts for completed books and current-page progress for currently reading or DNF books. Because reading sessions are not tracked, yearly pages conservatively include only known page counts for books completed in that year; they do not estimate pages from ongoing books. Unrated books are excluded from averages, and completed books contribute to every associated genre.

### Example authentication flow

Register:

```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "BookHaus Reader",
  "email": "reader@example.com",
  "password": "BookHaus@123!"
}
```

Use the returned token for a book search:

```http
GET /api/books/search?q=Dune
Authorization: Bearer <jwt-token>
```

## Book integration behaviour

- Searches use Open Library's work-oriented Search API.
- Search results are mapped into BookHaus DTOs; raw provider responses are not exposed.
- Searching does not create database records.
- Cover URLs are constructed from Open Library cover IDs; image files are not downloaded or stored.
- Saved books are reused by provider/work ID first, ISBN second, and an exact title/year fallback last.
- Authors and genres are trimmed, whitespace-normalised and reused case-insensitively where possible.
- At most 10 Open Library subjects are stored as genres for a book.
- New library entries default to `TO_READ` when no reading status is supplied.
- Adding the same book to the same user's library returns `409 Conflict`.

## Tests and build

Run the complete automated test suite:

```powershell
cd backend
mvn test
```

Build the executable JAR:

```powershell
mvn package
```

The tests cover authentication, JWT validation, protected endpoints, Open Library mapping and failures, book persistence, library updates, derived progress, shelf management, journal upserts, quote validation and ordering, annual goals, fixed-clock pace calculations, statistics, conflicts, ownership, and book details.

## Repository structure

```text
bookhaus/
|-- backend/
|   |-- src/main/java/com/bookhaus/
|   |   |-- auth/
|   |   |-- book/
|   |   |-- config/
|   |   |-- goal/
|   |   |-- journal/
|   |   |-- library/
|   |   |-- shelf/
|   |   |-- stats/
|   |   `-- user/
|   |-- src/main/resources/
|   |   |-- db/migration/
|   |   |-- application.yml
|   |   `-- application-dev.yml
|   |-- src/test/java/com/bookhaus/
|   `-- pom.xml
|-- .env.example
|-- .gitignore
`-- README.md
```

The frontend and CI configuration will be introduced in later phases. Empty placeholder directories are intentionally avoided.

## Architecture principles

- Package code by feature.
- Keep API DTOs separate from persistence entities.
- Derive personal-resource ownership from authenticated identity.
- Keep secrets and local configuration out of source control.
- Manage schema changes exclusively through versioned Flyway migrations.
- Use JPA schema validation rather than automatic schema mutation.
- Keep third-party integrations behind application-owned interfaces.
- Add infrastructure and dependencies only when a concrete phase requires them.

## Next phases

- OpenAPI documentation and CI
- React frontend and the later immersive reading-room experience
