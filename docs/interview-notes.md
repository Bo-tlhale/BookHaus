# BookHaus interview notes

## One-minute overview

BookHaus is a full-stack personal reading application. I built it to show how a product can move from an external catalog result to secure, user-owned data with useful derived insights. The React client handles discovery, library organisation, journals, goals and visual statistics. The Spring Boot API owns authentication, validation, business rules and Open Library integration. PostgreSQL stores the normalized domain and Flyway controls every schema change.

## Problem and product decisions

The core problem was larger than saving a list of titles. A reader needs provider metadata, personal reading state, progress, ratings, thematic shelves, reflections and goals without those concerns becoming tangled.

I separated reusable `Book` metadata from `LibraryBook`, which represents one user's relationship with a book. That decision keeps provider data reusable while making status, progress, rating and dates private and user-specific. An ordered `ShelfBook` entity supports many shelves per library item. Journals use a one-to-one relationship and quotes use one-to-many because their lifecycles differ.

## Security model

Registration and login are public. Passwords are hashed with BCrypt and successful authentication returns a signed JWT. Protected requests pass through a JWT filter before reaching controllers.

The important ownership rule is that services derive the user from the authenticated principal. Requests never choose a user ID. Repositories and services query through that ownership boundary, so knowing another resource ID does not grant access. Tests exercise unauthorized access and cross-user behavior.

## Open Library integration

The backend owns external discovery through a `BookProvider` interface and an Open Library implementation. The client receives BookHaus DTOs rather than raw provider responses. Search itself does not persist data. A book is normalized only when the reader adds it, with reuse by provider ID, ISBN and a conservative title-year fallback.

This boundary makes failures consistent for the frontend and leaves room for another provider or a cache without changing page components.

## Derived data

Statistics and goal progress are calculated from library records rather than stored as separate mutable totals. A completed book contributes to annual progress only when its finish date belongs to that year. Unknown pages are not guessed and unrated books do not distort the average.

The tradeoff is extra query and calculation work. For this scale, correctness and a single source of truth matter more than precomputed aggregates. At higher volume, I would introduce query projections or cached summaries with explicit invalidation.

## Persistence and migrations

Flyway owns the schema and Hibernate runs in validation mode. This prevents the application from silently mutating production structure. Seven migrations build the domain in dependency order and finish with indexes for common lookup, filter and ordering paths.

An applied migration is immutable. Any later schema correction belongs in a new version so every environment has the same auditable history.

## Frontend architecture

Route-level pages compose shared components such as book covers, progress bars, status badges, stat cards, loading skeletons, empty states and confirmation dialogs. API modules provide a narrow HTTP boundary and normalize backend failures into one client error type. Authentication context coordinates the session token, current user and unauthorized handling.

The interface was designed around clear product states: loading, empty, populated, error and destructive confirmation. Lazy route loading keeps the initial bundle focused.

## Testing strategy

Backend tests focus on business rules and API boundaries: authentication, provider mapping, conflicts, ownership, progress validation, shelf ordering, journal upserts, quotes, annual goals and statistics. Frontend tests cover authentication state, protected routes, API error normalization, filters, progress rendering, dialogs and key redesigned pages.

The project uses a real PostgreSQL development database but tests isolate application behavior through Spring's test support and mocks where external network access would make results unreliable.

## Production readiness

The production profile disables SQL logging and limits error detail. Secrets and database credentials come from environment variables. The backend exposes a minimal health endpoint and accepts a platform-assigned port. The Vite build supports an explicit API origin and Vercel has an SPA rewrite.

The recommended deployment is Vercel, Render and managed PostgreSQL. I would add CI, OpenAPI contract checks, monitoring and automated end-to-end tests before treating it as a multi-user production service.

## Tradeoffs and limitations

- `sessionStorage` is simple and avoids long-lived browser persistence, but refresh-token rotation would provide a stronger long-running session model.
- Live Open Library search keeps the catalog fresh, but adds availability and latency risk. Caching and bounded retries would be the next operational improvement.
- Derived statistics avoid stale totals, but may need projections or caching as data volume grows.
- Yearly pages are intentionally conservative because the app does not yet record reading sessions.
- The application is private-first and does not include social feeds, recommendations or public profiles.

## Strong follow-up improvements

1. Add OpenAPI documentation and generated contract tests.
2. Add CI for backend tests, frontend lint and both production builds.
3. Add access and refresh token rotation plus account recovery.
4. Add reading-session history for pace and time-based analytics.
5. Add provider caching, telemetry and end-to-end browser coverage.

## Questions I would expect

**Why separate books from library books?**

Because title metadata is shared while progress, rating and status belong to one reader. The split avoids duplicated catalog data and makes ownership explicit.

**Why calculate statistics instead of storing them?**

Stored totals can drift from source records. Derivation keeps the model correct and is appropriate for the current scale.

**How do you prevent insecure direct object references?**

Services load resources through the authenticated user's ownership path. A raw resource ID alone is never sufficient.

**How would you change the system at scale?**

I would add cached provider results, database projections for dashboard queries, pagination, observability and background aggregation where measurements show it is needed.
