package com.voxloud.provisioning.dto;

import com.voxloud.provisioning.entity.FragmentType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class OverrideFragmentRequest {
    @NotNull
    private FragmentType type;

    @NotBlank
    private String content;
}
