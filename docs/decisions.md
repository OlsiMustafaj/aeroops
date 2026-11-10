# Design decisions

Short log of choices made and why. Add an entry whenever a decision is non-obvious.

## 001 — Package by feature
Backend code is grouped by domain (`network`, `fleet`, `schedule`, `crew`, `ops`)
rather than by layer, so each rule lives next to the data it protects.

## 002 — UTC everywhere
All instants are stored as `TIMESTAMPTZ` and handled as `Instant` in Java.
Airport `timezone` (IANA id) is used only to render local times.

## 003 — Flyway owns the schema
`spring.jpa.hibernate.ddl-auto=validate`. Every schema change is a new migration.

## 004 — Errors as RFC 9457 problem details
`GlobalExceptionHandler` returns `ProblemDetail`; validation errors are listed under `errors`.
