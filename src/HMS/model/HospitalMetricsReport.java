package hms.model;

import java.util.ArrayList;
import java.util.List;

/** Report containing appointment status and performance metrics. */
public class HospitalMetricsReport implements HospitalReport {

    private int totalAppointments;
    private int completedAppointments;
    private int confirmedAppointments;
    private int cancelledAppointments;
    private int rescheduledAppointments;

    public HospitalMetricsReport() {
    }

    public HospitalMetricsReport(
            int totalAppointments,
            int completedAppointments,
            int confirmedAppointments,
            int cancelledAppointments,
            int rescheduledAppointments) {

        setMetrics(
                totalAppointments,
                completedAppointments,
                confirmedAppointments,
                cancelledAppointments,
                rescheduledAppointments
        );
    }

    public void setMetrics(
            int totalAppointments,
            int completedAppointments,
            int confirmedAppointments,
            int cancelledAppointments,
            int rescheduledAppointments) {

        if (totalAppointments < 0
                || completedAppointments < 0
                || confirmedAppointments < 0
                || cancelledAppointments < 0
                || rescheduledAppointments < 0) {

            throw new IllegalArgumentException(
                    "Report values cannot be negative"
            );
        }

        this.totalAppointments = totalAppointments;
        this.completedAppointments = completedAppointments;
        this.confirmedAppointments = confirmedAppointments;
        this.cancelledAppointments = cancelledAppointments;
        this.rescheduledAppointments = rescheduledAppointments;
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
        rows.add("Confirmed appointments: " + confirmedAppointments);
        rows.add("Cancelled appointments: " + cancelledAppointments);
        rows.add("Rescheduled appointments: " + rescheduledAppointments);

        double completionRate = 0.0;
        double cancellationRate = 0.0;

        if (totalAppointments > 0) {
            completionRate =
                    completedAppointments * 100.0 / totalAppointments;

            cancellationRate =
                    cancelledAppointments * 100.0 / totalAppointments;
        }

        rows.add("");

        rows.add(String.format(
                "Completion rate: %.1f%%",
                completionRate
        ));

        rows.add(String.format(
                "Cancellation rate: %.1f%%",
                cancellationRate
        ));

        return rows;
    }

    public int getTotalAppointments() {
        return totalAppointments;
    }

    public int getCompletedAppointments() {
        return completedAppointments;
    }

    public int getConfirmedAppointments() {
        return confirmedAppointments;
    }

    public int getCancelledAppointments() {
        return cancelledAppointments;
    }

    public int getRescheduledAppointments() {
        return rescheduledAppointments;
    }
}