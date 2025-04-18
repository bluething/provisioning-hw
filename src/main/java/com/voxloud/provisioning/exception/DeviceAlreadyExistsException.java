package com.voxloud.provisioning.exception;

public class DeviceAlreadyExistsException extends RuntimeException {
    public DeviceAlreadyExistsException(String mac) {
        super("Device with MAC " + mac + " already exists");
    }
}
