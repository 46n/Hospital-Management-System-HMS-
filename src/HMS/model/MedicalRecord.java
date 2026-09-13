package hms.model;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Model entity defined by the HMS UML class diagram.
 */
public class MedicalRecord {

    private String recordId;
    private String vitalSigns;
    private String consultationNotes;
    private String diagnosis;
    private Date recordDate;
    private Patient patient;
    private Doctor doctor;
    private Appointment appointment;
    private List<Prescription> prescriptions = new ArrayList<>();
    private List<MedicalRequest> medicalRequests = new ArrayList<>();
    private List<AssessmentResult> assessmentResults = new ArrayList<>();

    public MedicalRecord() {
    }

    public MedicalRecord(String recordId, String vitalSigns, String consultationNotes, String diagnosis, Date recordDate) {
        this.recordId = recordId;
        this.vitalSigns = vitalSigns;
        this.consultationNotes = consultationNotes;
        this.diagnosis = diagnosis;
        this.recordDate = recordDate;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getVitalSigns() {
        return vitalSigns;
    }

    public void setVitalSigns(String vitalSigns) {
        this.vitalSigns = vitalSigns;
    }

    public String getConsultationNotes() {
        return consultationNotes;
    }

    public void setConsultationNotes(String consultationNotes) {
        this.consultationNotes = consultationNotes;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public Date getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(List<Prescription> prescriptions) {
        this.prescriptions = prescriptions;
    }

    public List<MedicalRequest> getMedicalRequests() {
        return medicalRequests;
    }

    public void setMedicalRequests(List<MedicalRequest> medicalRequests) {
        this.medicalRequests = medicalRequests;
    }

    public void addPrescription(Prescription prescription) {
        if (prescription != null && !prescriptions.contains(prescription)) prescriptions.add(prescription);
    }

    public void addRequest(MedicalRequest request) {
        if (request != null && !medicalRequests.contains(request)) medicalRequests.add(request);
    }

    public void finalizeRecord() {
        if (consultationNotes == null || consultationNotes.trim().isEmpty()) {
            throw new IllegalStateException("Consultation notes are required");
        }
        if (appointment != null) appointment.setStatus("COMPLETED");
    }

    public List<AssessmentResult> getAssessmentResults() { return assessmentResults; }
    public void setAssessmentResults(List<AssessmentResult> assessmentResults) { this.assessmentResults = assessmentResults; }
    public void addAssessmentResult(AssessmentResult result) {
        if (result != null && !assessmentResults.contains(result)) assessmentResults.add(result);
    }
}
