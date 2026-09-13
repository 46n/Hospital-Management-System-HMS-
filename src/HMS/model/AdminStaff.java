package hms.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model entity defined by the HMS UML class diagram.
 */
public class AdminStaff extends User {

    private String staffId;
    private List<User> managedUsers = new ArrayList<>();
    private List<DoctorAssignment> createdAssignments = new ArrayList<>();

    public AdminStaff() {
        super();
    }

    public AdminStaff(String userId, String username, String password, String phone, String email, String role, String staffId) {
        super(userId, username, password, phone, email, role);
        this.staffId = staffId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public List<User> getManagedUsers() {
        return managedUsers;
    }

    public void setManagedUsers(List<User> managedUsers) {
        this.managedUsers = managedUsers;
    }

    public List<DoctorAssignment> getCreatedAssignments() {
        return createdAssignments;
    }

    public void setCreatedAssignments(List<DoctorAssignment> createdAssignments) {
        this.createdAssignments = createdAssignments;
    }

    @Override
    public String getUserType() {
        return "ADMIN_STAFF";
    }

    public void addManagedUser(User user) {
        if (user != null && !managedUsers.contains(user)) managedUsers.add(user);
    }
}
