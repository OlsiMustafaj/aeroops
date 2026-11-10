package com.aeroops.network.gate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record GateRequest(
        @NotBlank @Pattern(regexp = "^\\s*[A-Za-z0-9]{1,10}\\s*$", message = "must be 1-10 letters or digits")
        String code,

        @NotNull GateType type,

        @NotNull SizeCategory sizeCategory
) {
}
