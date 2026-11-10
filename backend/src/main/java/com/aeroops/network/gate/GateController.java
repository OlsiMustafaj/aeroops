package com.aeroops.network.gate;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/airports/{airportId}/gates")
public class GateController {

    private final GateService service;

    public GateController(GateService service) {
        this.service = service;
    }

    @GetMapping
    public List<GateResponse> list(@PathVariable Long airportId) {
        return service.findAll(airportId);
    }

    @GetMapping("/{gateId}")
    public GateResponse get(@PathVariable Long airportId, @PathVariable Long gateId) {
        return service.findById(airportId, gateId);
    }

    @PostMapping
    public ResponseEntity<GateResponse> create(@PathVariable Long airportId,
                                               @Valid @RequestBody GateRequest request) {
        GateResponse created = service.create(airportId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{gateId}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{gateId}")
    public GateResponse update(@PathVariable Long airportId, @PathVariable Long gateId,
                               @Valid @RequestBody GateRequest request) {
        return service.update(airportId, gateId, request);
    }

    @DeleteMapping("/{gateId}")
    public ResponseEntity<Void> delete(@PathVariable Long airportId, @PathVariable Long gateId) {
        service.delete(airportId, gateId);
        return ResponseEntity.noContent().build();
    }
}
