package com.voxloud.provisioning.service;

import com.voxloud.provisioning.dto.DeviceRequest;

public interface DeviceService {
    void createDevice(DeviceRequest dto);
    void deleteDevice(String mac);
}
