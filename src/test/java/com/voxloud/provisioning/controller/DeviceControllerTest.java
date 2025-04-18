package com.voxloud.provisioning.controller;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceType;
import com.voxloud.provisioning.repository.DeviceRepository;
import com.voxloud.provisioning.repository.OverrideFragmentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class DeviceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviceRepository deviceRepo;

    private String macAddress;

    @BeforeEach
    void cleanDb() {
        macAddress = "7D:A8:01:0F:6F:1E";
    }

    @Test
    void createDevice_returns201AndLocation_andPersists() throws Exception {
        String payload = "{\n" +
                "  \"macAddress\": \"" + macAddress + "\",\n" +
                "  \"type\": \"DESK\",\n" +
                "  \"username\": \"john\",\n" +
                "  \"password\": \"doe\"\n" +
                "}";

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/provisioning/" + macAddress)));

        // verify it’s really in the DB
        assertThat(deviceRepo.findById(macAddress)).isPresent()
                .get()
                .satisfies(d -> {
                    assertThat(d.getModel()).isEqualTo(DeviceType.DESK);
                    assertThat(d.getUsername()).isEqualTo("john");
                });
    }

    @Test
    void createDevice_conflictIfExists_returns409() throws Exception {
        // seed a device
        deviceRepo.save(Device.builder()
                .macAddress(macAddress)
                .model(DeviceType.DESK)
                .username("u")
                .password("p")
                .build()
        );

        String payload = "{\n" +
                "  \"macAddress\": \"" + macAddress + "\",\n" +
                "  \"type\": \"DESK\",\n" +
                "  \"username\": \"x\",\n" +
                "  \"password\": \"y\"\n" +
                "}";

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void createDevice_unsupportedMediaType_returns415() throws Exception {
        String payload = "mac=aa&type=DESK";
        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(payload))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("DELETE /api/v1/devices/{mac} for existing device → 204 No Content and removed")
    void deleteExistingDevice_returns204AndRemoves() throws Exception {
        // 1) Seed a device
        deviceRepo.save(Device.builder()
                .macAddress(macAddress)
                .model(DeviceType.DESK)
                .username("alice")
                .password("secret")
                .build());

        // 2) Perform DELETE
        mockMvc.perform(delete("/api/v1/devices/{mac}", macAddress))
                .andExpect(status().isNoContent());

        // 3) Assert it’s gone
        assertThat(deviceRepo.findById(macAddress)).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/v1/devices/{mac} for missing device → 404 Not Found")
    void deleteNonExistingDevice_returns404() throws Exception {
        // no seeding

        mockMvc.perform(delete("/api/v1/devices/{mac}", macAddress))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("No device with MAC")));
    }
}