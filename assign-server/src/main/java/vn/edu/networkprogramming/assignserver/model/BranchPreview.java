package vn.edu.networkprogramming.assignserver.model;

public record BranchPreview(
        boolean canCreateNextSession,
        int nextSessionNo,
        int staffDatasetSize,
        int roomDatasetSize,
        int usedPairCount,
        int constrainedStaffCount,
        String message
) {
}
