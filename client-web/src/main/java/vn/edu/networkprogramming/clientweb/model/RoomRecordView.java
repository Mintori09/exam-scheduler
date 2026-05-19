package vn.edu.networkprogramming.clientweb.model;

public record RoomRecordView(
        int stt,
        String roomName,
        String location
) {
    public int getStt() { return stt; }
    public String getRoomName() { return roomName; }
    public String getLocation() { return location; }
}
