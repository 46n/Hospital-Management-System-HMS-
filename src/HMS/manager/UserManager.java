package hms.manager;

import hms.data.TextFileUtil;
import hms.model.AdminStaff;
import hms.model.Doctor;
import hms.model.MedicalManager;
import hms.model.Patient;
import hms.model.User;
import hms.util.IdGenerator;
import hms.util.ValidationUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Coordinates user registration, login, and profile operations, backed by users.txt. */
public class UserManager {

    private static final String DATA_FILE = "data/users.txt";
    private static final String HEADER =
            "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final List<String> VALID_SPECIALTIES = Arrays.asList(
            "Cardiology", "Neurology", "Pediatrics", "Orthopedics", "Dermatology",
            "General Medicine", "Radiology", "Psychiatry", "Oncology", "Emergency Medicine");

    private final List<User> users = new ArrayList<>();
    private final File dataFile;

    public UserManager() {
        this(new File(DATA_FILE));
    }

    public UserManager(File dataFile) {
        this.dataFile = dataFile;
        load();
    }

    public User registerUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }
        ValidationUtil.requireValidUsername(user.getUsername(), "Username");
        ValidationUtil.requireNonBlank(user.getPassword(), "Password");
        ValidationUtil.requireValidPhone(user.getPhone(), "Phone");
        ValidationUtil.requireValidEmail(user.getEmail(), "Email");
        if (user instanceof Doctor) {
            validateSpecialty(((Doctor) user).getSpecialty());
        }
        if (user instanceof Patient && ((Patient) user).getDateOfBirth() == null) {
            throw new IllegalArgumentException("Date of birth is required");
        }
        if (findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username '" + user.getUsername() + "' is already taken");
        }
        user.setUserId(IdGenerator.next(rolePrefix(user), collectIds()));
        assignRoleSpecificId(user);
        users.add(user);
        save();
        return user;
    }

    public User login(String username, String password) {
        ValidationUtil.requireNonBlank(username, "Username");
        ValidationUtil.requireNonBlank(password, "Password");
        User user = findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        if (!user.isActive()) {
            throw new IllegalArgumentException("This account has been deactivated");
        }
        return user;
    }

    public void updateProfile(String userId, String phone, String email) {
        User user = requireById(userId);
        ValidationUtil.requireValidPhone(phone, "Phone");
        ValidationUtil.requireValidEmail(email, "Email");
        user.updateContactDetails(phone, email);
        save();
    }

    /**
     * The complete Admin "Edit Profile" operation for a managed user, done as one atomic update:
     * username, phone, and email, plus the role-specific field (Doctor specialty or Patient date
     * of birth) when the user is that role. Every value is validated first, using the same rules
     * as registration - nothing is changed until every check has passed - and only then are all
     * applicable fields applied and saved together in a single save(). This is what stops a
     * failed Doctor specialty or Patient date of birth from leaving an already-changed username,
     * phone, or email in place. specialty and dateOfBirth are ignored unless the user is that
     * role, so callers editing an Admin Staff or Medical Manager may pass null for both. Role and
     * every generated ID (userId, staffId, managerId, doctorId, patientId) are never touched.
     */
    public void updateManagedUser(String userId, String username, String phone, String email,
            String specialty, Date dateOfBirth) {
        User user = requireById(userId);

        ValidationUtil.requireValidUsername(username, "Username");
        ValidationUtil.requireValidPhone(phone, "Phone");
        ValidationUtil.requireValidEmail(email, "Email");
        String trimmedUsername = username.trim();
        User sameUsername = findByUsername(trimmedUsername);
        if (sameUsername != null && !sameUsername.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Username '" + trimmedUsername + "' is already taken");
        }
        if (user instanceof Doctor) {
            validateSpecialty(specialty);
        }
        if (user instanceof Patient && dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth is required");
        }

        // Every value has now passed validation - apply and save them together, as one operation.
        user.setUsername(trimmedUsername);
        user.updateContactDetails(phone.trim(), email.trim());
        if (user instanceof Doctor) {
            ((Doctor) user).setSpecialty(specialty);
        }
        if (user instanceof Patient) {
            ((Patient) user).setDateOfBirth(dateOfBirth);
        }
        save();
    }

    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = requireById(userId);
        if (!user.getPassword().equals(currentPassword)) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        ValidationUtil.requireNonBlank(newPassword, "New password");
        user.setPassword(newPassword);
        save();
    }

    /** Admin-initiated reset: unlike changePassword(), does not require knowing the current password. */
    public void resetPassword(String userId, String newPassword) {
        User user = requireById(userId);
        ValidationUtil.requireNonBlank(newPassword, "New password");
        user.setPassword(newPassword);
        save();
    }

    /**
     * Deletes a user and keeps the Doctor/Medical Manager relationship consistent: a deleted
     * Doctor is removed from its assigned manager's list first, and a deleted Medical Manager
     * has every one of its assigned Doctors unassigned first - so no Doctor is ever left
     * pointing at a manager that no longer exists, and no manager is ever left listing a
     * Doctor that no longer exists.
     */
    public void deleteUser(String userId) {
        User user = requireById(userId);
        if (user instanceof Doctor) {
            Doctor doctor = (Doctor) user;
            MedicalManager manager = doctor.getShiftManager();
            if (manager != null) {
                manager.removeDoctor(doctor);
                doctor.setShiftManager(null);
            }
        } else if (user instanceof MedicalManager) {
            MedicalManager manager = (MedicalManager) user;
            List<Doctor> managedDoctors = new ArrayList<>(manager.getManagedDoctors());
            for (Doctor doctor : managedDoctors) {
                doctor.setShiftManager(null);
                manager.removeDoctor(doctor);
            }
        }
        users.remove(user);
        save();
    }

    public void deactivateUser(String userId) {
        User user = requireById(userId);
        user.setActive(false);
        save();
    }

    public void reactivateUser(String userId) {
        User user = requireById(userId);
        user.setActive(true);
        save();
    }

    /**
     * Assigns (or, with a null/blank managerId, unassigns) a doctor's Medical
     * Manager. Removes the doctor from any previous manager's list first, so
     * a doctor is never listed under two managers at once. Unassigning is
     * always allowed - even for an inactive Doctor, since cleaning up an
     * existing relationship must never be blocked - but a new assignment (a
     * non-blank managerId) requires both the Doctor and the Medical Manager
     * to be active. Deactivating an account never removes an existing
     * assignment by itself; this check only applies to a fresh assign call.
     */
    public void assignDoctorToManager(String doctorId, String managerId) {
        Doctor doctor = requireDoctor(doctorId);
        MedicalManager previousManager = doctor.getShiftManager();

        if (managerId == null || managerId.trim().isEmpty()) {
            if (previousManager != null) {
                previousManager.removeDoctor(doctor);
            }
            doctor.setShiftManager(null);
            save();
            return;
        }

        if (!doctor.isActive()) {
            throw new IllegalArgumentException("Cannot assign an inactive Doctor to a Medical Manager");
        }
        MedicalManager newManager = requireMedicalManager(managerId);
        if (!newManager.isActive()) {
            throw new IllegalArgumentException("Cannot assign a Doctor to an inactive Medical Manager");
        }
        if (previousManager != null && !previousManager.getManagerId().equals(newManager.getManagerId())) {
            previousManager.removeDoctor(doctor);
        }
        doctor.assignToManager(newManager);
        save();
    }

    public void updateDoctorSpecialty(String doctorId, String specialty) {
        Doctor doctor = requireDoctor(doctorId);
        validateSpecialty(specialty);
        doctor.setSpecialty(specialty);
        save();
    }

    public static List<String> getValidSpecialties() {
        return new ArrayList<>(VALID_SPECIALTIES);
    }

    private void validateSpecialty(String specialty) {
        ValidationUtil.requireNonBlank(specialty, "Specialty");
        if (!VALID_SPECIALTIES.contains(specialty)) {
            throw new IllegalArgumentException("Specialty must be one of " + VALID_SPECIALTIES);
        }
    }

    public MedicalManager findMedicalManagerById(String managerId) {
        User user = findById(managerId);
        return (user instanceof MedicalManager) ? (MedicalManager) user : null;
    }

    public User findById(String userId) {
        if (userId == null) {
            return null;
        }
        for (User user : users) {
            if (userId.equals(user.getUserId())) {
                return user;
            }
        }
        return null;
    }

    public User findByUsername(String username) {
        if (username == null) {
            return null;
        }
        for (User user : users) {
            if (user.getUsername() != null && user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    public Doctor findDoctorById(String doctorId) {
        User user = findById(doctorId);
        return (user instanceof Doctor) ? (Doctor) user : null;
    }

    public Patient findPatientById(String patientId) {
        User user = findById(patientId);
        return (user instanceof Patient) ? (Patient) user : null;
    }

    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    private User requireById(String userId) {
        User user = findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        return user;
    }

    private Doctor requireDoctor(String doctorId) {
        Doctor doctor = findDoctorById(doctorId);
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor not found: " + doctorId);
        }
        return doctor;
    }

    private MedicalManager requireMedicalManager(String managerId) {
        MedicalManager manager = findMedicalManagerById(managerId);
        if (manager == null) {
            throw new IllegalArgumentException("Medical Manager not found: " + managerId);
        }
        return manager;
    }

    private List<String> collectIds() {
        List<String> ids = new ArrayList<>();
        for (User user : users) {
            ids.add(user.getUserId());
        }
        return ids;
    }

    private String rolePrefix(User user) {
        if (user instanceof AdminStaff) {
            return "AS";
        }
        if (user instanceof MedicalManager) {
            return "MM";
        }
        if (user instanceof Doctor) {
            return "DR";
        }
        if (user instanceof Patient) {
            return "PT";
        }
        throw new IllegalArgumentException("Unknown user type");
    }

    private void assignRoleSpecificId(User user) {
        if (user instanceof AdminStaff) {
            ((AdminStaff) user).setStaffId(user.getUserId());
        } else if (user instanceof MedicalManager) {
            ((MedicalManager) user).setManagerId(user.getUserId());
        } else if (user instanceof Doctor) {
            ((Doctor) user).setDoctorId(user.getUserId());
        } else if (user instanceof Patient) {
            ((Patient) user).setPatientId(user.getUserId());
        }
    }

    private void load() {
        // Every ID and every lowercased username already loaded from this file, so a duplicate
        // row can be detected with a plain list search - the same style used elsewhere in the app.
        List<String> loadedIds = new ArrayList<>();
        List<String> loadedUsernamesLower = new ArrayList<>();
        Map<String, String> doctorManagerLinks = new HashMap<>();
        for (String line : TextFileUtil.readLines(dataFile)) {
            User user = parseLine(line, loadedIds, loadedUsernamesLower);
            if (user == null) {
                continue;
            }
            users.add(user);
            loadedIds.add(user.getUserId());
            loadedUsernamesLower.add(user.getUsername().toLowerCase());
            if (user instanceof Doctor) {
                String[] fields = line.split("\\|", -1);
                if (fields.length >= 10 && !fields[9].trim().isEmpty()) {
                    doctorManagerLinks.put(user.getUserId(), fields[9].trim());
                }
            }
        }
        // Resolved in a second pass since the manager's row may appear
        // anywhere in the file relative to the doctor's row.
        for (Map.Entry<String, String> link : doctorManagerLinks.entrySet()) {
            Doctor doctor = findDoctorById(link.getKey());
            MedicalManager manager = findMedicalManagerById(link.getValue());
            if (doctor != null && manager != null) {
                doctor.assignToManager(manager);
            } else {
                System.err.println("Skipping unresolved doctor-manager link for doctor: " + link.getKey());
            }
        }
    }

    private User parseLine(String line, List<String> loadedIds, List<String> loadedUsernamesLower) {
        String[] fields = line.split("\\|", -1);
        if (fields.length < 8 || fields.length > 10) {
            System.err.println("Skipping malformed user record: " + line);
            return null;
        }
        String userId = fields[0].trim();
        String role = fields[1].trim();
        String username = fields[2].trim();
        String password = fields[3].trim();
        String phone = fields[4].trim();
        String email = fields[5].trim();
        String extra1 = fields[6].trim();
        String extra2 = fields[7].trim();

        if (userId.isEmpty()) {
            System.err.println("Skipping user record with a blank user ID: " + line);
            return null;
        }
        if (loadedIds.contains(userId)) {
            System.err.println("Skipping duplicate user ID: " + line);
            return null;
        }
        if (role.isEmpty()) {
            System.err.println("Skipping user record with a blank role: " + line);
            return null;
        }
        if (username.isEmpty()) {
            System.err.println("Skipping user record with a blank username: " + line);
            return null;
        }
        if (loadedUsernamesLower.contains(username.toLowerCase())) {
            System.err.println("Skipping duplicate username: " + line);
            return null;
        }
        if (password.isEmpty()) {
            System.err.println("Skipping user record with a blank password: " + line);
            return null;
        }
        if (phone.isEmpty()) {
            System.err.println("Skipping user record with a blank phone: " + line);
            return null;
        }
        if (email.isEmpty()) {
            System.err.println("Skipping user record with a blank email: " + line);
            return null;
        }

        // Only an 8-field legacy row (no active column at all) defaults to active. Once a ninth
        // field is present, it must be exactly true or false - a blank value there is malformed,
        // not "unset", and must be skipped like any other invalid active value.
        boolean active = true;
        if (fields.length >= 9) {
            String activeText = fields[8].trim();
            if (!activeText.equalsIgnoreCase("true") && !activeText.equalsIgnoreCase("false")) {
                System.err.println("Skipping user record with an invalid active value \"" + activeText + "\": " + line);
                return null;
            }
            active = Boolean.parseBoolean(activeText);
        }

        User user;
        try {
            switch (role) {
                case "ADMIN_STAFF":
                    user = new AdminStaff(userId, username, password, phone, email, role, extra1);
                    break;
                case "MEDICAL_MANAGER":
                    user = new MedicalManager(userId, username, password, phone, email, role, extra1);
                    break;
                case "DOCTOR":
                    if (!VALID_SPECIALTIES.contains(extra2)) {
                        System.err.println("Skipping doctor record with an invalid specialty \"" + extra2 + "\": " + line);
                        return null;
                    }
                    user = new Doctor(userId, username, password, phone, email, role, extra1, extra2);
                    break;
                case "PATIENT":
                    if (extra2.isEmpty()) {
                        System.err.println("Skipping patient record with a blank date of birth: " + line);
                        return null;
                    }
                    Date dob = ValidationUtil.parseStrictDate(extra2);
                    user = new Patient(userId, username, password, phone, email, role, extra1, dob);
                    break;
                default:
                    System.err.println("Skipping user record with unknown role: " + line);
                    return null;
            }
        } catch (ParseException e) {
            System.err.println("Skipping user record with invalid date of birth: " + line);
            return null;
        }
        user.setActive(active);
        return user;
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(toLine(user));
        }
        TextFileUtil.writeLines(dataFile, HEADER, lines);
    }

    private String toLine(User user) {
        String extra1 = "";
        String extra2 = "";
        String doctorManagerId = "";
        if (user instanceof AdminStaff) {
            extra1 = ((AdminStaff) user).getStaffId();
        } else if (user instanceof MedicalManager) {
            extra1 = ((MedicalManager) user).getManagerId();
        } else if (user instanceof Doctor) {
            Doctor doctor = (Doctor) user;
            extra1 = doctor.getDoctorId();
            extra2 = TextFileUtil.emptyIfNull(doctor.getSpecialty());
            doctorManagerId = doctor.getShiftManager() == null ? "" : doctor.getShiftManager().getManagerId();
        } else if (user instanceof Patient) {
            Patient patient = (Patient) user;
            extra1 = patient.getPatientId();
            extra2 = patient.getDateOfBirth() == null ? "" : DATE_FORMAT.format(patient.getDateOfBirth());
        }
        return String.join("|",
                user.getUserId(),
                user.getUserType(),
                user.getUsername(),
                user.getPassword(),
                TextFileUtil.emptyIfNull(user.getPhone()),
                TextFileUtil.emptyIfNull(user.getEmail()),
                TextFileUtil.emptyIfNull(extra1),
                TextFileUtil.emptyIfNull(extra2),
                String.valueOf(user.isActive()),
                doctorManagerId);
    }
}
