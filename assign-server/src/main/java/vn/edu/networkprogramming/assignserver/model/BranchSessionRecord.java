package vn.edu.networkprogramming.assignserver.model;

import java.time.Instant;
import java.util.List;

public record BranchSessionRecord(
        String branchId,
        int sessionNo,
        long selectionSeed,
        int requestedStaffCount,
        int requestedRoomCount,
        List<String> selectedStaffCodes,
        List<String> selectedRoomNames,
        SessionAssignment session,
        AssignmentSummary summary,
        Instant createdAt
) {
}
