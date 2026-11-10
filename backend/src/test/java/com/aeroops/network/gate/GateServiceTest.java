package com.aeroops.network.gate;

import com.aeroops.common.error.ConflictException;
import com.aeroops.common.error.NotFoundException;
import com.aeroops.network.airport.Airport;
import com.aeroops.network.airport.AirportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GateServiceTest {

    @Mock
    GateRepository gates;

    @Mock
    AirportRepository airports;

    @InjectMocks
    GateService service;

    Airport tia;

    @BeforeEach
    void setUp() {
        tia = new Airport("TIA", "LATA", "Tirana", "Tirana", "Albania", "Europe/Tirane");
    }

    @Test
    void create_normalizesCode() {
        when(airports.findById(1L)).thenReturn(Optional.of(tia));
        when(gates.save(any(Gate.class))).thenAnswer(inv -> inv.getArgument(0));

        GateResponse res = service.create(1L, new GateRequest(" a1 ", GateType.CONTACT, SizeCategory.C));

        ArgumentCaptor<Gate> captor = ArgumentCaptor.forClass(Gate.class);
        verify(gates).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("A1");
        assertThat(res.airportIata()).isEqualTo("TIA");
    }

    @Test
    void create_rejectsDuplicateCodeAtSameAirport() {
        when(airports.findById(1L)).thenReturn(Optional.of(tia));
        when(gates.existsByAirportIdAndCode(1L, "A1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(1L, new GateRequest("A1", GateType.CONTACT, SizeCategory.C)))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Gate A1 already exists at TIA");
        verify(gates, never()).save(any());
    }

    @Test
    void create_failsForUnknownAirport() {
        when(airports.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(99L, new GateRequest("A1", GateType.CONTACT, SizeCategory.C)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Airport");
    }

    @Test
    void findById_gateFromAnotherAirportIsNotFound() {
        when(airports.findById(1L)).thenReturn(Optional.of(tia));
        when(gates.findByIdAndAirportId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(1L, 5L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Gate");
    }

    @Test
    void update_allowsKeepingOwnCode() {
        Gate gate = new Gate(tia, "A1", GateType.CONTACT, SizeCategory.C);
        when(airports.findById(1L)).thenReturn(Optional.of(tia));
        when(gates.findByIdAndAirportId(5L, 1L)).thenReturn(Optional.of(gate));
        when(gates.existsByAirportIdAndCodeAndIdNot(1L, "A1", 5L)).thenReturn(false);

        GateResponse res = service.update(1L, 5L, new GateRequest("A1", GateType.REMOTE, SizeCategory.D));

        assertThat(res.type()).isEqualTo(GateType.REMOTE);
        assertThat(res.sizeCategory()).isEqualTo(SizeCategory.D);
    }
}
