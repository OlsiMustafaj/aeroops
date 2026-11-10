package com.aeroops.network.airport;

import com.aeroops.common.error.ConflictException;
import com.aeroops.common.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class AirportService {

    private final AirportRepository repository;

    public AirportService(AirportRepository repository) {
        this.repository = repository;
    }

    public List<AirportResponse> findAll() {
        return repository.findAllByOrderByIataCodeAsc().stream()
                .map(AirportResponse::from)
                .toList();
    }

    public AirportResponse findById(Long id) {
        return AirportResponse.from(getOrThrow(id));
    }

    @Transactional
    public AirportResponse create(AirportRequest req) {
        String iata = normalize(req.iataCode());
        String icao = normalize(req.icaoCode());
        validateTimezone(req.timezone());

        if (repository.existsByIataCode(iata)) {
            throw new ConflictException("Airport with IATA code " + iata + " already exists");
        }
        if (repository.existsByIcaoCode(icao)) {
            throw new ConflictException("Airport with ICAO code " + icao + " already exists");
        }

        Airport saved = repository.save(new Airport(iata, icao, req.name().trim(),
                req.city().trim(), req.country().trim(), req.timezone()));
        return AirportResponse.from(saved);
    }

    @Transactional
    public AirportResponse update(Long id, AirportRequest req) {
        Airport airport = getOrThrow(id);
        String iata = normalize(req.iataCode());
        String icao = normalize(req.icaoCode());
        validateTimezone(req.timezone());

        if (repository.existsByIataCodeAndIdNot(iata, id)) {
            throw new ConflictException("Airport with IATA code " + iata + " already exists");
        }
        if (repository.existsByIcaoCodeAndIdNot(icao, id)) {
            throw new ConflictException("Airport with ICAO code " + icao + " already exists");
        }

        airport.update(iata, icao, req.name().trim(), req.city().trim(),
                req.country().trim(), req.timezone());
        return AirportResponse.from(airport);
    }

    @Transactional
    public void delete(Long id) {
        // Later phases: refuse deletion while gates or flights reference the airport.
        repository.delete(getOrThrow(id));
    }

    private Airport getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Airport", id));
    }

    private static String normalize(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private static void validateTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
        } catch (DateTimeException ex) {
            throw new IllegalArgumentException("Unknown timezone: " + timezone);
        }
    }
}
