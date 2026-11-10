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

## 005 — Gates are nested under airports
API is `/api/airports/{airportId}/gates`, and a gate is only looked up together with its
airport id, so a gate id from another airport returns 404. Gate codes are unique per airport.
Deleting an airport with gates returns 409; a `RESTRICT` foreign key backs this up in the DB.

## 006 — Gate size uses ICAO code letters (A–F)
A gate of category X accepts aircraft of category X or smaller (`SizeCategory.accommodates`).
This is the hook for rule 12 once aircraft types exist.
