package hms.model;

import java.util.Date;

/**
 * Model entity defined by the HMS UML class diagram.
 */
public class Feedback {

    private String feedbackId;
    private int rating;
    private String comment;
    private Date feedbackDate;
    private Patient patient;
    private Doctor doctor;
    private String appointmentId;

    public Feedback() {
    }

    public Feedback(String feedbackId, int rating, String comment, Date feedbackDate) {
        this.feedbackId = feedbackId;
        this.rating = rating;
        this.comment = comment;
        this.feedbackDate = feedbackDate;
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(String feedbackId) {
        this.feedbackId = feedbackId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating must be from 1 to 5");
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getFeedbackDate() {
        return feedbackDate;
    }

    public void setFeedbackDate(Date feedbackDate) {
        this.feedbackDate = feedbackDate;
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

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

}
