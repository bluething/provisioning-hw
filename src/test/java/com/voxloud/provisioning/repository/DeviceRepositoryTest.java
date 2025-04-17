package com.voxloud.provisioning.repository;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DeviceRepositoryTest {
    @Autowired
    private DeviceRepository deviceRepository;

    @Test
    @DisplayName("When saving a Device, findById should return it")
    void saveAndFindById_shouldReturnSavedDevice() {
        // given
        Device device = Device.builder()
                .macAddress("AA-BB-CC-DD-EE-FF")
                .model(DeviceType.DESK)
                .username("testUser")
                .password("testPass")
                .build();

        // when
        deviceRepository.save(device);

        // then
        Optional<Device> found = deviceRepository.findById(device.getMacAddress());
        assertThat(found).isPresent()
                .get()
                .satisfies(d -> {
                    assertThat(d.getUsername()).isEqualTo("testUser");
                    assertThat(d.getPassword()).isEqualTo("testPass");
                    assertThat(d.getModel()).isEqualTo(DeviceType.DESK);
                });
    }

    @Test
    @DisplayName("findById for non‑existent MAC returns empty")
    void findById_whenNotExists_shouldReturnEmpty() {
        Optional<Device> found = deviceRepository.findById("00-00-00-00-00-00");
        assertThat(found).isNotPresent();
    }
}