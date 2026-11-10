package com.aeroops.network.gate;

import com.aeroops.network.airport.Airport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "gate")
public class Gate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "airport_id", nullable = false, updatable = false)
    private Airport airport;

    @Column(nullable = false, length = 10)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private GateType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "size_category", nullable = false, length = 1)
    private SizeCategory sizeCategory;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Gate() {
        // for JPA
    }

    public Gate(Airport airport, String code, GateType type, SizeCategory sizeCategory) {
        this.airport = airport;
        this.code = code;
        this.type = type;
        this.sizeCategory = sizeCategory;
    }

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void update(String code, GateType type, SizeCategory sizeCategory) {
        this.code = code;
        this.type = type;
        this.sizeCategory = sizeCategory;
    }

    public Long getId() { return id; }
    public Airport getAirport() { return airport; }
    public String getCode() { return code; }
    public GateType getType() { return type; }
    public SizeCategory getSizeCategory() { return sizeCategory; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
