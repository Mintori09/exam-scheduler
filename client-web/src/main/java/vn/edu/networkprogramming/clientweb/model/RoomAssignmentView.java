package vn.edu.networkprogramming.clientweb.model;

public record RoomAssignmentView(
        RoomRecordView room,
        StaffRecordView invigilatorOne,
        StaffRecordView invigilatorTwo
) {
    public RoomRecordView getRoom() { return room; }
    public StaffRecordView getInvigilatorOne() { return invigilatorOne; }
    public StaffRecordView getInvigilatorTwo() { return invigilatorTwo; }
}
