package com.voxloud.provisioning.controller;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceType;
import com.voxloud.provisioning.entity.FragmentType;
import com.voxloud.provisioning.entity.OverrideFragment;
import com.voxloud.provisioning.repository.DeviceRepository;
import com.voxloud.provisioning.repository.OverrideFragmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class OverrideFragmentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviceRepository deviceRepo;

    @Autowired
    private OverrideFragmentRepository overrideRepo;

    private static final String BASE = "/api/v1/devices/{mac}/override-fragment";
    private Device testDevice;

    @BeforeEach
    void setUp() {
        testDevice = Device.builder()
                .macAddress("7D:A8:01:0F:6F:1E")
                .model(DeviceType.DESK)
                .username("john")
                .password("doe")
                .build();
        deviceRepo.save(testDevice);
    }

    @Test
    @DisplayName("POST create override when none exists → 201 Created")
    void addOverride_whenNoneExists_returns201AndPersists() throws Exception {
        String payload =
                "{\n" +
                        "  \"type\": \"PROPERTIES\",\n" +
                        "  \"content\": \"domain=test.local\\nport=6000\"\n" +
                        "}";

        mockMvc.perform(post(BASE, testDevice.getMacAddress())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        endsWith("/api/v1/provisioning/" + testDevice.getMacAddress())));

        Optional<OverrideFragment> opt =
                overrideRepo.findByDevice_MacAddress(testDevice.getMacAddress());

        // 1) assert it exists
        assertThat(opt).as("Override fragment should have been created")
                .isPresent();

        // 2) then inspect the actual fragment
        OverrideFragment f = opt.get();
        assertThat(f.getType()).isEqualTo(FragmentType.PROPERTIES);
        assertThat(f.getContent()).isEqualTo("domain=test.local\nport=6000");

    }

    @Test
    @DisplayName("POST update override when same type exists → 201 Created and updates")
    void addOverride_whenSameTypeExists_updatesContent() throws Exception {
        // seed existing override
        overrideRepo.save(
                OverrideFragment.builder()
                        .device(testDevice)
                        .type(FragmentType.PROPERTIES)
                        .content("domain=old.local\nport=5000")
                        .build()
        );

        String payload =
                "{\n" +
                        "  \"type\": \"PROPERTIES\",\n" +
                        "  \"content\": \"domain=new.local\\nport=7000\"\n" +
                        "}";

        mockMvc.perform(post(BASE, testDevice.getMacAddress())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        Optional<OverrideFragment> opt =
                overrideRepo.findByDevice_MacAddress(testDevice.getMacAddress());

        // 1) assert it exists
        assertThat(opt)
                .as("Override fragment should exist")
                .isPresent();

        // 2) inspect the fragment
        OverrideFragment f = opt.get();
        assertThat(f.getType()).isEqualTo(FragmentType.PROPERTIES);
        assertThat(f.getContent()).isEqualTo("domain=new.local\nport=7000");

    }

    @Test
    @DisplayName("POST conflict when different type exists → 409 Conflict")
    void addOverride_whenDifferentTypeExists_returns409() throws Exception {
        // seed PROPERTIES override
        overrideRepo.save(
                OverrideFragment.builder()
                        .device(testDevice)
                        .type(FragmentType.PROPERTIES)
                        .content("foo=bar")
                        .build()
        );

        String payload =
                "{\n" +
                        "  \"type\": \"JSON\",\n" +
                        "  \"content\": \"{\\\"foo\\\":\\\"bar\\\"}\"\n" +
                        "}";

        mockMvc.perform(post(BASE, testDevice.getMacAddress())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE existing override → 204 No Content and removes")
    void deleteOverride_whenExists_returns204AndRemoves() throws Exception {
        // seed override
        overrideRepo.save(
                OverrideFragment.builder()
                        .device(testDevice)
                        .type(FragmentType.JSON)
                        .content("{\"foo\":\"baz\"}")
                        .build()
        );

        mockMvc.perform(delete(BASE, testDevice.getMacAddress()))
                .andExpect(status().isNoContent());

        assertThat(overrideRepo.findByDevice_MacAddress(testDevice.getMacAddress()))
                .isEmpty();
    }

    @Test
    @DisplayName("DELETE non-existing override → 404 Not Found")
    void deleteOverride_whenNoneExists_returns404() throws Exception {
        overrideRepo.deleteAll();

        mockMvc.perform(delete(BASE, testDevice.getMacAddress()))
                .andExpect(status().isNotFound());
    }
}