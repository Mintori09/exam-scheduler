package vn.edu.networkprogramming.assignserver.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import vn.edu.networkprogramming.assignserver.model.AssignmentInput;
import vn.edu.networkprogramming.assignserver.model.AssignmentResult;
import vn.edu.networkprogramming.assignserver.model.HallMonitorAssignment;
import vn.edu.networkprogramming.assignserver.model.RoomAssignment;
import vn.edu.networkprogramming.assignserver.model.RoomRecord;
import vn.edu.networkprogramming.assignserver.model.SessionAssignment;
import vn.edu.networkprogramming.assignserver.model.StaffRecord;

public class AssignmentPlanner {

    private static final int SESSION_BUILD_ATTEMPTS = 24;
    private static final int PAIRING_ATTEMPTS_PER_SESSION = 8;

    public AssignmentResult plan(AssignmentInput input) {
        return plan(input, null);
    }

    public AssignmentResult plan(AssignmentInput input, Consumer<SessionAssignment> onSessionCompleted) {
        int roomCount = input.roomRecords().size();
        int staffCount = input.staffRecords().size();
        if (staffCount < roomCount * 2) {
            return new AssignmentResult("FAILED", "So can bo phai it nhat bang 2 lan so phong thi", List.of());
        }

        List<StaffRecord> allStaff = new ArrayList<>(input.staffRecords());
        allStaff.sort(Comparator.comparing(StaffRecord::staffCode));

        Map<String, Set<String>> roomsSeenByStaff = new HashMap<>();
        Set<String> usedPairs = new HashSet<>();
        Map<String, Integer> invigilatorCount = new HashMap<>();
        Map<String, Integer> monitorCount = new HashMap<>();
        List<SessionAssignment> sessions = new ArrayList<>();
        int rotationOffset = 0;

        for (int sessionNo = 1; sessionNo <= input.sessionCount(); sessionNo++) {
            List<StaffRecord> sorted = new ArrayList<>(allStaff);
            sorted.sort(Comparator
                    .comparingInt((StaffRecord staff) -> invigilatorCount.getOrDefault(staff.staffCode(), 0))
                    .thenComparingInt(staff -> monitorCount.getOrDefault(staff.staffCode(), 0))
                    .thenComparing(StaffRecord::staffCode));

            SessionAssignment sessionAssignment = null;
            List<StaffRecord> hallMonitors = List.of();
            for (int attempt = 0; attempt < SESSION_BUILD_ATTEMPTS; attempt++) {
                List<StaffRecord> rotated = rotate(sorted, (rotationOffset + attempt) % sorted.size());
                List<StaffRecord> selectedInvigilators = new ArrayList<>(rotated.subList(0, roomCount * 2));
                hallMonitors = rotated.subList(roomCount * 2, rotated.size());

                sessionAssignment = buildSession(
                        sessionNo,
                        input.roomRecords(),
                        selectedInvigilators,
                        hallMonitors,
                        roomsSeenByStaff,
                        usedPairs,
                        sessionNo * 10007L + attempt * 389L
                );
                if (sessionAssignment != null) {
                    break;
                }
            }

            if (sessionAssignment == null) {
                return new AssignmentResult("FAILED", "Khong tim thay phan cong hop le", sessions);
            }

            sessions.add(sessionAssignment);
            if (onSessionCompleted != null) {
                onSessionCompleted.accept(sessionAssignment);
            }
            for (RoomAssignment roomAssignment : sessionAssignment.roomAssignments()) {
                increment(invigilatorCount, roomAssignment.invigilatorOne().staffCode());
                increment(invigilatorCount, roomAssignment.invigilatorTwo().staffCode());
                roomsSeenByStaff.computeIfAbsent(roomAssignment.invigilatorOne().staffCode(), ignored -> new HashSet<>())
                        .add(roomAssignment.room().roomName());
                roomsSeenByStaff.computeIfAbsent(roomAssignment.invigilatorTwo().staffCode(), ignored -> new HashSet<>())
                        .add(roomAssignment.room().roomName());
                usedPairs.add(canonicalPair(roomAssignment.invigilatorOne().staffCode(), roomAssignment.invigilatorTwo().staffCode()));
            }
            for (HallMonitorAssignment hallMonitorAssignment : sessionAssignment.hallMonitorAssignments()) {
                increment(monitorCount, hallMonitorAssignment.staff().staffCode());
            }
            rotationOffset++;
        }

        return new AssignmentResult("SUCCESS", "Phan cong thanh cong", sessions);
    }

    private SessionAssignment buildSession(
            int sessionNo,
            List<RoomRecord> rooms,
            List<StaffRecord> selectedInvigilators,
            List<StaffRecord> hallMonitors,
            Map<String, Set<String>> roomsSeenByStaff,
            Set<String> usedPairs,
            long seed
    ) {
        for (int attempt = 0; attempt < PAIRING_ATTEMPTS_PER_SESSION; attempt++) {
            Random random = new Random(seed + attempt * 9973L);
            List<StaffPair> pairs = buildPairs(selectedInvigilators, usedPairs, random);
            if (pairs == null) {
                continue;
            }
            List<RoomAssignment> assignments = assignRooms(rooms, pairs, roomsSeenByStaff, random);
            if (assignments != null) {
                return new SessionAssignment(sessionNo, assignments, buildHallMonitorAssignments(hallMonitors, rooms));
            }
        }
        return null;
    }

    private List<StaffPair> buildPairs(List<StaffRecord> selectedInvigilators, Set<String> usedPairs, Random random) {
        List<StaffRecord> unpaired = new ArrayList<>(selectedInvigilators);
        Collections.shuffle(unpaired, random);
        List<StaffPair> result = new ArrayList<>(selectedInvigilators.size() / 2);

        while (!unpaired.isEmpty()) {
            StaffRecord first = unpaired.remove(unpaired.size() - 1);
            int chosenIndex = -1;
            for (int i = unpaired.size() - 1; i >= 0; i--) {
                StaffRecord candidate = unpaired.get(i);
                if (!usedPairs.contains(canonicalPair(first.staffCode(), candidate.staffCode()))) {
                    chosenIndex = i;
                    break;
                }
            }
            if (chosenIndex < 0) {
                return null;
            }
            StaffRecord second = unpaired.remove(chosenIndex);
            result.add(new StaffPair(first, second));
        }
        return result;
    }

    private List<RoomAssignment> assignRooms(
            List<RoomRecord> rooms,
            List<StaffPair> pairs,
            Map<String, Set<String>> roomsSeenByStaff,
            Random random
    ) {
        int roomCount = rooms.size();
        List<int[]> candidatesByPair = new ArrayList<>(pairs.size());
        for (StaffPair pair : pairs) {
            int[] candidateRooms = new int[roomCount];
            int candidateCount = 0;
            for (int roomIndex = 0; roomIndex < roomCount; roomIndex++) {
                RoomRecord room = rooms.get(roomIndex);
                if (roomsSeenByStaff.getOrDefault(pair.left().staffCode(), Set.of()).contains(room.roomName())) {
                    continue;
                }
                if (roomsSeenByStaff.getOrDefault(pair.right().staffCode(), Set.of()).contains(room.roomName())) {
                    continue;
                }
                candidateRooms[candidateCount++] = roomIndex;
            }
            if (candidateCount == 0) {
                return null;
            }
            int[] packed = new int[candidateCount];
            System.arraycopy(candidateRooms, 0, packed, 0, candidateCount);
            shuffleIntArray(packed, random);
            candidatesByPair.add(packed);
        }

        List<Integer> pairOrder = new ArrayList<>(pairs.size());
        for (int i = 0; i < pairs.size(); i++) {
            pairOrder.add(i);
        }
        pairOrder.sort(Comparator.comparingInt(i -> candidatesByPair.get(i).length));

        int[] roomToPair = new int[roomCount];
        for (int i = 0; i < roomCount; i++) {
            roomToPair[i] = -1;
        }

        for (int pairIndex : pairOrder) {
            boolean[] visited = new boolean[roomCount];
            if (!tryMatch(pairIndex, candidatesByPair, roomToPair, visited)) {
                return null;
            }
        }

        List<RoomAssignment> assignments = new ArrayList<>(roomCount);
        for (int roomIndex = 0; roomIndex < roomCount; roomIndex++) {
            int pairIndex = roomToPair[roomIndex];
            if (pairIndex < 0) {
                return null;
            }
            StaffPair pair = pairs.get(pairIndex);
            assignments.add(new RoomAssignment(rooms.get(roomIndex), pair.left(), pair.right()));
        }
        return assignments;
    }

    private boolean tryMatch(
            int pairIndex,
            List<int[]> candidatesByPair,
            int[] roomToPair,
            boolean[] visited
    ) {
        for (int roomIndex : candidatesByPair.get(pairIndex)) {
            if (visited[roomIndex]) {
                continue;
            }
            visited[roomIndex] = true;
            if (roomToPair[roomIndex] == -1 || tryMatch(roomToPair[roomIndex], candidatesByPair, roomToPair, visited)) {
                roomToPair[roomIndex] = pairIndex;
                return true;
            }
        }
        return false;
    }

    private List<HallMonitorAssignment> buildHallMonitorAssignments(List<StaffRecord> hallMonitors, List<RoomRecord> rooms) {
        if (hallMonitors.isEmpty()) {
            return List.of();
        }
        int totalRooms = rooms.size();
        int baseSize = totalRooms / hallMonitors.size();
        int extra = totalRooms % hallMonitors.size();
        int cursor = 0;
        List<HallMonitorAssignment> assignments = new ArrayList<>();
        for (int i = 0; i < hallMonitors.size(); i++) {
            int segmentSize = baseSize + (i < extra ? 1 : 0);
            int start = cursor;
            int end = cursor + segmentSize - 1;
            cursor += segmentSize;
            String rangeText = segmentSize == 0
                    ? "Từ N/A đến N/A"
                    : "Từ " + rooms.get(start).roomName() + " đến " + rooms.get(end).roomName();
            assignments.add(new HallMonitorAssignment(hallMonitors.get(i), rangeText, segmentSize));
        }
        return assignments;
    }

    private List<StaffRecord> rotate(List<StaffRecord> sorted, int offset) {
        if (offset == 0 || sorted.isEmpty()) {
            return sorted;
        }
        List<StaffRecord> rotated = new ArrayList<>(sorted.size());
        rotated.addAll(sorted.subList(offset, sorted.size()));
        rotated.addAll(sorted.subList(0, offset));
        return rotated;
    }

    private void increment(Map<String, Integer> counts, String staffCode) {
        counts.put(staffCode, counts.getOrDefault(staffCode, 0) + 1);
    }

    private void shuffleIntArray(int[] values, Random random) {
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = values[i];
            values[i] = values[j];
            values[j] = tmp;
        }
    }

    private String canonicalPair(String left, String right) {
        return left.compareTo(right) < 0 ? left + "|" + right : right + "|" + left;
    }

    private record StaffPair(StaffRecord left, StaffRecord right) {
    }
}
