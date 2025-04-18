package com.voxloud.provisioning.controller;

import com.voxloud.provisioning.dto.OverrideFragmentRequest;
import com.voxloud.provisioning.service.OverrideFragmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/devices/{mac}/override-fragment")
@RequiredArgsConstructor
class OverrideFragmentController {
    private final OverrideFragmentService service;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> addOrUpdate(
            @PathVariable String mac,
            @Valid @RequestBody OverrideFragmentRequest req,
            UriComponentsBuilder uriBuilder) {

        service.addOrUpdate(mac, req);
        return ResponseEntity
                .created(uriBuilder
                        .path("/api/v1/provisioning/{mac}")
                        .buildAndExpand(mac.toUpperCase())
                        .toUri())
                .build();
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable String mac) {
        service.delete(mac);
        return ResponseEntity.noContent().build();
    }
}
