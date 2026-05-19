package vn.edu.networkprogramming.clientweb.model;

public record HallMonitorAssignmentView(
        StaffRecordView staff,
        String rangeText,
        int roomCount
) {
    public StaffRecordView getStaff() { return staff; }
    public String getRangeText() { return rangeText; }
    public int getRoomCount() { return roomCount; }
}
