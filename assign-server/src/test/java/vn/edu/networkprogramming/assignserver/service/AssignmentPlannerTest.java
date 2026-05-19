package vn.edu.networkprogramming.assignserver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import vn.edu.networkprogramming.assignserver.model.AssignmentInput;
import vn.edu.networkprogramming.assignserver.model.AssignmentResult;
import vn.edu.networkprogramming.assignserver.model.HallMonitorAssignment;
import vn.edu.networkprogramming.assignserver.model.RoomAssignment;
import vn.edu.networkprogramming.assignserver.model.RoomRecord;
import vn.edu.networkprogramming.assignserver.model.StaffRecord;

class AssignmentPlannerTest {

    private final AssignmentPlanner planner = new AssignmentPlanner();

    @Test
    void buildsMultipleSessionsWithoutRepeatingRoomsOrPairs() {
        AssignmentInput input = new AssignmentInput(
                List.of(
                        staff("GV01", "A"),
                        staff("GV02", "B"),
                        staff("GV03", "C"),
                        staff("GV04", "D"),
                        staff("GV05", "E"),
                        staff("GV06", "F"),
                        staff("GV07", "G"),
                        staff("GV08", "H")
                ),
                List.of(
                        room(1, "P101"),
                        room(2, "P102"),
                        room(3, "P103")
                ),
                2
        );

        AssignmentResult result = planner.plan(input);

        assertEquals("SUCCESS", result.status());
        assertEquals(2, result.sessions().size());

        Map<String, Set<String>> roomsSeenByStaff = new HashMap<>();
        Set<String> usedPairs = new HashSet<>();

        for (var session : result.sessions()) {
            assertEquals(3, session.roomAssignments().size());
            for (RoomAssignment roomAssignment : session.roomAssignments()) {
                assertNotNull(roomAssignment.invigilatorOne());
                assertNotNull(roomAssignment.invigilatorTwo());
                assertFalse(roomAssignment.invigilatorOne().staffCode().equals(roomAssignment.invigilatorTwo().staffCode()));

                roomsSeenByStaff.computeIfAbsent(roomAssignment.invigilatorOne().staffCode(), ignored -> new HashSet<>());
                roomsSeenByStaff.computeIfAbsent(roomAssignment.invigilatorTwo().staffCode(), ignored -> new HashSet<>());
                assertTrue(roomsSeenByStaff.get(roomAssignment.invigilatorOne().staffCode()).add(roomAssignment.room().roomName()));
                assertTrue(roomsSeenByStaff.get(roomAssignment.invigilatorTwo().staffCode()).add(roomAssignment.room().roomName()));

                String pairKey = canonicalPair(roomAssignment.invigilatorOne().staffCode(), roomAssignment.invigilatorTwo().staffCode());
                assertTrue(usedPairs.add(pairKey));
            }

            assertEquals(2, session.hallMonitorAssignments().size());
            assertBalancedRanges(session.hallMonitorAssignments());
        }
    }

    @Test
    void failsWhenStaffCountIsTooSmall() {
        AssignmentInput input = new AssignmentInput(
                List.of(staff("GV01", "A"), staff("GV02", "B"), staff("GV03", "C")),
                List.of(room(1, "P101"), room(2, "P102")),
                1
        );

        AssignmentResult result = planner.plan(input);

        assertEquals("FAILED", result.status());
        assertEquals("So can bo phai it nhat bang 2 lan so phong thi", result.message());
        assertEquals(0, result.sessions().size());
    }

    private void assertBalancedRanges(List<HallMonitorAssignment> assignments) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (HallMonitorAssignment assignment : assignments) {
            int count = assignment.roomCount();
            min = Math.min(min, count);
            max = Math.max(max, count);
            assertTrue(assignment.rangeText().startsWith("Từ "));
        }
        assertTrue(max - min <= 1);
    }

    private StaffRecord staff(String code, String suffix) {
        return new StaffRecord(1, "Staff " + suffix, "01/01/1980", code, "Don vi");
    }

    private RoomRecord room(int stt, String roomName) {
        return new RoomRecord(stt, roomName, "Dia diem");
    }

    private String canonicalPair(String left, String right) {
        return left.compareTo(right) < 0 ? left + "|" + right : right + "|" + left;
    }
}
