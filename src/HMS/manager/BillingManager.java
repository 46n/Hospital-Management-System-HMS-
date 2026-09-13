package hms.manager;

import hms.data.TextFileUtil;
import hms.model.Appointment;
import hms.model.Payment;
import hms.util.IdGenerator;
import hms.util.ValidationUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Coordinates payment recording, backed by payments.txt. */
public class BillingManager {

    private static final String DATA_FILE = "data/payments.txt";
    private static final String HEADER = "#paymentId|patientId|appointmentId|amount|paymentDate|status";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final List<Payment> payments = new ArrayList<>();
    private final AppointmentManager appointmentManager;
    private final File dataFile;

    public BillingManager(AppointmentManager appointmentManager) {
        this(appointmentManager, new File(DATA_FILE));
    }

    public BillingManager(AppointmentManager appointmentManager, File dataFile) {
        if (appointmentManager == null) {
            throw new IllegalArgumentException("Appointment manager is required");
        }
        this.appointmentManager = appointmentManager;
        this.dataFile = dataFile;
        load();
    }

    public Payment recordPayment(String patientId, String appointmentId, double amount) {
        ValidationUtil.requireNonBlank(patientId, "Patient");
        ValidationUtil.requireNonBlank(appointmentId, "Appointment");
        Appointment appointment = appointmentManager.findById(appointmentId);
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment not found: " + appointmentId);
        }
        if (appointment.getPatient() == null || !patientId.equals(appointment.getPatient().getPatientId())) {
            throw new IllegalArgumentException("Appointment does not belong to this patient");
        }
        if (isAlreadyPaid(appointmentId)) {
            throw new IllegalArgumentException("This appointment has already been paid");
        }
        Payment payment = new Payment(nextId(), patientId, appointmentId, amount, new Date());
        payment.markPaid();
        payments.add(payment);
        save();
        return payment;
    }

    public List<Payment> getPatientBills(String patientId) {
        List<Payment> result = new ArrayList<>();
        for (Payment payment : payments) {
            if (patientId.equals(payment.getPatientId())) {
                result.add(payment);
            }
        }
        return result;
    }

    public List<Payment> getPayments() {
        return new ArrayList<>(payments);
    }

    private boolean isAlreadyPaid(String appointmentId) {
        for (Payment payment : payments) {
            if (appointmentId.equals(payment.getAppointmentId()) && payment.isPaid()) {
                return true;
            }
        }
        return false;
    }

    private String nextId() {
        List<String> ids = new ArrayList<>();
        for (Payment payment : payments) {
            ids.add(payment.getPaymentId());
        }
        return IdGenerator.next("PM", ids);
    }

    private void load() {
        for (String line : TextFileUtil.readLines(dataFile)) {
            String[] fields = line.split("\\|", -1);
            if (fields.length < 6) {
                System.err.println("Skipping malformed payment record: " + line);
                continue;
            }
            try {
                Payment payment = new Payment(fields[0], fields[1], fields[2],
                        Double.parseDouble(fields[3]), DATE_FORMAT.parse(fields[4]));
                payment.setStatus(fields[5]);
                payments.add(payment);
            } catch (ParseException | NumberFormatException e) {
                System.err.println("Skipping payment record with invalid data: " + line);
            }
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (Payment payment : payments) {
            lines.add(String.join("|",
                    payment.getPaymentId(),
                    payment.getPatientId(),
                    payment.getAppointmentId(),
                    String.valueOf(payment.getAmount()),
                    DATE_FORMAT.format(payment.getPaymentDate()),
                    payment.getStatus()));
        }
        TextFileUtil.writeLines(dataFile, HEADER, lines);
    }
}
