package com.voxloud.provisioning.exception;

public class OverrideNotFoundException extends RuntimeException {
    public OverrideNotFoundException(String mac) {
        super("No override fragment for device " + mac);
    }
}
