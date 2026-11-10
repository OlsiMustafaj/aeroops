package com.aeroops.network.gate;

import com.aeroops.network.airport.Airport;
import com.aeroops.network.airport.AirportRepository;
import com.aeroops.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GateRepositoryIT extends PostgresIntegrationTest {

    @Autowired
    GateRepository gates;

    @Autowired
    AirportRepository airports;

    Airport tia;
    Airport fco;

    @BeforeEach
    void setUp() {
        tia = airports.save(new Airport("TIA", "LATA", "Tirana", "Tirana", "Albania", "Europe/Tirane"));
        fco = airports.save(new Airport("FCO", "LIRF", "Fiumicino", "Rome", "Italy", "Europe/Rome"));
    }

    @Test
    void sameCodeAllowedAtDifferentAirports() {
        gates.saveAndFlush(new Gate(tia, "A1", GateType.CONTACT, SizeCategory.C));
        gates.saveAndFlush(new Gate(fco, "A1", GateType.CONTACT, SizeCategory.E));

        assertThat(gates.existsByAirportIdAndCode(tia.getId(), "A1")).isTrue();
        assertThat(gates.existsByAirportIdAndCode(fco.getId(), "A1")).isTrue();
    }

    @Test
    void databaseEnforcesUniqueCodePerAirport() {
        gates.saveAndFlush(new Gate(tia, "A1", GateType.CONTACT, SizeCategory.C));

        assertThatThrownBy(() -> gates.saveAndFlush(new Gate(tia, "A1", GateType.REMOTE, SizeCategory.C)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByIdAndAirportId_doesNotLeakAcrossAirports() {
        Gate g = gates.saveAndFlush(new Gate(tia, "B2", GateType.REMOTE, SizeCategory.C));

        assertThat(gates.findByIdAndAirportId(g.getId(), tia.getId())).isPresent();
        assertThat(gates.findByIdAndAirportId(g.getId(), fco.getId())).isEmpty();
    }

    @Test
    void listIsOrderedByCode() {
        gates.save(new Gate(tia, "B1", GateType.REMOTE, SizeCategory.C));
        gates.save(new Gate(tia, "A2", GateType.CONTACT, SizeCategory.C));
        gates.saveAndFlush(new Gate(tia, "A1", GateType.CONTACT, SizeCategory.D));

        assertThat(gates.findAllByAirportIdOrderByCodeAsc(tia.getId()))
                .extracting(Gate::getCode)
                .containsExactly("A1", "A2", "B1");
    }

    @Test
    void foreignKeyBlocksDeletingAirportWithGates() {
        gates.saveAndFlush(new Gate(tia, "A1", GateType.CONTACT, SizeCategory.C));

        assertThatThrownBy(() -> {
            airports.delete(tia);
            airports.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
