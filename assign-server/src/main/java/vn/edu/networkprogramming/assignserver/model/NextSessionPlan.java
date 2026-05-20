package vn.edu.networkprogramming.assignserver.model;

import java.util.List;

public record NextSessionPlan(
        SessionAssignment session,
        long selectionSeed,
        List<String> selectedStaffCodes,
        List<String> selectedRoomNames
) {
}
