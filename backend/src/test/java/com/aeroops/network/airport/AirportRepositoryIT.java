package com.aeroops.network.airport;

import com.aeroops.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AirportRepositoryIT extends PostgresIntegrationTest {

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

    @Test
    void databaseRejectsLowercaseCodes() {
        assertThatThrownBy(() -> repository.saveAndFlush(
                new Airport("tia", "LATA", "A", "Tirana", "Albania", "Europe/Tirane")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
