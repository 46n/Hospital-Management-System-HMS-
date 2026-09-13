package hms.model;

import java.util.List;

/** Common abstraction for reports shown by the hospital system. */
public interface HospitalReport {
    String getReportName();
    List<String> generateRows();
}
