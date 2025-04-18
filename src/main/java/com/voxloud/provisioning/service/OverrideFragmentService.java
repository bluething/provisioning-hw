package com.voxloud.provisioning.service;

import com.voxloud.provisioning.dto.OverrideFragmentRequest;

public interface OverrideFragmentService {
    void addOrUpdate(String mac, OverrideFragmentRequest req);
    void delete(String mac);
}
