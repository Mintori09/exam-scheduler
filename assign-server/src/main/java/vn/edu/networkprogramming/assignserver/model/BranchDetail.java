package vn.edu.networkprogramming.assignserver.model;

import java.util.List;

public record BranchDetail(
        ScheduleBranch branch,
        List<BranchSessionRecord> sessions,
        boolean invigilatorFileAvailable,
        boolean monitorFileAvailable
) {
}
