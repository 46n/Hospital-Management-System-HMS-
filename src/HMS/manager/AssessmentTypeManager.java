package hms.manager;

import hms.data.TextFileUtil;
import hms.model.AssessmentType;
import hms.model.MedicalManager;
import hms.model.User;
import hms.util.IdGenerator;
import hms.util.ValidationUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Coordinates assessment type configuration, backed by assessment_types.txt. */
public class AssessmentTypeManager {

    private static final String DATA_FILE = "data/assessment_types.txt";
    private static final String HEADER =
            "#assessmentTypeId|name|category|gradingRule|baseCharge|active|createdByManagerId";
    private static final List<String> VALID_CATEGORIES =
            Arrays.asList("LAB", "IMAGING", "VITALS", "PHYSICAL_EXAM");
    private static final List<String> VALID_GRADING_RULES =
            Arrays.asList("NUMERIC_RANGE", "PASS_FAIL", "DESCRIPTIVE");

    private final List<AssessmentType> types = new ArrayList<>();
    private final UserManager userManager;
    private final File dataFile;

    public AssessmentTypeManager(UserManager userManager) {
        this(userManager, new File(DATA_FILE));
    }

    public AssessmentTypeManager(UserManager userManager, File dataFile) {
        if (userManager == null) {
            throw new IllegalArgumentException("User manager is required");
        }
        this.userManager = userManager;
        this.dataFile = dataFile;
        load();
    }

    public static List<String> getValidCategories() {
        return new ArrayList<>(VALID_CATEGORIES);
    }

    public static List<String> getValidGradingRules() {
        return new ArrayList<>(VALID_GRADING_RULES);
    }

    public AssessmentType createType(String name, String category, String gradingRule,
            double baseCharge, String createdByManagerId) {
        ValidationUtil.requireNonBlank(name, "Name");
        requireUniqueName(name, null);
        validateCategory(category);
        validateGradingRule(gradingRule);
        ValidationUtil.requireNonNegative(baseCharge, "Base charge");
        MedicalManager manager = requireMedicalManager(createdByManagerId);

        AssessmentType type = new AssessmentType(
                IdGenerator.next("AT", collectIds()), name, category, gradingRule, baseCharge, true);
        type.setCreatedBy(manager);
        types.add(type);
        save();
        return type;
    }

    public void updateType(String typeId, String name, String category, String gradingRule, double baseCharge) {
        AssessmentType type = requireType(typeId);
        ValidationUtil.requireNonBlank(name, "Name");
        requireUniqueName(name, typeId);
        validateCategory(category);
        validateGradingRule(gradingRule);
        ValidationUtil.requireNonNegative(baseCharge, "Base charge");
        type.setName(name);
        type.setCategory(category);
        type.setGradingRule(gradingRule);
        type.setBaseCharge(baseCharge);
        save();
    }

    public void setActive(String typeId, boolean active) {
        AssessmentType type = requireType(typeId);
        type.setActive(active);
        save();
    }

    public void deleteType(String typeId) {
        AssessmentType type = requireType(typeId);
        types.remove(type);
        save();
    }

    public AssessmentType findById(String typeId) {
        if (typeId == null) {
            return null;
        }
        for (AssessmentType type : types) {
            if (typeId.equals(type.getAssessmentTypeId())) {
                return type;
            }
        }
        return null;
    }

    public List<AssessmentType> getTypes() {
        return new ArrayList<>(types);
    }

    private void requireUniqueName(String name, String excludingTypeId) {
        for (AssessmentType type : types) {
            if (type.getAssessmentTypeId().equals(excludingTypeId)) {
                continue;
            }
            if (type.getName().equalsIgnoreCase(name.trim())) {
                throw new IllegalArgumentException("An assessment type named '" + name + "' already exists");
            }
        }
    }

    private void validateCategory(String category) {
        ValidationUtil.requireNonBlank(category, "Type");
        if (!VALID_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Type must be one of " + VALID_CATEGORIES);
        }
    }

    private void validateGradingRule(String gradingRule) {
        ValidationUtil.requireNonBlank(gradingRule, "Grading rule");
        if (!VALID_GRADING_RULES.contains(gradingRule)) {
            throw new IllegalArgumentException("Grading rule must be one of " + VALID_GRADING_RULES);
        }
    }

    private MedicalManager requireMedicalManager(String managerId) {
        User user = userManager.findById(managerId);
        if (!(user instanceof MedicalManager)) {
            throw new IllegalArgumentException("Medical Manager not found: " + managerId);
        }
        return (MedicalManager) user;
    }

    private AssessmentType requireType(String typeId) {
        AssessmentType type = findById(typeId);
        if (type == null) {
            throw new IllegalArgumentException("Assessment type not found: " + typeId);
        }
        return type;
    }

    private List<String> collectIds() {
        List<String> ids = new ArrayList<>();
        for (AssessmentType type : types) {
            ids.add(type.getAssessmentTypeId());
        }
        return ids;
    }

    private void load() {
        for (String line : TextFileUtil.readLines(dataFile)) {
            AssessmentType type = parseLine(line);
            if (type != null) {
                types.add(type);
            }
        }
    }

    private AssessmentType parseLine(String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length < 7) {
            System.err.println("Skipping malformed assessment type record: " + line);
            return null;
        }
        try {
            AssessmentType type = new AssessmentType(fields[0], fields[1], fields[2], fields[3],
                    Double.parseDouble(fields[4]), Boolean.parseBoolean(fields[5]));
            if (!fields[6].isEmpty()) {
                User user = userManager.findById(fields[6]);
                if (user instanceof MedicalManager) {
                    type.setCreatedBy((MedicalManager) user);
                }
            }
            return type;
        } catch (NumberFormatException e) {
            System.err.println("Skipping assessment type record with invalid data: " + line);
            return null;
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (AssessmentType type : types) {
            lines.add(toLine(type));
        }
        TextFileUtil.writeLines(dataFile, HEADER, lines);
    }

    private String toLine(AssessmentType type) {
        return String.join("|",
                type.getAssessmentTypeId(),
                TextFileUtil.emptyIfNull(type.getName()),
                TextFileUtil.emptyIfNull(type.getCategory()),
                TextFileUtil.emptyIfNull(type.getGradingRule()),
                String.valueOf(type.getBaseCharge()),
                String.valueOf(type.isActive()),
                type.getCreatedBy() == null ? "" : type.getCreatedBy().getManagerId());
    }
}
