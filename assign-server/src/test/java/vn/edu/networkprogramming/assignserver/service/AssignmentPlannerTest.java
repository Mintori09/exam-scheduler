package vn.edu.networkprogramming.assignserver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import vn.edu.networkprogramming.assignserver.model.AssignmentSummary;
import vn.edu.networkprogramming.assignserver.model.BranchSessionRecord;
import vn.edu.networkprogramming.assignserver.model.NextSessionPlan;
import vn.edu.networkprogramming.assignserver.model.RoomAssignment;
import vn.edu.networkprogramming.assignserver.model.RoomRecord;
import vn.edu.networkprogramming.assignserver.model.StaffRecord;

class AssignmentPlannerTest {

    private final AssignmentPlanner planner = new AssignmentPlanner();

    @Test
    void buildsNextSessionWithoutRepeatingRoomsOrPairs() {
        List<StaffRecord> staffPool = List.of(
                staff("GV01"), staff("GV02"), staff("GV03"), staff("GV04"),
                staff("GV05"), staff("GV06"), staff("GV07"), staff("GV08")
        );
        List<RoomRecord> roomPool = List.of(room(1, "P101"), room(2, "P102"), room(3, "P103"));

        NextSessionPlan first = planner.planNextSession(staffPool, roomPool, 8, 3, List.of(), 1234L);
        BranchSessionRecord firstRecord = new BranchSessionRecord(
                "B1",
                1,
                first.selectionSeed(),
                first.selectedStaffCodes(),
                first.selectedRoomNames(),
                first.session(),
                new AssignmentSummary(1, 3, 2),
                java.time.Instant.now()
        );
        NextSessionPlan second = planner.planNextSession(staffPool, roomPool, 8, 3, List.of(firstRecord), 6789L);

        Set<String> usedPairs = new HashSet<>();
        Set<String> usedStaffRoom = new HashSet<>();
        verify(first, usedPairs, usedStaffRoom);
        verify(second, usedPairs, usedStaffRoom);
        assertEquals(2, second.session().hallMonitorAssignments().size());
    }

    private void verify(NextSessionPlan plan, Set<String> usedPairs, Set<String> usedStaffRoom) {
        for (RoomAssignment assignment : plan.session().roomAssignments()) {
            assertFalse(assignment.invigilatorOne().staffCode().equals(assignment.invigilatorTwo().staffCode()));
            assertTrue(usedPairs.add(pairKey(assignment.invigilatorOne().staffCode(), assignment.invigilatorTwo().staffCode())));
            assertTrue(usedStaffRoom.add(assignment.invigilatorOne().staffCode() + "@" + assignment.room().roomName()));
            assertTrue(usedStaffRoom.add(assignment.invigilatorTwo().staffCode() + "@" + assignment.room().roomName()));
        }
    }

    private StaffRecord staff(String code) {
        return new StaffRecord(1, "Staff " + code, "01/01/1980", code, "Don vi");
    }

    private RoomRecord room(int stt, String roomName) {
        return new RoomRecord(stt, roomName, "Dia diem");
    }

    private String pairKey(String left, String right) {
        return left.compareTo(right) < 0 ? left + "|" + right : right + "|" + left;
    }
}
