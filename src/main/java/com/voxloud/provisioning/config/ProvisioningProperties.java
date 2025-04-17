package com.voxloud.provisioning.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "provisioning")
public class ProvisioningProperties {
    private String domain;
    private int port;
    private List<String> codecs;
}
