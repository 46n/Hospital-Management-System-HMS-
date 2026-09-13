package hms.model;

import java.util.Date;

/** Payment made against a patient's medical bill. */
public class Payment {
    private String paymentId;
    private String patientId;
    private String appointmentId;
    private double amount;
    private Date paymentDate;
    private String status;

    public Payment() {
        this.status = "PENDING";
    }

    public Payment(String paymentId, String patientId, String appointmentId,
            double amount, Date paymentDate) {
        this.paymentId = paymentId;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        setAmount(amount);
        this.paymentDate = paymentDate;
        this.status = "PENDING";
    }

    public void markPaid() { status = "PAID"; }
    public boolean isPaid() { return "PAID".equals(status); }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative"); this.amount = amount; }
    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
