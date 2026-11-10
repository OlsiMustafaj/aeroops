package com.aeroops.network.airport;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AirportRequest(
        @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$", message = "must be 3 letters")
        String iataCode,

        @NotBlank @Pattern(regexp = "^[A-Za-z]{4}$", message = "must be 4 letters")
        String icaoCode,

        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String country,

        @NotBlank @Size(max = 64) String timezone
) {
}
