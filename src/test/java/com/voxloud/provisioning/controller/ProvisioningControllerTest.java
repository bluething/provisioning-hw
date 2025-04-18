package com.voxloud.provisioning.controller;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceType;
import com.voxloud.provisioning.entity.FragmentType;
import com.voxloud.provisioning.entity.OverrideFragment;
import com.voxloud.provisioning.repository.DeviceRepository;
import com.voxloud.provisioning.repository.OverrideFragmentRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class ProvisioningControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviceRepository deviceRepo;

    @Autowired
    private OverrideFragmentRepository overrideRepo;

    private Device deskDevice;
    private Device confDevice;

    @BeforeEach
    void setUp() {
        // clean slate
        overrideRepo.deleteAll();
        deviceRepo.deleteAll();

        // seed a DESK device
        deskDevice = Device.builder()
                .macAddress("AA:BB:CC:DD:EE:FF")
                .model(DeviceType.DESK)
                .username("deskUser")
                .password("deskPass")
                .build();

        // seed a CONFERENCE device
        confDevice = Device.builder()
                .macAddress("11:22:33:44:55:66")
                .model(DeviceType.CONFERENCE)
                .username("confUser")
                .password("confPass")
                .build();

        deviceRepo.saveAll(Arrays.asList(deskDevice, confDevice));

        // PROPERTIES override for DESK
        OverrideFragment propsOverride = OverrideFragment.builder()
                .device(deskDevice)
                .type(FragmentType.PROPERTIES)
                .content("domain=test.local\nport=6000")
                .build();

        // JSON override for CONFERENCE
        OverrideFragment jsonOverride = OverrideFragment.builder()
                .device(confDevice)
                .type(FragmentType.JSON)
                .content("{\"domain\":\"test.local\",\"port\":6000}")
                .build();

        overrideRepo.saveAll(Arrays.asList(propsOverride, jsonOverride));
    }

    @Test
    void whenGetDeskWithoutAccept_thenReturnsPlainTextWithOverrides() throws Exception {
        mockMvc.perform(get("/api/v1/provisioning/{mac}", deskDevice.getMacAddress()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(Matchers.containsString("domain=test.local")))
                .andExpect(content().string(Matchers.containsString("port=6000")))
                .andExpect(content().string(Matchers.containsString("username=deskUser")));
    }

    @Test
    void whenGetConferenceWithAcceptJson_thenReturnsJsonWithOverrides() throws Exception {
        mockMvc.perform(get("/api/v1/provisioning/{mac}", confDevice.getMacAddress())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.domain").value("test.local"))
                .andExpect(jsonPath("$.port").value(6000))
                .andExpect(jsonPath("$.username").value("confUser"));
    }

    @Test
    void whenGetUnknownMac_thenReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/provisioning/{mac}", "00:11:22:33:44:55"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDevice_withUnchangedEtag_returns304() throws Exception {
        MvcResult first = mockMvc.perform(get("/api/v1/provisioning/{mac}", deskDevice.getMacAddress()))
                .andExpect(header()
                        .exists(HttpHeaders.ETAG))
                .andReturn();
        String etag = first.getResponse().getHeader(HttpHeaders.ETAG);
        // second call with If-None-Match should yield 304
        mockMvc.perform(get("/api/v1/provisioning/{mac}", deskDevice.getMacAddress())
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified())
                .andExpect(content().string(""));
    }
}