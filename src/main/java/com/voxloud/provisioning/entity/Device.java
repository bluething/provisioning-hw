package com.voxloud.provisioning.entity;

import javax.persistence.*;

import lombok.*;

@Entity
@Table(name = "device")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device extends Auditable {

    @Id
    @Column(name = "mac_address", nullable = false, length = 17)
    private String macAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType model;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;
}