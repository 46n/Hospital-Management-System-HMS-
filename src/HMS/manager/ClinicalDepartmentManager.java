package hms.manager;

import hms.data.TextFileUtil;
import hms.model.ClinicalDepartment;
import hms.model.MedicalManager;
import hms.model.User;
import hms.util.IdGenerator;
import hms.util.ValidationUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Coordinates clinical department management, backed by clinical_departments.txt. */
public class ClinicalDepartmentManager {

    private static final String DATA_FILE = "data/clinical_departments.txt";
    private static final String HEADER = "#departmentId|departmentName|description|active|managedByManagerId";

    private final List<ClinicalDepartment> departments = new ArrayList<>();
    private final UserManager userManager;
    private final File dataFile;

    public ClinicalDepartmentManager(UserManager userManager) {
        this(userManager, new File(DATA_FILE));
    }

    public ClinicalDepartmentManager(UserManager userManager, File dataFile) {
        if (userManager == null) {
            throw new IllegalArgumentException("User manager is required");
        }
        this.userManager = userManager;
        this.dataFile = dataFile;
        load();
    }

    public ClinicalDepartment createDepartment(String departmentName, String description, String createdByManagerId) {
        ValidationUtil.requireNonBlank(departmentName, "Department name");
        requireUniqueName(departmentName, null);
        MedicalManager manager = requireMedicalManager(createdByManagerId);

        ClinicalDepartment department = new ClinicalDepartment(
                IdGenerator.next("CD", collectIds()), departmentName, description, true);
        department.setMedicalManager(manager);
        departments.add(department);
        save();
        return department;
    }

    public void updateDepartment(String departmentId, String departmentName, String description) {
        ClinicalDepartment department = requireDepartment(departmentId);
        ValidationUtil.requireNonBlank(departmentName, "Department name");
        requireUniqueName(departmentName, departmentId);
        department.setDepartmentName(departmentName);
        department.setDescription(description);
        save();
    }

    public void setActive(String departmentId, boolean active) {
        ClinicalDepartment department = requireDepartment(departmentId);
        department.setActive(active);
        save();
    }

    public void deleteDepartment(String departmentId) {
        ClinicalDepartment department = requireDepartment(departmentId);
        departments.remove(department);
        save();
    }

    public ClinicalDepartment findById(String departmentId) {
        if (departmentId == null) {
            return null;
        }
        for (ClinicalDepartment department : departments) {
            if (departmentId.equals(department.getDepartmentId())) {
                return department;
            }
        }
        return null;
    }

    public List<ClinicalDepartment> getDepartments() {
        return new ArrayList<>(departments);
    }

    private void requireUniqueName(String departmentName, String excludingDepartmentId) {
        for (ClinicalDepartment department : departments) {
            if (department.getDepartmentId().equals(excludingDepartmentId)) {
                continue;
            }
            if (department.getDepartmentName().equalsIgnoreCase(departmentName.trim())) {
                throw new IllegalArgumentException("A department named '" + departmentName + "' already exists");
            }
        }
    }

    private MedicalManager requireMedicalManager(String managerId) {
        User user = userManager.findById(managerId);
        if (!(user instanceof MedicalManager)) {
            throw new IllegalArgumentException("Medical Manager not found: " + managerId);
        }
        return (MedicalManager) user;
    }

    private ClinicalDepartment requireDepartment(String departmentId) {
        ClinicalDepartment department = findById(departmentId);
        if (department == null) {
            throw new IllegalArgumentException("Clinical department not found: " + departmentId);
        }
        return department;
    }

    private List<String> collectIds() {
        List<String> ids = new ArrayList<>();
        for (ClinicalDepartment department : departments) {
            ids.add(department.getDepartmentId());
        }
        return ids;
    }

    private void load() {
        for (String line : TextFileUtil.readLines(dataFile)) {
            ClinicalDepartment department = parseLine(line);
            if (department != null) {
                departments.add(department);
            }
        }
    }

    private ClinicalDepartment parseLine(String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length < 5) {
            System.err.println("Skipping malformed clinical department record: " + line);
            return null;
        }
        ClinicalDepartment department = new ClinicalDepartment(
                fields[0], fields[1], fields[2], Boolean.parseBoolean(fields[3]));
        if (!fields[4].isEmpty()) {
            User user = userManager.findById(fields[4]);
            if (user instanceof MedicalManager) {
                department.setMedicalManager((MedicalManager) user);
            }
        }
        return department;
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (ClinicalDepartment department : departments) {
            lines.add(toLine(department));
        }
        TextFileUtil.writeLines(dataFile, HEADER, lines);
    }

    private String toLine(ClinicalDepartment department) {
        return String.join("|",
                department.getDepartmentId(),
                TextFileUtil.emptyIfNull(department.getDepartmentName()),
                TextFileUtil.emptyIfNull(department.getDescription()),
                String.valueOf(department.isActive()),
                department.getMedicalManager() == null ? "" : department.getMedicalManager().getManagerId());
    }
}
