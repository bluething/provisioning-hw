package com.voxloud.provisioning.repository;

import com.voxloud.provisioning.entity.OverrideFragment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface OverrideFragmentRepository extends JpaRepository<OverrideFragment, Long> {
    Optional<OverrideFragment> findByDevice_MacAddress(String macAddress);
    @Modifying
    @Transactional
    void deleteByDevice_MacAddress(String macAddress);
}
