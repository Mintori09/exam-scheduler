package vn.edu.networkprogramming.assignserver.model;

import java.util.List;

public record AssignmentInput(
        List<StaffRecord> staffRecords,
        List<RoomRecord> roomRecords,
        int sessionCount
) {
}
