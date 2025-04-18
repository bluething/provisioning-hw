package com.voxloud.provisioning.exception;

public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(String macAddress) {
        super("No device with MAC " + macAddress);
    }
}
