package com.voxloud.provisioning.dto;

import com.voxloud.provisioning.entity.DeviceType;
import com.voxloud.provisioning.entity.FragmentType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class DeviceRequest {

    @NotBlank
    // MAC address regex (e.g. AA:BB:CC:DD:EE:FF)
    @Pattern(regexp = "([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})",
            message = "must be a valid MAC address")
    private String macAddress;

    @NotNull
    private DeviceType type;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private FragmentType overrideType;
    private String overrideContent;
}
