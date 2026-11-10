package com.aeroops.network.gate;

public record GateResponse(
        Long id,
        Long airportId,
        String airportIata,
        String code,
        GateType type,
        SizeCategory sizeCategory
) {
    /** Must be called inside a transaction: reads the lazy airport association. */
    static GateResponse from(Gate g) {
        return new GateResponse(g.getId(), g.getAirport().getId(), g.getAirport().getIataCode(),
                g.getCode(), g.getType(), g.getSizeCategory());
    }
}
