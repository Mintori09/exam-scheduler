package vn.edu.networkprogramming.clientweb.model;

import java.util.List;

public record SessionAssignmentView(
        int sessionNo,
        List<RoomAssignmentView> roomAssignments,
        List<HallMonitorAssignmentView> hallMonitorAssignments
) {
    public int getSessionNo() { return sessionNo; }
    public List<RoomAssignmentView> getRoomAssignments() { return roomAssignments; }
    public List<HallMonitorAssignmentView> getHallMonitorAssignments() { return hallMonitorAssignments; }
}
