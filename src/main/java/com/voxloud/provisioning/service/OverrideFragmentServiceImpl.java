package com.voxloud.provisioning.service;

import com.voxloud.provisioning.dto.OverrideFragmentRequest;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.OverrideFragment;
import com.voxloud.provisioning.exception.DeviceNotFoundException;
import com.voxloud.provisioning.exception.OverrideNotFoundException;
import com.voxloud.provisioning.exception.OverrideTypeConflictException;
import com.voxloud.provisioning.repository.DeviceRepository;
import com.voxloud.provisioning.repository.OverrideFragmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
class OverrideFragmentServiceImpl implements OverrideFragmentService {
    private final DeviceRepository deviceRepo;
    private final OverrideFragmentRepository overrideRepo;

    @Override
    @Transactional
    public void addOrUpdate(String mac, OverrideFragmentRequest req) {
        String upperCaseMac = mac.toUpperCase();
        Device device = deviceRepo.findById(upperCaseMac)
                .orElseThrow(() -> new DeviceNotFoundException(upperCaseMac));

        // see if one already exists
        Optional<OverrideFragment> opt = overrideRepo.findByDevice_MacAddress(upperCaseMac);
        if (opt.isPresent()) {
            OverrideFragment existing = opt.get();
            // conflict if types differ
            if (!existing.getType().equals(req.getType())) {
                throw new OverrideTypeConflictException(
                        existing.getType().name(), req.getType().name());
            }
            // same type → update content
            existing.setContent(req.getContent());
            overrideRepo.save(existing);

        } else {
            // none exists → create new fragment
            OverrideFragment frag = OverrideFragment.builder()
                    .device(device)
                    .type(req.getType())
                    .content(req.getContent())
                    .build();
            overrideRepo.save(frag);
        }

    }

    @Override
    public void delete(String mac) {
        String upperCaseMac = mac.toUpperCase();
        // throws if missing
        overrideRepo.findByDevice_MacAddress(upperCaseMac)
                .orElseThrow(() -> new OverrideNotFoundException(upperCaseMac));
        overrideRepo.deleteByDevice_MacAddress(upperCaseMac);
    }
}
