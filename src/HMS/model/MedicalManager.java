package hms.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model entity defined by the HMS UML class diagram.
 */
public class MedicalManager extends User {

    private String managerId;
    private List<DoctorAssignment> doctorAssignments = new ArrayList<>();
    private List<ClinicalDepartment> clinicalDepartments = new ArrayList<>();
    private List<Doctor> managedDoctors = new ArrayList<>();

    public MedicalManager() {
        super();
    }

    public MedicalManager(String userId, String username, String password, String phone, String email, String role, String managerId) {
        super(userId, username, password, phone, email, role);
        this.managerId = managerId;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public List<DoctorAssignment> getDoctorAssignments() {
        return doctorAssignments;
    }

    public void setDoctorAssignments(List<DoctorAssignment> doctorAssignments) {
        this.doctorAssignments = doctorAssignments;
    }

    public List<ClinicalDepartment> getClinicalDepartments() {
        return clinicalDepartments;
    }

    public void setClinicalDepartments(List<ClinicalDepartment> clinicalDepartments) {
        this.clinicalDepartments = clinicalDepartments;
    }

    public List<Doctor> getManagedDoctors() {
        return managedDoctors;
    }

    public void setManagedDoctors(List<Doctor> managedDoctors) {
        this.managedDoctors = managedDoctors;
    }

    @Override
    public String getUserType() {
        return "MEDICAL_MANAGER";
    }

    public void addDoctor(Doctor doctor) {
        if (doctor != null && !managedDoctors.contains(doctor)) managedDoctors.add(doctor);
    }

    public void removeDoctor(Doctor doctor) {
        managedDoctors.remove(doctor);
    }
}
