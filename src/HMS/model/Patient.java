package hms.model;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Model entity defined by the HMS UML class diagram.
 */
public class Patient extends User {

    private String patientId;
    private Date dateOfBirth;
    private List<Appointment> appointments = new ArrayList<>();
    private List<MedicalRecord> medicalRecords = new ArrayList<>();
    private List<Prescription> prescriptions = new ArrayList<>();
    private List<Feedback> feedbackEntries = new ArrayList<>();

    public Patient() {
        super();
    }

    public Patient(String userId, String username, String password, String phone, String email, String role, String patientId, Date dateOfBirth) {
        super(userId, username, password, phone, email, role);
        this.patientId = patientId;
        this.dateOfBirth = dateOfBirth;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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

    public List<Feedback> getFeedbackEntries() {
        return feedbackEntries;
    }

    public void setFeedbackEntries(List<Feedback> feedbackEntries) {
        this.feedbackEntries = feedbackEntries;
    }

    @Override
    public String getUserType() {
        return "PATIENT";
    }

    public void addAppointment(Appointment appointment) {
        if (appointment != null && !appointments.contains(appointment)) appointments.add(appointment);
    }
}
