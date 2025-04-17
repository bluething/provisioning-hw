package com.voxloud.provisioning.repository;

import com.voxloud.provisioning.entity.OverrideFragment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OverrideFragmentRepository extends JpaRepository<OverrideFragment, Long> {
    Optional<OverrideFragment> findByDevice_MacAddress(String macAddress);
}
