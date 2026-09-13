package hms.model;

import java.util.ArrayList;
import java.util.List;

/** Report containing billing and payment totals. */
public class RevenueReport implements HospitalReport {
    private double totalBilled;
    private double totalPaid;

    public RevenueReport() {
    }

    public RevenueReport(double totalBilled, double totalPaid) {
        setAmounts(totalBilled, totalPaid);
    }

    public void setAmounts(double totalBilled, double totalPaid) {
        if (totalBilled < 0 || totalPaid < 0) {
            throw new IllegalArgumentException("Revenue values cannot be negative");
        }
        this.totalBilled = totalBilled;
        this.totalPaid = totalPaid;
    }

    @Override
    public String getReportName() {
        return "Revenue Report";
    }

    @Override
    public List<String> generateRows() {
        List<String> rows = new ArrayList<>();
        rows.add("Total billed: " + totalBilled);
        rows.add("Total paid: " + totalPaid);
        rows.add("Outstanding: " + (totalBilled - totalPaid));
        return rows;
    }

    public double getTotalBilled() { return totalBilled; }
    public double getTotalPaid() { return totalPaid; }
}
