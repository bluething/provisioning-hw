package com.voxloud.provisioning.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "override_fragment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverrideFragment extends Auditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "device_mac", referencedColumnName = "mac_address", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FragmentType type;

    @Lob
    @Column(nullable = false)
    private String content;
}
