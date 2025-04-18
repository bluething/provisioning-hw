package com.voxloud.provisioning.exception;

public class OverrideTypeConflictException extends RuntimeException {
    public OverrideTypeConflictException(String existing, String requested) {
        super("Override already exists as " + existing + ", cannot replace with " + requested);
    }
}
