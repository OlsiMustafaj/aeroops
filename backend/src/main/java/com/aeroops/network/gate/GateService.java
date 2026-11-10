package com.aeroops.network.gate;

import com.aeroops.common.error.ConflictException;
import com.aeroops.common.error.NotFoundException;
import com.aeroops.network.airport.Airport;
import com.aeroops.network.airport.AirportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class GateService {

    private final GateRepository gates;
    private final AirportRepository airports;

    public GateService(GateRepository gates, AirportRepository airports) {
        this.gates = gates;
        this.airports = airports;
    }

    public List<GateResponse> findAll(Long airportId) {
        requireAirport(airportId);
        return gates.findAllByAirportIdOrderByCodeAsc(airportId).stream()
                .map(GateResponse::from)
                .toList();
    }

    public GateResponse findById(Long airportId, Long gateId) {
        return GateResponse.from(getOrThrow(airportId, gateId));
    }

    @Transactional
    public GateResponse create(Long airportId, GateRequest req) {
        Airport airport = requireAirport(airportId);
        String code = normalize(req.code());

        if (gates.existsByAirportIdAndCode(airportId, code)) {
            throw duplicate(airport, code);
        }

        Gate saved = gates.save(new Gate(airport, code, req.type(), req.sizeCategory()));
        return GateResponse.from(saved);
    }

    @Transactional
    public GateResponse update(Long airportId, Long gateId, GateRequest req) {
        Gate gate = getOrThrow(airportId, gateId);
        String code = normalize(req.code());

        if (gates.existsByAirportIdAndCodeAndIdNot(airportId, code, gateId)) {
            throw duplicate(gate.getAirport(), code);
        }

        gate.update(code, req.type(), req.sizeCategory());
        return GateResponse.from(gate);
    }

    @Transactional
    public void delete(Long airportId, Long gateId) {
        // Later phases: refuse deletion while future flights are allocated to the gate.
        gates.delete(getOrThrow(airportId, gateId));
    }

    private Airport requireAirport(Long airportId) {
        return airports.findById(airportId)
                .orElseThrow(() -> new NotFoundException("Airport", airportId));
    }

    /** A gate is only found through its own airport, so /airports/1/gates/{id of a gate at airport 2} is a 404. */
    private Gate getOrThrow(Long airportId, Long gateId) {
        requireAirport(airportId);
        return gates.findByIdAndAirportId(gateId, airportId)
                .orElseThrow(() -> new NotFoundException("Gate", gateId));
    }

    private static ConflictException duplicate(Airport airport, String code) {
        return new ConflictException("Gate " + code + " already exists at " + airport.getIataCode());
    }

    private static String normalize(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
