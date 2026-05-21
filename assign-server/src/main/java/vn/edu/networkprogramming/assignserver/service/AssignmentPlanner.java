package vn.edu.networkprogramming.assignserver.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import vn.edu.networkprogramming.assignserver.model.BranchSessionRecord;
import vn.edu.networkprogramming.assignserver.model.HallMonitorAssignment;
import vn.edu.networkprogramming.assignserver.model.NextSessionPlan;
import vn.edu.networkprogramming.assignserver.model.RoomAssignment;
import vn.edu.networkprogramming.assignserver.model.RoomRecord;
import vn.edu.networkprogramming.assignserver.model.SessionAssignment;
import vn.edu.networkprogramming.assignserver.model.StaffRecord;
import vn.edu.networkprogramming.assignserver.service.exception.ValidationException;

public class AssignmentPlanner {

    private static final int SUBSET_ATTEMPTS = 64;
    private static final int SESSION_BUILD_ATTEMPTS = 24;
    private static final int PAIRING_ATTEMPTS_PER_SESSION = 8;
    private static final Logger LOGGER = Logger.getLogger(AssignmentPlanner.class.getName());

    public NextSessionPlan planNextSession(
            List<StaffRecord> staffPool,
            List<RoomRecord> roomPool,
            int requestedStaffCount,
            int requestedRoomCount,
            List<BranchSessionRecord> history,
            long seedBase
    ) {
        LOGGER.info(() -> "Planner bat dau planNextSession: requestedStaffCount=" + requestedStaffCount
                + ", requestedRoomCount=" + requestedRoomCount + ", historySize=" + history.size()
                + ", staffPoolSize=" + staffPool.size() + ", roomPoolSize=" + roomPool.size()
                + ", seedBase=" + seedBase);
        if (requestedStaffCount <= 0 || requestedRoomCount <= 0) {
            throw new ValidationException("Số cán bộ và số phòng phải là số nguyên dương");
        }
        if (requestedStaffCount > staffPool.size()) {
            throw new ValidationException("Số cán bộ vượt quá dữ liệu hiện có");
        }
        if (requestedRoomCount > roomPool.size()) {
            throw new ValidationException("Số phòng vượt quá dữ liệu hiện có");
        }
        if (requestedStaffCount <= requestedRoomCount * 2) {
            throw new ValidationException("Số cán bộ phải lớn hơn gấp đôi số phòng để có ít nhất 1 giám sát hành lang");
        }

        HistoryContext historyContext = buildHistory(history);
        int sessionNo = history.size() + 1;

        for (int subsetAttempt = 0; subsetAttempt < SUBSET_ATTEMPTS; subsetAttempt++) {
            long seed = seedBase + subsetAttempt * 10_007L;
            Random random = new Random(seed);
            List<StaffRecord> selectedStaff = pickStaffSubset(staffPool, requestedStaffCount, random);
            List<RoomRecord> selectedRooms = pickRoomSubset(roomPool, requestedRoomCount);
            SessionAssignment session = buildSession(
                    sessionNo,
                    selectedRooms,
                    selectedStaff,
                    historyContext,
                    seed
            );
            if (session != null) {
                int currentSubsetAttempt = subsetAttempt + 1;
                LOGGER.info(() -> "Planner tim thay loi giai: sessionNo=" + session.sessionNo()
                        + ", subsetAttempt=" + currentSubsetAttempt
                        + ", selectionSeed=" + seed
                        + ", roomAssignments=" + session.roomAssignments().size()
                        + ", hallMonitors=" + session.hallMonitorAssignments().size());
                return new NextSessionPlan(
                        session,
                        seed,
                        selectedStaff.stream().map(StaffRecord::staffCode).toList(),
                        selectedRooms.stream().map(RoomRecord::roomName).toList()
                );
            }
        }

        LOGGER.warning(() -> "Planner khong tim duoc loi giai cho sessionNo=" + sessionNo);
        throw new ValidationException("Không thể tạo thêm ca thi với ràng buộc hiện tại. Hãy làm lại nhánh hoặc chọn bộ dữ liệu khác");
    }

    public boolean canCreateNextSession(
            List<StaffRecord> staffPool,
            List<RoomRecord> roomPool,
            int requestedStaffCount,
            int requestedRoomCount,
            List<BranchSessionRecord> history,
            long seedBase
    ) {
        try {
            planNextSession(staffPool, roomPool, requestedStaffCount, requestedRoomCount, history, seedBase);
            return true;
        } catch (ValidationException exception) {
            return false;
        }
    }

    public int usedPairCount(List<BranchSessionRecord> history) {
        return buildHistory(history).usedPairs().size();
    }

    public int constrainedStaffCount(List<BranchSessionRecord> history) {
        return buildHistory(history).roomsSeenByStaff().size();
    }

    private SessionAssignment buildSession(
            int sessionNo,
            List<RoomRecord> rooms,
            List<StaffRecord> selectedStaff,
            HistoryContext historyContext,
            long seed
    ) {
        List<StaffRecord> sorted = new ArrayList<>(selectedStaff);
        sorted.sort(Comparator
                .comparingInt((StaffRecord staff) -> historyContext.invigilatorCount().getOrDefault(staff.staffCode(), 0))
                .thenComparingInt(staff -> historyContext.monitorCount().getOrDefault(staff.staffCode(), 0))
                .thenComparing(StaffRecord::staffCode));

        for (int attempt = 0; attempt < SESSION_BUILD_ATTEMPTS; attempt++) {
            List<StaffRecord> rotated = rotate(sorted, attempt % sorted.size());
            List<StaffRecord> invigilators = new ArrayList<>(rotated.subList(0, rooms.size() * 2));
            List<StaffRecord> hallMonitors = rotated.subList(rooms.size() * 2, rotated.size());
            for (int pairingAttempt = 0; pairingAttempt < PAIRING_ATTEMPTS_PER_SESSION; pairingAttempt++) {
                Random random = new Random(seed + attempt * 9973L + pairingAttempt * 389L);
                List<StaffPair> pairs = buildPairs(invigilators, historyContext.usedPairs(), random);
                if (pairs == null) {
                    continue;
                }
                List<RoomAssignment> roomAssignments = assignRooms(rooms, pairs, historyContext.roomsSeenByStaff(), random);
                if (roomAssignments != null) {
                    int currentAttempt = attempt + 1;
                    int currentPairingAttempt = pairingAttempt + 1;
                    LOGGER.info(() -> "Build session thanh cong: sessionNo=" + sessionNo
                            + ", sessionAttempt=" + currentAttempt
                            + ", pairingAttempt=" + currentPairingAttempt
                            + ", invigilators=" + invigilators.size()
                            + ", hallMonitors=" + hallMonitors.size());
                    return new SessionAssignment(sessionNo, roomAssignments, buildHallMonitorAssignments(hallMonitors, rooms));
                }
            }
        }
        return null;
    }

    private HistoryContext buildHistory(List<BranchSessionRecord> history) {
        Map<String, Set<String>> roomsSeenByStaff = new HashMap<>();
        Set<String> usedPairs = new HashSet<>();
        Map<String, Integer> invigilatorCount = new HashMap<>();
        Map<String, Integer> monitorCount = new HashMap<>();

        for (BranchSessionRecord record : history) {
            for (RoomAssignment roomAssignment : record.session().roomAssignments()) {
                increment(invigilatorCount, roomAssignment.invigilatorOne().staffCode());
                increment(invigilatorCount, roomAssignment.invigilatorTwo().staffCode());
                roomsSeenByStaff.computeIfAbsent(roomAssignment.invigilatorOne().staffCode(), ignored -> new HashSet<>())
                        .add(roomAssignment.room().roomName());
                roomsSeenByStaff.computeIfAbsent(roomAssignment.invigilatorTwo().staffCode(), ignored -> new HashSet<>())
                        .add(roomAssignment.room().roomName());
                usedPairs.add(canonicalPair(roomAssignment.invigilatorOne().staffCode(), roomAssignment.invigilatorTwo().staffCode()));
            }
            for (HallMonitorAssignment assignment : record.session().hallMonitorAssignments()) {
                increment(monitorCount, assignment.staff().staffCode());
            }
        }
        return new HistoryContext(roomsSeenByStaff, usedPairs, invigilatorCount, monitorCount);
    }

    private List<StaffRecord> pickStaffSubset(List<StaffRecord> source, int count, Random random) {
        List<StaffRecord> shuffled = new ArrayList<>(source);
        Collections.shuffle(shuffled, random);
        List<StaffRecord> result = new ArrayList<>(new LinkedHashSet<>(shuffled.subList(0, count)));
        result.sort(Comparator.comparing(StaffRecord::staffCode));
        return result;
    }

    private List<RoomRecord> pickRoomSubset(List<RoomRecord> source, int count) {
        List<RoomRecord> ordered = new ArrayList<>(source);
        ordered.sort(Comparator.comparingInt(RoomRecord::stt));
        return new ArrayList<>(ordered.subList(0, count));
    }

    private List<StaffPair> buildPairs(List<StaffRecord> invigilators, Set<String> usedPairs, Random random) {
        List<StaffRecord> unpaired = new ArrayList<>(invigilators);
        Collections.shuffle(unpaired, random);
        List<StaffPair> result = new ArrayList<>(invigilators.size() / 2);

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

        List<RoomAssignment> result = new ArrayList<>(roomCount);
        for (int roomIndex = 0; roomIndex < roomCount; roomIndex++) {
            int pairIndex = roomToPair[roomIndex];
            if (pairIndex < 0) {
                return null;
            }
            StaffPair pair = pairs.get(pairIndex);
            result.add(new RoomAssignment(rooms.get(roomIndex), pair.left(), pair.right()));
        }
        return result;
    }

    private boolean tryMatch(int pairIndex, List<int[]> candidatesByPair, int[] roomToPair, boolean[] visited) {
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

    private record HistoryContext(
            Map<String, Set<String>> roomsSeenByStaff,
            Set<String> usedPairs,
            Map<String, Integer> invigilatorCount,
            Map<String, Integer> monitorCount
    ) {
    }
}
