CREATE TABLE airport (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    iata_code   CHAR(3)      NOT NULL,
    icao_code   CHAR(4)      NOT NULL,
    name        VARCHAR(150) NOT NULL,
    city        VARCHAR(100) NOT NULL,
    country     VARCHAR(100) NOT NULL,
    timezone    VARCHAR(64)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_airport_iata UNIQUE (iata_code),
    CONSTRAINT uq_airport_icao UNIQUE (icao_code)
);
