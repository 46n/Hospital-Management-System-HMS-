package hms.model;

import java.util.ArrayList;
import java.util.List;

/** Report showing how many assessment results have been recorded per type. */
public class AssessmentUsageReport implements HospitalReport {

    private final List<String> entries = new ArrayList<>();
    private int totalResults;

    public AssessmentUsageReport() {
    }

    public void addEntry(String typeName, String category, int count) {
        entries.add(typeName + " [" + category + "]: " + count + " result(s)");
        totalResults += count;
    }

    @Override
    public String getReportName() {
        return "Assessment Usage Report";
    }

    @Override
    public List<String> generateRows() {
        List<String> rows = new ArrayList<>();
        if (entries.isEmpty()) {
            rows.add("No assessment results recorded.");
            return rows;
        }
        rows.add("Total assessment results recorded: " + totalResults);
        rows.add("");
        rows.addAll(entries);
        return rows;
    }

    public int getTotalResults() {
        return totalResults;
    }
}
