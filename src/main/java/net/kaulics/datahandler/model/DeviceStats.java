package net.kaulics.datahandler.model;

public record DeviceStats(
        String deviceId,
        long count,
        long lateCount,
        long droppedCount,
        double min,
        double max,
        double average) {

    public static DeviceStats empty(String deviceId) {
        return new DeviceStats(deviceId, 0, 0, 0, Double.NaN, Double.NaN, Double.NaN);
    }
}
