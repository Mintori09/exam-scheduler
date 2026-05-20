package vn.edu.networkprogramming.assignserver.model;

import java.time.Instant;

public record ScheduleBranch(
        String branchId,
        String name,
        String status,
        String message,
        Instant createdAt,
        Instant updatedAt,
        String staffDatasetId,
        String staffDatasetName,
        String roomDatasetId,
        String roomDatasetName,
        int requestedStaffCount,
        int requestedRoomCount,
        int nextSessionNo,
        int sessionCreatedCount,
        boolean archived,
        String outputStatus,
        String outputError
) {
}
