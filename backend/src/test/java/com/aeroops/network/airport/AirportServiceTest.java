package com.aeroops.network.airport;

import com.aeroops.common.error.ConflictException;
import com.aeroops.common.error.NotFoundException;
import com.aeroops.network.gate.GateRepository;
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
class AirportServiceTest {

    @Mock
    AirportRepository repository;

    @Mock
    GateRepository gates;

    @InjectMocks
    AirportService service;

    private static AirportRequest request(String iata, String icao, String tz) {
        return new AirportRequest(iata, icao, " Tirana International ", "Tirana", "Albania", tz);
    }

    @Test
    void create_normalizesCodesAndTrimsName() {
        when(repository.save(any(Airport.class))).thenAnswer(inv -> inv.getArgument(0));

        AirportResponse res = service.create(request(" tia ", "lata", "Europe/Tirane"));

        ArgumentCaptor<Airport> captor = ArgumentCaptor.forClass(Airport.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getIataCode()).isEqualTo("TIA");
        assertThat(captor.getValue().getIcaoCode()).isEqualTo("LATA");
        assertThat(res.name()).isEqualTo("Tirana International");
    }

    @Test
    void create_rejectsDuplicateIata() {
        when(repository.existsByIataCode("TIA")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("TIA", "LATA", "Europe/Tirane")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("TIA");
        verify(repository, never()).save(any());
    }

    @Test
    void create_rejectsUnknownTimezone() {
        assertThatThrownBy(() -> service.create(request("TIA", "LATA", "Mars/Olympus")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mars/Olympus");
        verify(repository, never()).save(any());
    }

    @Test
    void update_allowsKeepingOwnCodes() {
        Airport existing = new Airport("TIA", "LATA", "Old", "Tirana", "Albania", "Europe/Tirane");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByIataCodeAndIdNot("TIA", 1L)).thenReturn(false);
        when(repository.existsByIcaoCodeAndIdNot("LATA", 1L)).thenReturn(false);

        AirportResponse res = service.update(1L, request("TIA", "LATA", "Europe/Tirane"));

        assertThat(res.name()).isEqualTo("Tirana International");
    }

    @Test
    void findById_throwsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_refusedWhileAirportHasGates() {
        Airport existing = new Airport("TIA", "LATA", "Tirana", "Tirana", "Albania", "Europe/Tirane");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(gates.existsByAirportId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("still has gates");
        verify(repository, never()).delete(any());
    }

    @Test
    void delete_removesAirportWithoutGates() {
        Airport existing = new Airport("TIA", "LATA", "Tirana", "Tirana", "Albania", "Europe/Tirane");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(gates.existsByAirportId(1L)).thenReturn(false);

        service.delete(1L);

        verify(repository).delete(existing);
    }
}
