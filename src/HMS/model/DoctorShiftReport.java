package hms.model;

import java.util.ArrayList;
import java.util.List;

/** Report showing scheduled shift counts and total hours per doctor. */
public class DoctorShiftReport implements HospitalReport {

    private final List<String> entries = new ArrayList<>();
    private int totalShifts;

    public DoctorShiftReport() {
    }

    public void addEntry(String doctorLabel, int shiftCount, double totalHours) {
        entries.add(String.format("%s: %d shift(s), %.1f total hours", doctorLabel, shiftCount, totalHours));
        totalShifts += shiftCount;
    }

    @Override
    public String getReportName() {
        return "Doctor Shift Report";
    }

    @Override
    public List<String> generateRows() {
        List<String> rows = new ArrayList<>();
        if (entries.isEmpty()) {
            rows.add("No doctor shifts have been configured.");
            return rows;
        }
        rows.add("Total scheduled shifts: " + totalShifts);
        rows.add("");
        rows.addAll(entries);
        return rows;
    }

    public int getTotalShifts() {
        return totalShifts;
    }
}
