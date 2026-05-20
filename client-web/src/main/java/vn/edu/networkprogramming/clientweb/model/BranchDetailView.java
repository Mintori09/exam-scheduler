package vn.edu.networkprogramming.clientweb.model;

import java.util.List;

public record BranchDetailView(
        ScheduleBranchView branch,
        List<BranchSessionRecordView> sessions,
        boolean invigilatorFileAvailable,
        boolean monitorFileAvailable
) {
    public ScheduleBranchView getBranch() { return branch; }
    public List<BranchSessionRecordView> getSessions() { return sessions; }
    public boolean isInvigilatorFileAvailable() { return invigilatorFileAvailable; }
    public boolean isMonitorFileAvailable() { return monitorFileAvailable; }
}
