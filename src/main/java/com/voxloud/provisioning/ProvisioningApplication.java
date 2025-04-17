package com.voxloud.provisioning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ProvisioningApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProvisioningApplication.class, args);
    }

}