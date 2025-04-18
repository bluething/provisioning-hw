package com.voxloud.provisioning.service;

import java.io.IOException;

public interface ProvisioningService {

    String getProvisioningFile(String macAddress) throws IOException;
}
