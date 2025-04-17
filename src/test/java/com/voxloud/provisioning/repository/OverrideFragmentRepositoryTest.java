package com.voxloud.provisioning.repository;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceType;
import com.voxloud.provisioning.entity.FragmentType;
import com.voxloud.provisioning.entity.OverrideFragment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OverrideFragmentRepositoryTest {
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private OverrideFragmentRepository overrideFragmentRepository;

    @Test
    @DisplayName("When saving an OverrideFragment, findByDevice_MacAddress should return it")
    void saveAndFindByDeviceMacAddress_shouldReturnFragment() {
        // given: a device
        Device device = Device.builder()
                .macAddress("11-22-33-44-55-66")
                .model(DeviceType.CONFERENCE)
                .username("userX")
                .password("passX")
                .build();
        deviceRepository.save(device);

        // and an override fragment for that device
        OverrideFragment fragment = OverrideFragment.builder()
                .device(device)
                .type(FragmentType.JSON)
                .content("{\"key\":\"value\"}")
                .build();
        overrideFragmentRepository.save(fragment);

        // when
        Optional<OverrideFragment> found =
                overrideFragmentRepository.findByDevice_MacAddress(device.getMacAddress());

        // then
        assertThat(found).isPresent()
                .get()
                .satisfies(f -> {
                    assertThat(f.getDevice().getMacAddress())
                            .isEqualTo(device.getMacAddress());
                    assertThat(f.getType()).isEqualTo(FragmentType.JSON);
                    assertThat(f.getContent()).contains("\"key\":\"value\"");
                });
    }

    @Test
    @DisplayName("findByDevice_MacAddress for non‑existent MAC returns empty")
    void findByDeviceMacAddress_whenNotExists_shouldReturnEmpty() {
        Optional<OverrideFragment> found =
                overrideFragmentRepository.findByDevice_MacAddress("ZZ-YY-XX-00-11-22");
        assertThat(found).isNotPresent();
    }
}