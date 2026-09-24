package hms.model;

import java.util.Date;

/**
 * Model entity defined by the HMS UML class diagram.
 */
public class MedicalRequest {

    private String requestId;
    private String requestType;
    private Date requestDate;
    private String status;
    private Doctor doctor;
    private MedicalRecord medicalRecord;
    private String details;
    private String rejectionReason;
    private String processedBy;
    private Date processedDate;

    public MedicalRequest() {
    }

    public MedicalRequest(String requestId, String requestType, Date requestDate, String status) {
        this.requestId = requestId;
        this.requestType = requestType;
        this.requestDate = requestDate;
        this.status = status;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public MedicalRecord getMedicalRecord() {
        return medicalRecord;
    }

    public void setMedicalRecord(MedicalRecord medicalRecord) {
        this.medicalRecord = medicalRecord;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public Date getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(Date processedDate) {
        this.processedDate = processedDate;
    }

    public void markAllocated() { status = "ALLOCATED"; }
    public void markCompleted() { status = "COMPLETED"; }
    public void cancel() { status = "CANCELLED"; }
}
