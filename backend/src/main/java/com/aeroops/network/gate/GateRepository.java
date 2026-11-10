package com.aeroops.network.gate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GateRepository extends JpaRepository<Gate, Long> {

    List<Gate> findAllByAirportIdOrderByCodeAsc(Long airportId);

    Optional<Gate> findByIdAndAirportId(Long id, Long airportId);

    boolean existsByAirportId(Long airportId);

    boolean existsByAirportIdAndCode(Long airportId, String code);

    boolean existsByAirportIdAndCodeAndIdNot(Long airportId, String code, Long id);
}
