package net.kaulics.datahandler.exception;

public class UnknownDeviceTypeException extends RuntimeException {

    public UnknownDeviceTypeException(String deviceType) {
        super("Unknown device type: " + deviceType);
    }
}
