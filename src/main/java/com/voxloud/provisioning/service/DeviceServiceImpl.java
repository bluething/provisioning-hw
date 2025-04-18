package com.voxloud.provisioning.service;

import com.voxloud.provisioning.dto.DeviceRequest;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.OverrideFragment;
import com.voxloud.provisioning.exception.DeviceAlreadyExistsException;
import com.voxloud.provisioning.exception.DeviceNotFoundException;
import com.voxloud.provisioning.repository.DeviceRepository;
import com.voxloud.provisioning.repository.OverrideFragmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class DeviceServiceImpl implements DeviceService {
    private final DeviceRepository deviceRepo;
    private final OverrideFragmentRepository overrideRepo;

    @Override
    @Transactional
    public void createDevice(DeviceRequest dto) {
        String mac = dto.getMacAddress().toUpperCase();
        if (deviceRepo.existsById(mac)) {
            throw new DeviceAlreadyExistsException(mac);
        }

        Device device = Device.builder()
                .macAddress(mac)
                .model(dto.getType())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .build();
        device = deviceRepo.save(device);

        // only if both fields were supplied
        if (dto.getOverrideType() != null && dto.getOverrideContent() != null) {
            OverrideFragment frag = OverrideFragment.builder()
                    .device(device)
                    .type(dto.getOverrideType())
                    .content(dto.getOverrideContent())
                    .build();
            overrideRepo.save(frag);
        }
    }

    @Override
    @Transactional
    public void deleteDevice(String mac) {
        String normalized = mac.toUpperCase();
        Device device = deviceRepo.findById(normalized)
                .orElseThrow(() -> new DeviceNotFoundException(normalized));
        deviceRepo.delete(device);
    }
}
