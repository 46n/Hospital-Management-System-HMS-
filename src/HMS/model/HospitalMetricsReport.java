package hms.model;

import java.util.ArrayList;
import java.util.List;

/** Report containing basic appointment and consultation metrics. */
public class HospitalMetricsReport implements HospitalReport {
    private int totalAppointments;
    private int completedAppointments;
    private int cancelledAppointments;

    public HospitalMetricsReport() {
    }

    public HospitalMetricsReport(int totalAppointments, int completedAppointments,
            int cancelledAppointments) {
        setMetrics(totalAppointments, completedAppointments, cancelledAppointments);
    }

    public void setMetrics(int totalAppointments, int completedAppointments,
            int cancelledAppointments) {
        if (totalAppointments < 0 || completedAppointments < 0 || cancelledAppointments < 0) {
            throw new IllegalArgumentException("Report values cannot be negative");
        }
        this.totalAppointments = totalAppointments;
        this.completedAppointments = completedAppointments;
        this.cancelledAppointments = cancelledAppointments;
    }

    @Override
    public String getReportName() {
        return "Hospital Metrics Report";
    }

    @Override
    public List<String> generateRows() {
        List<String> rows = new ArrayList<>();
        rows.add("Total appointments: " + totalAppointments);
        rows.add("Completed appointments: " + completedAppointments);
        rows.add("Cancelled appointments: " + cancelledAppointments);
        return rows;
    }

    public int getTotalAppointments() { return totalAppointments; }
    public int getCompletedAppointments() { return completedAppointments; }
    public int getCancelledAppointments() { return cancelledAppointments; }
}
