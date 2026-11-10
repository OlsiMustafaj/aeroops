package com.aeroops.network.airport;

public record AirportResponse(
        Long id,
        String iataCode,
        String icaoCode,
        String name,
        String city,
        String country,
        String timezone
) {
    static AirportResponse from(Airport a) {
        return new AirportResponse(a.getId(), a.getIataCode(), a.getIcaoCode(),
                a.getName(), a.getCity(), a.getCountry(), a.getTimezone());
    }
}
