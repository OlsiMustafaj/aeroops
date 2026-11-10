package com.aeroops.network.airport;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AirportRepository extends JpaRepository<Airport, Long> {

    boolean existsByIataCode(String iataCode);

    boolean existsByIcaoCode(String icaoCode);

    boolean existsByIataCodeAndIdNot(String iataCode, Long id);

    boolean existsByIcaoCodeAndIdNot(String icaoCode, Long id);

    List<Airport> findAllByOrderByIataCodeAsc();
}
