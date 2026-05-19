package vn.edu.networkprogramming.clientweb.model;

public record StaffRecordView(
        int stt,
        String fullName,
        String birthDate,
        String staffCode,
        String department
) {
    public int getStt() { return stt; }
    public String getFullName() { return fullName; }
    public String getBirthDate() { return birthDate; }
    public String getStaffCode() { return staffCode; }
    public String getDepartment() { return department; }
}
