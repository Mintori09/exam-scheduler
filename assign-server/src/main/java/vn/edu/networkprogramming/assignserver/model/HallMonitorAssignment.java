package vn.edu.networkprogramming.assignserver.model;

public record HallMonitorAssignment(
        StaffRecord staff,
        String rangeText,
        int roomCount
) {
}
