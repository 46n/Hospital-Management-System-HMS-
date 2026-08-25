package hms.model;

/**
 * Model entity defined by the HMS UML class diagram.
 */
public class ClinicalDepartment {

    private String departmentId;
    private String departmentName;
    private String specialty;
    private MedicalManager medicalManager;

    public ClinicalDepartment() {
    }

    public ClinicalDepartment(String departmentId, String departmentName, String specialty) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.specialty = specialty;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public MedicalManager getMedicalManager() {
        return medicalManager;
    }

    public void setMedicalManager(MedicalManager medicalManager) {
        this.medicalManager = medicalManager;
    }
}
