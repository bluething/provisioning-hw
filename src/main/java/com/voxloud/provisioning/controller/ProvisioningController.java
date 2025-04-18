package com.voxloud.provisioning.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxloud.provisioning.service.ProvisioningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/provisioning")
@RequiredArgsConstructor
public class ProvisioningController {

    private final ProvisioningService service;
    private final ObjectMapper objectMapper;

    @GetMapping(
            path = "/{mac}",
            produces = {
                    MediaType.TEXT_PLAIN_VALUE,
                    MediaType.APPLICATION_JSON_VALUE
            }
    )
    public ResponseEntity<String> getConfig(@PathVariable String mac) throws IOException {
        String body = service.getProvisioningFile(mac);
        String json = objectMapper.writeValueAsString(body);
        String etag = "\"" + DigestUtils.md5DigestAsHex(json.getBytes(StandardCharsets.UTF_8)) + "\"";
        return ResponseEntity.ok()
                .eTag(etag)
                .body(body);
    }
}