package com.aeroops.network.airport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Runs the real Flyway migration against Postgres in Docker. */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AirportRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    AirportRepository repository;

    @Test
    void savesAndFindsByIata() {
        repository.saveAndFlush(new Airport("TIA", "LATA", "Tirana International",
                "Tirana", "Albania", "Europe/Tirane"));

        assertThat(repository.existsByIataCode("TIA")).isTrue();
        assertThat(repository.findAllByOrderByIataCodeAsc()).hasSize(1);
    }

    @Test
    void databaseEnforcesUniqueIata() {
        repository.saveAndFlush(new Airport("TIA", "LATA", "A", "Tirana", "Albania", "Europe/Tirane"));

        assertThatThrownBy(() -> repository.saveAndFlush(
                new Airport("TIA", "LXXX", "B", "Tirana", "Albania", "Europe/Tirane")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
