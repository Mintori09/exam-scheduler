package vn.edu.networkprogramming.assignserver.model;

import java.util.List;

public record SessionAssignment(
        int sessionNo,
        List<RoomAssignment> roomAssignments,
        List<HallMonitorAssignment> hallMonitorAssignments
) {
}
