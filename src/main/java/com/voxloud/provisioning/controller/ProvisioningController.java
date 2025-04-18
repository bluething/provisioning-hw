package com.voxloud.provisioning.controller;

import com.voxloud.provisioning.service.ProvisioningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/provisioning")
@RequiredArgsConstructor
public class ProvisioningController {

    private final ProvisioningService service;

    @GetMapping(
            path = "/{mac}",
            produces = {
                    MediaType.TEXT_PLAIN_VALUE,
                    MediaType.APPLICATION_JSON_VALUE
            }
    )
    public ResponseEntity<String> getConfig(@PathVariable String mac) throws IOException {
        String body = service.getProvisioningFile(mac);
        return ResponseEntity.ok()
                .body(body);
    }
}