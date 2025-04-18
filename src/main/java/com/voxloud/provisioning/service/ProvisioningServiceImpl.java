package com.voxloud.provisioning.service;

import com.voxloud.provisioning.config.ProvisioningProperties;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceType;
import com.voxloud.provisioning.entity.OverrideFragment;
import com.voxloud.provisioning.exception.DeviceNotFoundException;
import com.voxloud.provisioning.formatter.ConfigFormatter;
import com.voxloud.provisioning.repository.DeviceRepository;
import com.voxloud.provisioning.repository.OverrideFragmentRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class ProvisioningServiceImpl implements ProvisioningService {

    private final DeviceRepository deviceRepo;
    private final OverrideFragmentRepository overrideRepo;
    private final ProvisioningProperties props;
    private final Map<DeviceType, ConfigFormatter> formatterMap;

    public ProvisioningServiceImpl(DeviceRepository deviceRepo,
                                   OverrideFragmentRepository overrideRepo,
                                   ProvisioningProperties props,
                                   List<ConfigFormatter> formatters) {
        this.deviceRepo = deviceRepo;
        this.overrideRepo = overrideRepo;
        this.props = props;
        EnumMap<DeviceType, ConfigFormatter> map = new EnumMap<>(DeviceType.class);
        for (ConfigFormatter fmt : formatters) {
            for (DeviceType dt : DeviceType.values()) {
                if (fmt.supports(dt)) {
                    map.put(dt, fmt);
                }
            }
        }
        this.formatterMap = Collections.unmodifiableMap(map);
    }

    public String getProvisioningFile(String macAddress) throws IOException {
        Device device = deviceRepo.findById(macAddress.toUpperCase())
                .orElseThrow(() -> new DeviceNotFoundException(macAddress));

        Optional<OverrideFragment> override = overrideRepo.findByDevice_MacAddress(device.getMacAddress());

        ConfigFormatter formatter = formatterMap.get(device.getModel());
        return formatter.format(device, override, props);
    }
}
