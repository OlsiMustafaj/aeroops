package com.aeroops.network.airport;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "airport")
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iata_code", nullable = false, length = 3, columnDefinition = "char(3)")
    private String iataCode;

    @Column(name = "icao_code", nullable = false, length = 4, columnDefinition = "char(4)")
    private String icaoCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String country;

    /** IANA zone id, e.g. "Europe/Tirane". Used only for local-time display. */
    @Column(nullable = false, length = 64)
    private String timezone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Airport() {
        // for JPA
    }

    public Airport(String iataCode, String icaoCode, String name, String city,
                   String country, String timezone) {
        this.iataCode = iataCode;
        this.icaoCode = icaoCode;
        this.name = name;
        this.city = city;
        this.country = country;
        this.timezone = timezone;
    }

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void update(String iataCode, String icaoCode, String name, String city,
                       String country, String timezone) {
        this.iataCode = iataCode;
        this.icaoCode = icaoCode;
        this.name = name;
        this.city = city;
        this.country = country;
        this.timezone = timezone;
    }

    public Long getId() { return id; }
    public String getIataCode() { return iataCode; }
    public String getIcaoCode() { return icaoCode; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getTimezone() { return timezone; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
