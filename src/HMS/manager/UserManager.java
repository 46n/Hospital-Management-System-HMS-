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

    public void deleteUser(String userId) {
        User user = requireById(userId);
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
     * a doctor is never listed under two managers at once.
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

        MedicalManager newManager = requireMedicalManager(managerId);
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
        Map<String, String> doctorManagerLinks = new HashMap<>();
        for (String line : TextFileUtil.readLines(dataFile)) {
            User user = parseLine(line);
            if (user == null) {
                continue;
            }
            users.add(user);
            if (user instanceof Doctor) {
                String[] fields = line.split("\\|", -1);
                if (fields.length >= 10 && !fields[9].isEmpty()) {
                    doctorManagerLinks.put(user.getUserId(), fields[9]);
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

    private User parseLine(String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length < 8) {
            System.err.println("Skipping malformed user record: " + line);
            return null;
        }
        String userId = fields[0];
        String role = fields[1];
        String username = fields[2];
        String password = fields[3];
        String phone = fields[4];
        String email = fields[5];
        String extra1 = fields[6];
        String extra2 = fields[7];

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
                    user = new Doctor(userId, username, password, phone, email, role, extra1, extra2);
                    break;
                case "PATIENT":
                    Date dob = extra2.isEmpty() ? null : DATE_FORMAT.parse(extra2);
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
        boolean active = fields.length < 9 || Boolean.parseBoolean(fields[8]);
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
