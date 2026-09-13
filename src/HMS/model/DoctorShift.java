package hms.model;

import java.time.LocalTime;
import java.util.Date;

/** A single day's working shift for a doctor, scheduled by their Medical Manager. */
public class DoctorShift {

    private String shiftId;
    private Doctor doctor;
    private Date shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private MedicalManager createdBy;

    public DoctorShift() {
    }

    public DoctorShift(String shiftId, Date shiftDate, LocalTime startTime, LocalTime endTime) {
        this.shiftId = shiftId;
        this.shiftDate = shiftDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Date getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(Date shiftDate) {
        this.shiftDate = shiftDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public MedicalManager getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(MedicalManager createdBy) {
        this.createdBy = createdBy;
    }
}
