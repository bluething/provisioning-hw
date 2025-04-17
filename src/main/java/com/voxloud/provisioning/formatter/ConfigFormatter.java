package com.voxloud.provisioning.formatter;

import com.voxloud.provisioning.config.ProvisioningProperties;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceType;
import com.voxloud.provisioning.entity.OverrideFragment;

import java.io.IOException;
import java.util.Optional;

public interface ConfigFormatter {
    boolean supports(DeviceType deviceType);
    String format(Device device, Optional<OverrideFragment> override, ProvisioningProperties props) throws IOException;
}
