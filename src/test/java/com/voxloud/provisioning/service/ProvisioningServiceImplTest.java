package com.voxloud.provisioning.service;

import com.voxloud.provisioning.config.ProvisioningProperties;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceType;
import com.voxloud.provisioning.entity.FragmentType;
import com.voxloud.provisioning.entity.OverrideFragment;
import com.voxloud.provisioning.exception.DeviceNotFoundException;
import com.voxloud.provisioning.formatter.ConfigFormatter;
import com.voxloud.provisioning.repository.DeviceRepository;
import com.voxloud.provisioning.repository.OverrideFragmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProvisioningServiceImplTest {
    @Mock
    private DeviceRepository deviceRepo;

    @Mock
    private OverrideFragmentRepository overrideRepo;

    @Mock
    private ProvisioningProperties props;

    @Mock
    private ConfigFormatter deskFormatter;

    @Mock
    private ConfigFormatter confFormatter;

    private ProvisioningServiceImpl service;

    @BeforeEach
    void setUp() {
        // deskFormatter handles DESK only
        when(deskFormatter.supports(DeviceType.DESK)).thenReturn(true);
        when(deskFormatter.supports(DeviceType.CONFERENCE)).thenReturn(false);

        // confFormatter handles CONFERENCE only
        when(confFormatter.supports(DeviceType.DESK)).thenReturn(false);
        when(confFormatter.supports(DeviceType.CONFERENCE)).thenReturn(true);

        // Build service with two formatters
        List<ConfigFormatter> formatters = Arrays.asList(deskFormatter, confFormatter);
        service = new ProvisioningServiceImpl(deviceRepo, overrideRepo, props, formatters);
    }

    @Test
    @DisplayName("getProvisioningFile() returns desk config when no override present")
    void provision_deskWithoutOverride_returnsConfig() throws IOException {
        String mac = "aa-bb-cc-dd-ee-ff";
        Device device = Device.builder()
                .macAddress(mac.toUpperCase())
                .model(DeviceType.DESK)
                .username("user1")
                .password("pass1")
                .build();

        // repository returns the device
        when(deviceRepo.findById(mac.toUpperCase())).thenReturn(Optional.of(device));
        // no override in DB
        when(overrideRepo.findByDevice_MacAddress(device.getMacAddress()))
                .thenReturn(Optional.empty());
        // formatter returns a dummy payload
        when(deskFormatter.format(device, Optional.empty(), props))
                .thenReturn("desk-config");

        // exercise
        String result = service.getProvisioningFile(mac);

        // verify
        assertThat(result).isEqualTo("desk-config");
        verify(deskFormatter).format(device, Optional.empty(), props);
        verify(confFormatter, never()).format(any(), any(), any());
    }

    @Test
    @DisplayName("getProvisioningFile() returns conference config when override present")
    void provision_conferenceWithOverride_returnsConfig() throws IOException {
        String mac = "11-22-33-44-55-66";
        Device device = Device.builder()
                .macAddress(mac.toUpperCase())
                .model(DeviceType.CONFERENCE)
                .username("confUser")
                .password("confPass")
                .build();

        OverrideFragment override = OverrideFragment.builder()
                .device(device)
                .type(FragmentType.JSON)
                .content("{\"timeout\":30}")
                .build();

        when(deviceRepo.findById(mac.toUpperCase())).thenReturn(Optional.of(device));
        when(overrideRepo.findByDevice_MacAddress(device.getMacAddress()))
                .thenReturn(Optional.of(override));
        when(confFormatter.format(device, Optional.of(override), props))
                .thenReturn("conf-config");

        String result = service.getProvisioningFile(mac);

        assertThat(result).isEqualTo("conf-config");
        verify(confFormatter).format(device, Optional.of(override), props);
        verify(deskFormatter, never()).format(any(), any(), any());
    }

    @Test
    @DisplayName("getProvisioningFile() throws when device not found")
    void provision_unknownMac_throwsException() {
        String mac = "00-00-00-00-00-00";
        when(deviceRepo.findById(mac.toUpperCase()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProvisioningFile(mac))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessageContaining(mac.toUpperCase());
    }

    @Test
    @DisplayName("getProvisioningFile() for desk with a properties override applies override")
    void provision_deskWithPropertiesOverride_appliesOverride() throws IOException {
        String mac = "AA-BB-CC-DD-EE-FF";
        Device device = Device.builder()
                .macAddress(mac)
                .model(DeviceType.DESK)
                .username("origUser")
                .password("origPass")
                .build();

        OverrideFragment override = OverrideFragment.builder()
                .device(device)
                .type(FragmentType.PROPERTIES)
                .content("username=overrideUser\nport=6001")
                .build();

        // stub repos
        when(deviceRepo.findById(mac)).thenReturn(Optional.of(device));
        when(overrideRepo.findByDevice_MacAddress(mac))
                .thenReturn(Optional.of(override));

        // stub formatter
        when(deskFormatter.format(device, Optional.of(override), props))
                .thenReturn("desk-config-with-override");

        // exercise
        String result = service.getProvisioningFile(mac);

        // verify result and interactions
        assertThat(result).isEqualTo("desk-config-with-override");
        verify(deskFormatter).format(device, Optional.of(override), props);
        verify(confFormatter, never()).format(any(), any(), any());
    }

    @Test
    @DisplayName("getProvisioningFile() for conference with a properties override still delegates to confFormatter")
    void provision_conferenceWithPropertiesOverride_delegatesOverride() throws IOException {
        String mac = "11-22-33-44-55-66";
        Device device = Device.builder()
                .macAddress(mac.toUpperCase())
                .model(DeviceType.CONFERENCE)
                .username("confUser")
                .password("confPass")
                .build();

        OverrideFragment override = OverrideFragment.builder()
                .device(device)
                .type(FragmentType.JSON)
                .content("{\n  \"domain\" : \"sip.anotherdomain.com\",\n  \"port\" : \"5161\",\n  \"timeout\" : 10 \n}")
                .build();

        when(deviceRepo.findById(mac.toUpperCase())).thenReturn(Optional.of(device));
        when(overrideRepo.findByDevice_MacAddress(mac.toUpperCase()))
                .thenReturn(Optional.of(override));
        when(confFormatter.format(device, Optional.of(override), props))
                .thenReturn("conf-config");

        String result = service.getProvisioningFile(mac);

        assertThat(result).isEqualTo("conf-config");
        verify(confFormatter).format(device, Optional.of(override), props);
        verify(deskFormatter, never()).format(any(), any(), any());
    }
}