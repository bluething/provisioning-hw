package com.voxloud.provisioning.controller;

import com.voxloud.provisioning.dto.DeviceRequest;
import com.voxloud.provisioning.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
class DeviceController {

    private final DeviceService deviceService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> createDevice(
            @Valid @RequestBody DeviceRequest dto,
            UriComponentsBuilder uriBuilder) {

        deviceService.createDevice(dto);
        return ResponseEntity.created(
                uriBuilder
                        .path("/api/v1/provisioning/{mac}")
                        .buildAndExpand(dto.getMacAddress().toUpperCase())
                        .toUri()
        ).build();
    }

    @DeleteMapping("/{mac}")
    public ResponseEntity<Void> deleteDevice(@PathVariable String mac) {
        deviceService.deleteDevice(mac);
        return ResponseEntity.noContent().build();
    }
}
