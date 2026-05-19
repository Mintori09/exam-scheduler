package vn.edu.networkprogramming.clientweb.model;

import java.util.List;

public record AssignmentDetailView(
        AssignmentRunView run,
        List<AssignmentSummaryView> sessionSummaries,
        boolean invigilatorFileAvailable,
        boolean monitorFileAvailable
) {
    public AssignmentRunView getRun() { return run; }
    public List<AssignmentSummaryView> getSessionSummaries() { return sessionSummaries; }
    public boolean isInvigilatorFileAvailable() { return invigilatorFileAvailable; }
    public boolean isMonitorFileAvailable() { return monitorFileAvailable; }
}
