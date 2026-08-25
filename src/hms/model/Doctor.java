package hms.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model entity defined by the HMS UML class diagram.
 */
public class Doctor extends User {

    private String doctorId;
    private String specialty;
    private List<DoctorAssignment> doctorAssignments = new ArrayList<>();
    private MedicalManager shiftManager;
    private List<Appointment> appointments = new ArrayList<>();
    private List<MedicalRecord> medicalRecords = new ArrayList<>();
    private List<Prescription> prescriptions = new ArrayList<>();
    private List<MedicalRequest> medicalRequests = new ArrayList<>();
    private List<Feedback> feedbackEntries = new ArrayList<>();

    public Doctor() {
        super();
    }

    public Doctor(String userId, String username, String password, String phone, String email, String role, String doctorId, String specialty) {
        super(userId, username, password, phone, email, role);
        this.doctorId = doctorId;
        this.specialty = specialty;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public List<DoctorAssignment> getDoctorAssignments() {
        return doctorAssignments;
    }

    public void setDoctorAssignments(List<DoctorAssignment> doctorAssignments) {
        this.doctorAssignments = doctorAssignments;
    }

    public MedicalManager getShiftManager() {
        return shiftManager;
    }

    public void setShiftManager(MedicalManager shiftManager) {
        this.shiftManager = shiftManager;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public List<MedicalRecord> getMedicalRecords() {
        return medicalRecords;
    }

    public void setMedicalRecords(List<MedicalRecord> medicalRecords) {
        this.medicalRecords = medicalRecords;
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

    public List<Feedback> getFeedbackEntries() {
        return feedbackEntries;
    }

    public void setFeedbackEntries(List<Feedback> feedbackEntries) {
        this.feedbackEntries = feedbackEntries;
    }
}
