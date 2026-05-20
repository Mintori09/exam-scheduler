package vn.edu.networkprogramming.clientweb.model;

public record BranchPreviewView(
        boolean canCreateNextSession,
        int nextSessionNo,
        int staffDatasetSize,
        int roomDatasetSize,
        int usedPairCount,
        int constrainedStaffCount,
        String message
) {
    public boolean isCanCreateNextSession() { return canCreateNextSession; }
    public int getNextSessionNo() { return nextSessionNo; }
    public int getStaffDatasetSize() { return staffDatasetSize; }
    public int getRoomDatasetSize() { return roomDatasetSize; }
    public int getUsedPairCount() { return usedPairCount; }
    public int getConstrainedStaffCount() { return constrainedStaffCount; }
    public String getMessage() { return message; }
}
