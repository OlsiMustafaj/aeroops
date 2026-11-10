# AeroOps — Flight Operations Management

AeroOps is a full-stack system for running a small airline's daily operations. It covers the flight schedule, the fleet, crew rostering, gate allocation, and delay handling. Most flight-management demos stop at basic create/read/update/delete. AeroOps is built around **real operational rules**: an aircraft can't be in two places at once, crew must be legally rested and qualified, gates can't be double-booked, and one delay ripples through the rest of the day.

> Status: early development. See the [Roadmap](#roadmap).

---

## Why this project

Airline operations are a domain where correctness matters and the rules interact with each other. One late inbound aircraft can break a turnaround, push a crew past their duty limit, and free up a gate that someone else needed. The goal of AeroOps is to model those rules explicitly, test them properly, and give dispatchers a clear view of what is about to go wrong.

## Tech stack

| Layer      | Technology |
|------------|------------|
| Backend    | Java 21, Spring Boot 3 (Web, Data JPA, Validation, Security), Hibernate |
| Database   | PostgreSQL 16, Flyway migrations |
| Frontend   | Angular (standalone components), Angular Material |
| Auth       | Spring Security + JWT, role-based access |
| Testing    | JUnit 5, Mockito, Testcontainers (Postgres), Angular unit tests |
| Tooling    | Docker Compose, Maven, GitHub Actions CI |
| API docs   | springdoc-openapi (Swagger UI) |

## Repository layout

```
aeroops/
├── backend/            Spring Boot application
│   └── src/main/java/.../aeroops/
│       ├── fleet/        aircraft, aircraft types, maintenance
│       ├── network/      airports, gates
│       ├── schedule/     flights, status lifecycle, delays
│       ├── crew/         crew members, qualifications, assignments, rest rules
│       ├── ops/          conflict detection, delay propagation, dashboard
│       ├── auth/         users, roles, JWT
│       └── common/       errors, auditing, shared config
├── frontend/           Angular application
├── docker-compose.yml  Postgres (+ backend/frontend later)
└── docs/               design notes, rule definitions, diagrams
```

The backend is organised **by domain feature, not by technical layer**. Each package owns its entities, repositories, services, controllers and DTOs.

## Domain model (core)

- **Airport**: IATA/ICAO code, name, timezone.
- **Gate**: belongs to an airport; has a type (contact / remote) and a size category.
- **AircraftType**: e.g. A320, B737; seat count, minimum cockpit and cabin crew, minimum turnaround time.
- **Aircraft**: registration, type, home base, status (ACTIVE, MAINTENANCE, RETIRED).
- **MaintenanceWindow**: a period when an aircraft is unavailable.
- **Flight**: flight number, origin, destination, scheduled and actual times (UTC), aircraft, gate, status.
- **DelayEvent**: a delay attached to a flight, with minutes, reason code and timestamp.
- **CrewMember**: name, role (CAPTAIN, FIRST_OFFICER, PURSER, CABIN), base, type qualifications.
- **CrewAssignment**: links a crew member to a flight in a given role.
- **User**: login with a role (ADMIN, DISPATCHER, CREW_SCHEDULER, VIEWER).

All times are stored in **UTC** and converted to the airport's local time only for display.

## Business rules

These are the heart of the project. Every rule should have its own service method and its own tests.

**Aircraft**
1. An aircraft cannot be assigned to two flights whose block times overlap.
2. Consecutive flights on the same aircraft need at least the type's minimum turnaround time between them.
3. **Continuity:** an aircraft's next flight must depart from the airport where its previous flight arrived.
4. An aircraft in a maintenance window, or with a non-ACTIVE status, cannot be scheduled.

**Crew** (simplified and inspired by EASA flight time limitations; *not* a compliant implementation)
5. Crew must be qualified on the aircraft type.
6. Each flight needs at least the type's minimum cockpit crew (one captain and one first officer) and minimum cabin crew.
7. Maximum duty period per day, with a configurable limit (default 13h).
8. Minimum rest between duty periods (default 10h).
9. Rolling flight-time limits (default 100h in 28 days), with configurable values.
10. A crew member cannot be on two overlapping flights.

**Gates**
11. A gate cannot hold two flights whose occupancy windows overlap. The configurable buffer defaults to 15 minutes.
12. A gate's size category must fit the aircraft type.

**Flight lifecycle**
13. Status moves through a defined state machine: `SCHEDULED → BOARDING → DEPARTED → ARRIVED`. Side branches are `DELAYED` and `CANCELLED`. Invalid transitions are rejected.
14. Recording a delay recalculates estimated times and **propagates** to the later flights of the same aircraft and crew. Any rule above that becomes broken is flagged as a conflict.

## Roadmap

### Phase 1: Foundation
- [ ] Repo skeleton: Spring Boot backend, Angular frontend, Postgres via Docker Compose
- [x] Flyway baseline migration, global error handling, OpenAPI docs
- [ ] CRUD for Airports, Gates, AircraftTypes, Aircraft (backend + Angular screens)
- [ ] CI pipeline: build and test backend and frontend on every push

### Phase 2: Scheduling
- [ ] Flights CRUD with validation (UTC times, origin ≠ destination)
- [ ] Aircraft rules 1–4 with tests
- [ ] Flight status state machine (rule 13)
- [ ] Schedule view: flights per day, filter by airport and aircraft

### Phase 3: Crew
- [ ] Crew members and type qualifications
- [ ] Crew assignment with rules 5, 6 and 10
- [ ] Duty, rest and flight-time limits (rules 7–9), with limits held in configuration
- [ ] Crew roster view: one crew member's duties across a week

### Phase 4: Gates and operations
- [ ] Gate allocation with rules 11–12
- [ ] Automatic gate suggestion for unassigned flights
- [ ] Delay recording and propagation (rule 14)
- [ ] Conflict detection service plus a "conflicts" panel on the dashboard

### Phase 5: Security and polish
- [ ] JWT authentication and role-based permissions
- [ ] Audit trail: who changed what, and when
- [ ] Operations dashboard: today's flights, delays, conflicts, aircraft utilisation
- [ ] Gantt-style timeline: aircraft rotations through the day

### Later ideas
- [ ] CSV import of a seasonal schedule
- [ ] Live updates on the dashboard via WebSocket
- [ ] Reports: on-time performance, delay causes, crew hours
- [ ] Containerised deployment of the full stack

## Getting started

**Prerequisites:** Java 21, Maven 3.9+, Docker. (Node 20+ once the frontend lands.)

```bash
# 1. start the database
docker compose up -d

# 2. run the backend (Flyway creates the schema on startup)
cd backend
mvn spring-boot:run

# 3. run all tests (unit + Testcontainers integration tests; needs Docker)
mvn verify
```

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API base: `http://localhost:8080/api`
- Frontend (coming in Phase 1): `http://localhost:4200`

Database settings come from `DB_URL`, `DB_USER` and `DB_PASSWORD`, defaulting to the Docker Compose values. Copy `.env.example` to `.env` to change them.

## Engineering conventions

- Small, focused commits in the Conventional Commits style (`feat:`, `fix:`, `test:`, `refactor:`).
- Each business rule gets unit tests. Repository queries get Testcontainers tests against real Postgres.
- Controllers stay thin. Rules live in services, and DTOs are kept separate from entities.
- Schema changes go through Flyway migrations only. Hibernate's `ddl-auto` is never used beyond `validate`.
- No secrets in the repo; configuration comes from environment variables.

## License

MIT
