package hms.model;

import java.util.Date;

/**
 * Model entity defined by the HMS UML class diagram.
 */
public class DoctorAssignment {

    private String assignmentId;
    private Date assignedDate;
    private String status;
    private AdminStaff createdBy;
    private MedicalManager medicalManager;
    private Doctor doctor;

    public DoctorAssignment() {
    }

    public DoctorAssignment(String assignmentId, Date assignedDate, String status) {
        this.assignmentId = assignmentId;
        this.assignedDate = assignedDate;
        this.status = status;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Date getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(Date assignedDate) {
        this.assignedDate = assignedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AdminStaff getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(AdminStaff createdBy) {
        this.createdBy = createdBy;
    }

    public MedicalManager getMedicalManager() {
        return medicalManager;
    }

    public void setMedicalManager(MedicalManager medicalManager) {
        this.medicalManager = medicalManager;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }
}
