package hms.manager;

import hms.model.Appointment;
import hms.model.AssessmentResult;
import hms.model.AssessmentType;
import hms.model.AssessmentUsageReport;
import hms.model.Doctor;
import hms.model.DoctorShift;
import hms.model.DoctorShiftReport;
import hms.model.HospitalMetricsReport;
import hms.model.HospitalReport;
import hms.model.MedicalRecord;
import hms.model.Payment;
import hms.model.RevenueReport;
import hms.model.User;

import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Uses the HospitalReport abstraction without depending on a concrete report
 * type, and builds the two concrete reports from the other managers' data.
 */
public class ReportManager {

    /** Polymorphic entry point: works the same regardless of which report is passed in. */
    public List<String> generate(HospitalReport report) {
        if (report == null) {
            throw new IllegalArgumentException("Report is required");
        }
        return report.generateRows();
    }

    public HospitalReport buildMetricsReport(
        AppointmentManager appointmentManager) {
    if (appointmentManager == null) {
        throw new IllegalArgumentException(
                "Appointment manager is required"
        );
    }
    int total = 0;
    int completed = 0;
    int confirmed = 0;
    int cancelled = 0;
    int rescheduled = 0;
    for (Appointment appointment
            : appointmentManager.getAppointments()) {
        total++;
        if ("COMPLETED".equals(appointment.getStatus())) {
            completed++;
        } else if ("CONFIRMED".equals(appointment.getStatus())) {
            confirmed++;
        } else if ("CANCELLED".equals(appointment.getStatus())) {
            cancelled++;
        } else if ("RESCHEDULED".equals(appointment.getStatus())) {
            rescheduled++;
        }
    }
    return new HospitalMetricsReport(  total, completed, confirmed, cancelled,rescheduled);
}

    public HospitalReport buildRevenueReport(BillingManager billingManager) {
        if (billingManager == null) {
            throw new IllegalArgumentException("Billing manager is required");
        }
        double billed = 0;
        double paid = 0;
        for (Payment payment : billingManager.getPayments()) {
            billed += payment.getAmount();
            if (payment.isPaid()) {
                paid += payment.getAmount();
            }
        }
        return new RevenueReport(billed, paid);
    }

    public HospitalReport buildAssessmentUsageReport(ClinicalManager clinicalManager,
            AssessmentTypeManager typeManager) {
        if (clinicalManager == null || typeManager == null) {
            throw new IllegalArgumentException("Clinical manager and assessment type manager are required");
        }
        Map<String, Integer> countById = new LinkedHashMap<>();
        Map<String, AssessmentType> typeById = new LinkedHashMap<>();
        for (AssessmentType type : typeManager.getTypes()) {
            countById.put(type.getAssessmentTypeId(), 0);
            typeById.put(type.getAssessmentTypeId(), type);
        }
        for (MedicalRecord record : clinicalManager.getRecords()) {
            for (AssessmentResult result : record.getAssessmentResults()) {
                if (result.getAssessmentType() != null) {
                    String id = result.getAssessmentType().getAssessmentTypeId();
                    countById.put(id, countById.getOrDefault(id, 0) + 1);
                }
            }
        }
        AssessmentUsageReport report = new AssessmentUsageReport();
        for (Map.Entry<String, Integer> entry : countById.entrySet()) {
            AssessmentType type = typeById.get(entry.getKey());
            report.addEntry(type.getName(), type.getCategory(), entry.getValue());
        }
        return report;
    }

    public HospitalReport buildDoctorShiftReport(DoctorShiftManager shiftManager, UserManager userManager) {
        if (shiftManager == null || userManager == null) {
            throw new IllegalArgumentException("Shift manager and user manager are required");
        }
        DoctorShiftReport report = new DoctorShiftReport();
        for (User user : userManager.getUsers()) {
            if (user instanceof Doctor) {
                Doctor doctor = (Doctor) user;
                List<DoctorShift> shifts = shiftManager.getShiftsForDoctor(doctor.getDoctorId());
                if (!shifts.isEmpty()) {
                    long totalMinutes = 0;
                    for (DoctorShift shift : shifts) {
                        long minutes = shift.getStartTime()
                                .until(shift.getEndTime(), ChronoUnit.MINUTES);
                        // Overnight shift, e.g. 19:30 -> 01:30
                        if (minutes < 0) {
                            minutes += 24 * 60;
                        }
                        totalMinutes += minutes;
                    }
                    report.addEntry(doctor.getUsername() + " (" + doctor.getDoctorId() + ")",
                            shifts.size(), totalMinutes / 60.0);
                }
            }
        }
        return report;
    }
}
