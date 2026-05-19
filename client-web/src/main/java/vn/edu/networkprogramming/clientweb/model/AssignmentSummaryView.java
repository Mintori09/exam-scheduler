package vn.edu.networkprogramming.clientweb.model;

public record AssignmentSummaryView(
        int sessionNo,
        int roomAssignmentCount,
        int hallMonitorCount
) {
    public int getSessionNo() { return sessionNo; }
    public int getRoomAssignmentCount() { return roomAssignmentCount; }
    public int getHallMonitorCount() { return hallMonitorCount; }
}
