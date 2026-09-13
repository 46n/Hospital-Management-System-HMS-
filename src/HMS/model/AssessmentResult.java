package hms.model;

import java.util.Date;

/** Result of a laboratory or clinical assessment. */
public class AssessmentResult {
    private String resultId;
    private String assessmentName;
    private String value;
    private String grade;
    private Date resultDate;
    private Doctor recordedBy;
    private MedicalRecord medicalRecord;
    private AssessmentType assessmentType;

    public AssessmentResult() { }

    public AssessmentResult(String resultId, String assessmentName, String value,
            String grade, Date resultDate) {
        this.resultId = resultId;
        this.assessmentName = assessmentName;
        this.value = value;
        this.grade = grade;
        this.resultDate = resultDate;
    }

    public void updateResult(String value, String grade) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Result value is required");
        this.value = value;
        this.grade = grade;
    }

    public String getResultId() { return resultId; }
    public void setResultId(String resultId) { this.resultId = resultId; }
    public String getAssessmentName() { return assessmentName; }
    public void setAssessmentName(String assessmentName) { this.assessmentName = assessmentName; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public Date getResultDate() { return resultDate; }
    public void setResultDate(Date resultDate) { this.resultDate = resultDate; }
    public Doctor getRecordedBy() { return recordedBy; }
    public void setRecordedBy(Doctor recordedBy) { this.recordedBy = recordedBy; }
    public MedicalRecord getMedicalRecord() { return medicalRecord; }
    public void setMedicalRecord(MedicalRecord medicalRecord) { this.medicalRecord = medicalRecord; }
    public AssessmentType getAssessmentType() { return assessmentType; }
    public void setAssessmentType(AssessmentType assessmentType) { this.assessmentType = assessmentType; }
}
