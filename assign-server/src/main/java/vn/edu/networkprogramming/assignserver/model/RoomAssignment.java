package vn.edu.networkprogramming.assignserver.model;

public record RoomAssignment(
        RoomRecord room,
        StaffRecord invigilatorOne,
        StaffRecord invigilatorTwo
) {
}
