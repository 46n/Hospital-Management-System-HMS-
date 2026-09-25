package hms.manager;

import hms.model.AdminStaff;
import hms.model.Appointment;
import hms.model.Doctor;
import hms.model.MedicalManager;
import hms.model.Patient;
import hms.model.User;
import hms.ui.admin.DoctorManagementPanel;
import hms.ui.admin.UserManagementPanel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * Standalone, dependency-free Step 4 tests for UserManager (registration,
 * search/read, profile and role-specific update, password reset,
 * deactivation, deletion with Doctor/Medical Manager relationship cleanup,
 * and hardened users.txt loading) and the Admin self-protection and
 * dependency-blocking checks enforced in UserManagementPanel. Same
 * hand-rolled runner style as the Step 1-3 test files, since the project has
 * no JUnit on its classpath. Every test works against its own temporary
 * directory (never the real data/ directory) and deletes it afterward.
 *
 * Run with (from the project root, after compiling src to build/classes):
 *   javac -d build/test-classes -cp build/classes test/HMS/manager/UserManagerTest.java
 *   java -cp build/classes;build/test-classes hms.manager.UserManagerTest
 */
public class UserManagerTest {

    private static final SimpleDateFormat DOB_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    static {
        DOB_FORMAT.setLenient(false);
    }

    private int passed = 0;
    private int failed = 0;

    public static void main(String[] args) throws Exception {
        UserManagerTest suite = new UserManagerTest();

        // ---- registration, search/read ----
        suite.run("testRegisterAndReloadAllFourRoles", suite::testRegisterAndReloadAllFourRoles);
        suite.run("testDuplicateUsernameRejectedCaseInsensitiveAtRegistration",
                suite::testDuplicateUsernameRejectedCaseInsensitiveAtRegistration);
        suite.run("testRegisterRejectsBlankRequiredFields", suite::testRegisterRejectsBlankRequiredFields);
        suite.run("testFindByIdAndUsernameCaseInsensitive", suite::testFindByIdAndUsernameCaseInsensitive);
        suite.run("testSearchPanelFiltersByUsernameRoleAndIdCaseInsensitive",
                suite::testSearchPanelFiltersByUsernameRoleAndIdCaseInsensitive);

        // ---- update ----
        suite.run("testUpdateManagedUserChangesUsernamePhoneEmail", suite::testUpdateManagedUserChangesUsernamePhoneEmail);
        suite.run("testUpdateManagedUserMayKeepOwnUsername", suite::testUpdateManagedUserMayKeepOwnUsername);
        suite.run("testUpdateManagedUserRejectsDuplicateUsernameCaseInsensitive",
                suite::testUpdateManagedUserRejectsDuplicateUsernameCaseInsensitive);
        suite.run("testUpdateManagedUserRejectsInvalidPhone", suite::testUpdateManagedUserRejectsInvalidPhone);
        suite.run("testUpdateManagedUserRejectsInvalidEmail", suite::testUpdateManagedUserRejectsInvalidEmail);
        suite.run("testUpdateManagedUserDoesNotChangeRoleOrId", suite::testUpdateManagedUserDoesNotChangeRoleOrId);
        suite.run("testUpdateDoctorSpecialtyValidAndInvalid", suite::testUpdateDoctorSpecialtyValidAndInvalid);
        suite.run("testUpdateManagedUserValidPatientDateOfBirthAndRejectsNull",
                suite::testUpdateManagedUserValidPatientDateOfBirthAndRejectsNull);
        suite.run("testInvalidPatientDateOfBirthLeavesEveryFieldUnchanged",
                suite::testInvalidPatientDateOfBirthLeavesEveryFieldUnchanged);
        suite.run("testInvalidDoctorSpecialtyLeavesEveryFieldUnchanged",
                suite::testInvalidDoctorSpecialtyLeavesEveryFieldUnchanged);
        suite.run("testSuccessfulManagedUpdatePersistsAllFieldsAfterReload",
                suite::testSuccessfulManagedUpdatePersistsAllFieldsAfterReload);

        // ---- password reset ----
        suite.run("testResetPasswordPersistsAfterReload", suite::testResetPasswordPersistsAfterReload);
        suite.run("testResetPasswordRejectsBlankPassword", suite::testResetPasswordRejectsBlankPassword);

        // ---- deactivation/reactivation and login ----
        suite.run("testDeactivationPreventsLogin", suite::testDeactivationPreventsLogin);
        suite.run("testReactivationRestoresLogin", suite::testReactivationRestoresLogin);

        // ---- delete and relationship integrity ----
        suite.run("testDeletingDoctorRemovesFromManagerAndAllowsCleanDeletion",
                suite::testDeletingDoctorRemovesFromManagerAndAllowsCleanDeletion);
        suite.run("testDeletingManagerUnassignsAllDoctors", suite::testDeletingManagerUnassignsAllDoctors);
        suite.run("testDoctorWithAppointmentBlocksDeletionViaPanel", suite::testDoctorWithAppointmentBlocksDeletionViaPanel);
        suite.run("testPatientWithAppointmentBlocksDeletionViaPanel", suite::testPatientWithAppointmentBlocksDeletionViaPanel);
        suite.run("testAdminStaffWithManagedAssetBlocksDeletionViaPanel",
                suite::testAdminStaffWithManagedAssetBlocksDeletionViaPanel);
        suite.run("testCleanUserHasNoDependencyBlockingDeletion", suite::testCleanUserHasNoDependencyBlockingDeletion);

        // ---- doctor-to-Medical-Manager assignment (Step 5) ----
        suite.run("testAssignDoctorToManagerInitialAssignment", suite::testAssignDoctorToManagerInitialAssignment);
        suite.run("testReassignDoctorToDifferentManagerRemovesFromPrevious",
                suite::testReassignDoctorToDifferentManagerRemovesFromPrevious);
        suite.run("testUnassignDoctorRemovesFromManager", suite::testUnassignDoctorRemovesFromManager);
        suite.run("testReassignSameManagerDoesNotDuplicate", suite::testReassignSameManagerDoesNotDuplicate);
        suite.run("testAssignmentPersistsAfterReload", suite::testAssignmentPersistsAfterReload);
        suite.run("testUnassignmentPersistsAfterReload", suite::testUnassignmentPersistsAfterReload);
        suite.run("testAssignRejectsInvalidDoctorId", suite::testAssignRejectsInvalidDoctorId);
        suite.run("testAssignRejectsNonDoctorId", suite::testAssignRejectsNonDoctorId);
        suite.run("testAssignRejectsInvalidManagerId", suite::testAssignRejectsInvalidManagerId);
        suite.run("testAssignRejectsNonManagerId", suite::testAssignRejectsNonManagerId);
        suite.run("testAssignRejectsInactiveDoctor", suite::testAssignRejectsInactiveDoctor);
        suite.run("testAssignRejectsInactiveManager", suite::testAssignRejectsInactiveManager);
        suite.run("testUnassignAllowedForInactiveDoctor", suite::testUnassignAllowedForInactiveDoctor);

        // ---- self-protection ----
        suite.run("testCannotSelectSelfForDeletionOrDeactivation", suite::testCannotSelectSelfForDeletionOrDeactivation);

        // ---- users.txt loading hardening ----
        suite.run("testMissingFileLoadsEmpty", suite::testMissingFileLoadsEmpty);
        suite.run("testLoadSkipsMalformedRowTooFewFields", suite::testLoadSkipsMalformedRowTooFewFields);
        suite.run("testLoadSkipsRowWithTooManyFields", suite::testLoadSkipsRowWithTooManyFields);
        suite.run("testLoadSkipsBlankUserId", suite::testLoadSkipsBlankUserId);
        suite.run("testLoadSkipsBlankRole", suite::testLoadSkipsBlankRole);
        suite.run("testLoadSkipsBlankUsername", suite::testLoadSkipsBlankUsername);
        suite.run("testLoadSkipsBlankPassword", suite::testLoadSkipsBlankPassword);
        suite.run("testLoadSkipsBlankPhone", suite::testLoadSkipsBlankPhone);
        suite.run("testLoadSkipsBlankEmail", suite::testLoadSkipsBlankEmail);
        suite.run("testLoadSkipsUnknownRole", suite::testLoadSkipsUnknownRole);
        suite.run("testLoadSkipsDuplicateUserId", suite::testLoadSkipsDuplicateUserId);
        suite.run("testLoadSkipsDuplicateUsernameCaseInsensitive", suite::testLoadSkipsDuplicateUsernameCaseInsensitive);
        suite.run("testLoadSkipsInvalidActiveValue", suite::testLoadSkipsInvalidActiveValue);
        suite.run("testLoadDefaultsActiveTrueWhenFieldMissing", suite::testLoadDefaultsActiveTrueWhenFieldMissing);
        suite.run("testLoadSkipsBlankActiveFieldWhenColumnPresent", suite::testLoadSkipsBlankActiveFieldWhenColumnPresent);
        suite.run("testLoadSkipsInvalidDoctorSpecialty", suite::testLoadSkipsInvalidDoctorSpecialty);
        suite.run("testLoadSkipsBlankPatientDateOfBirth", suite::testLoadSkipsBlankPatientDateOfBirth);
        suite.run("testLoadSkipsInvalidPatientDateOfBirth", suite::testLoadSkipsInvalidPatientDateOfBirth);
        suite.run("testLoadSkipsPatientDateOfBirthWithTrailingCharacters",
                suite::testLoadSkipsPatientDateOfBirthWithTrailingCharacters);
        suite.run("testLoadSkipsNonCanonicalPatientDateOfBirth", suite::testLoadSkipsNonCanonicalPatientDateOfBirth);
        suite.run("testAdminDateParsingRejectsTrailingCharacters", suite::testAdminDateParsingRejectsTrailingCharacters);
        suite.run("testAdminDateParsingRejectsNonCanonicalForm", suite::testAdminDateParsingRejectsNonCanonicalForm);
        suite.run("testLoadSkipsDoctorManagerLinkToNonManagerUser", suite::testLoadSkipsDoctorManagerLinkToNonManagerUser);
        suite.run("testLoadSkipsDoctorManagerLinkToUnknownManagerId", suite::testLoadSkipsDoctorManagerLinkToUnknownManagerId);
        suite.run("testLoadResolvesLinkToInactiveManager", suite::testLoadResolvesLinkToInactiveManager);
        suite.run("testLoadResolvesDoctorManagerLinkRegardlessOfRowOrder",
                suite::testLoadResolvesDoctorManagerLinkRegardlessOfRowOrder);
        suite.run("testValidLegacyRecordWithoutTrailingColumnsStillLoads",
                suite::testValidLegacyRecordWithoutTrailingColumnsStillLoads);

        // ---- GUI checks ----
        suite.run("testUserTableDisplaysRequiredColumns", suite::testUserTableDisplaysRequiredColumns);
        suite.run("testUserManagementButtonsHaveActionListeners", suite::testUserManagementButtonsHaveActionListeners);
        suite.run("testDoctorManagementTableDisplaysRequiredColumns", suite::testDoctorManagementTableDisplaysRequiredColumns);
        suite.run("testDoctorManagementButtonsHaveActionListeners", suite::testDoctorManagementButtonsHaveActionListeners);
        suite.run("testDoctorSearchMatchesIdUsernameSpecialtyManagerAndAssignedState",
                suite::testDoctorSearchMatchesIdUsernameSpecialtyManagerAndAssignedState);
        suite.run("testAssignmentDialogOffersOnlyActiveManagers", suite::testAssignmentDialogOffersOnlyActiveManagers);

        System.out.println();
        System.out.println(suite.passed + " passed, " + suite.failed + " failed");
        if (suite.failed > 0) {
            System.exit(1);
        }
    }

    private interface TestCase {
        void run() throws Exception;
    }

    private void run(String name, TestCase test) {
        try {
            test.run();
            System.out.println("PASS  " + name);
            passed++;
        } catch (Throwable t) {
            System.out.println("FAIL  " + name + " - " + t.getMessage());
            failed++;
        }
    }

    // ---- fixtures ----

    private static Date dob(String value) {
        try {
            return DOB_FORMAT.parse(value);
        } catch (ParseException e) {
            throw new IllegalStateException("Bad fixture date: " + value, e);
        }
    }

    /** A full, wired-together environment, backed only by temporary files - never data/. */
    private static final class Fixture implements AutoCloseable {
        final Path tempDir;
        final File usersFile;
        final UserManager userManager;
        final DoctorShiftManager shiftManager;
        final AppointmentManager appointmentManager;
        final ClinicalManager clinicalManager;
        final HospitalAssetManager hospitalAssetManager;
        final AssetAllocationManager allocationManager;
        final ConsultationRateManager rateManager;
        final InsuranceNetworkManager insuranceManager;
        final SessionManager sessionManager;

        Fixture() throws IOException {
            tempDir = Files.createTempDirectory("hms-usermanager-test-");
            usersFile = new File(tempDir.toFile(), "users.txt");
            File shiftsFile = new File(tempDir.toFile(), "doctor_shifts.txt");
            File appointmentsFile = new File(tempDir.toFile(), "appointments.txt");
            File typesFile = new File(tempDir.toFile(), "assessment_types.txt");
            File recordsFile = new File(tempDir.toFile(), "medical_records.txt");
            File prescriptionsFile = new File(tempDir.toFile(), "prescriptions.txt");
            File requestsFile = new File(tempDir.toFile(), "medical_requests.txt");
            File resultsFile = new File(tempDir.toFile(), "assessment_results.txt");
            File feedbackFile = new File(tempDir.toFile(), "feedback.txt");
            File assetsFile = new File(tempDir.toFile(), "hospital_assets.txt");
            File allocationsFile = new File(tempDir.toFile(), "asset_allocations.txt");
            File ratesFile = new File(tempDir.toFile(), "consultation_rates.txt");
            File insuranceFile = new File(tempDir.toFile(), "insurance_networks.txt");

            userManager = new UserManager(usersFile);
            shiftManager = new DoctorShiftManager(userManager, shiftsFile);
            appointmentManager = new AppointmentManager(userManager, shiftManager, appointmentsFile);
            AssessmentTypeManager typeManager = new AssessmentTypeManager(userManager, typesFile);
            clinicalManager = new ClinicalManager(userManager, appointmentManager, typeManager,
                    recordsFile, prescriptionsFile, requestsFile, resultsFile, feedbackFile);
            hospitalAssetManager = new HospitalAssetManager(userManager, assetsFile);
            allocationManager = new AssetAllocationManager(clinicalManager, hospitalAssetManager, userManager,
                    allocationsFile);
            rateManager = new ConsultationRateManager(userManager, ratesFile);
            insuranceManager = new InsuranceNetworkManager(userManager, insuranceFile);
            sessionManager = new SessionManager(userManager);
        }

        AdminStaff registerAdmin(String username) {
            return (AdminStaff) userManager.registerUser(
                    new AdminStaff(null, username, "pass1234", "0111111111", username + "@test.com", "ADMIN_STAFF", null));
        }

        MedicalManager registerManager(String username) {
            return (MedicalManager) userManager.registerUser(
                    new MedicalManager(null, username, "pass1234", "0122222222", username + "@test.com", "MEDICAL_MANAGER", null));
        }

        Doctor registerDoctor(String username, String specialty) {
            return (Doctor) userManager.registerUser(
                    new Doctor(null, username, "pass1234", "0133333333", username + "@test.com", "DOCTOR", null, specialty));
        }

        Patient registerPatient(String username, String dobText) {
            return (Patient) userManager.registerUser(
                    new Patient(null, username, "pass1234", "0144444444", username + "@test.com", "PATIENT", null, dob(dobText)));
        }

        /**
         * Assigns the doctor to the manager (a shift can only be created for a supervised
         * doctor), creates a shift covering the given window, then books a real appointment
         * inside it.
         */
        Appointment bookAppointment(Doctor doctor, Patient patient, String managerId, String dateText,
                String startTimeText, String endTimeText) throws ParseException {
            userManager.assignDoctorToManager(doctor.getDoctorId(), managerId);
            SimpleDateFormat shiftDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date shiftDate = shiftDateFormat.parse(dateText);
            LocalTime startTime = LocalTime.parse(startTimeText);
            LocalTime endTime = LocalTime.parse(endTimeText);
            shiftManager.createShift(managerId, doctor.getDoctorId(), shiftDate, startTime, endTime);
            return appointmentManager.book(patient.getPatientId(), doctor.getDoctorId(), shiftDate, startTime);
        }

        UserManagementPanel newPanel() {
            return new UserManagementPanel(userManager, appointmentManager, clinicalManager, hospitalAssetManager,
                    rateManager, insuranceManager, allocationManager, sessionManager, null, () -> { });
        }

        DoctorManagementPanel newDoctorPanel() {
            return new DoctorManagementPanel(userManager, appointmentManager, clinicalManager, hospitalAssetManager,
                    rateManager, insuranceManager, allocationManager, sessionManager, null, () -> { });
        }

        @Override
        public void close() throws IOException {
            deleteRecursively(tempDir);
        }
    }

    private static void writeFile(File file, String... lines) throws IOException {
        List<String> content = new ArrayList<>();
        for (String line : lines) {
            content.add(line);
        }
        Files.write(file.toPath(), content, StandardCharsets.UTF_8);
    }

    /** Ordinary recursion instead of a stream pipeline: delete every file inside, then the folder itself. */
    private static void deleteRecursively(Path root) throws IOException {
        File folder = root.toFile();
        if (!folder.exists()) {
            return;
        }
        File[] children = folder.listFiles();
        if (children == null) {
            throw new IOException("Could not list directory contents: " + folder);
        }
        for (File child : children) {
            if (child.isDirectory()) {
                deleteRecursively(child.toPath());
            } else {
                Files.deleteIfExists(child.toPath());
            }
        }
        Files.deleteIfExists(root);
    }

    // ---- assertions ----

    private static void assertEquals(Object expected, Object actual, String message) {
        boolean equal = expected == null ? actual == null : expected.equals(actual);
        if (!equal) {
            throw new AssertionError(message + " (expected <" + expected + "> but was <" + actual + ">)");
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertNull(Object value, String message) {
        if (value != null) {
            throw new AssertionError(message + " (was <" + value + ">)");
        }
    }

    private static void assertNotNull(Object value, String message) {
        if (value == null) {
            throw new AssertionError(message);
        }
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static <T extends Exception> void assertThrows(Class<T> type, ThrowingRunnable action, String message) {
        try {
            action.run();
        } catch (Exception e) {
            if (type.isInstance(e)) {
                return;
            }
            throw new AssertionError(message + " (threw " + e.getClass().getSimpleName() + " instead)");
        }
        throw new AssertionError(message + " (nothing was thrown)");
    }

    private static Object getPrivateField(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    /**
     * Invokes a private method directly, instead of driving it through a button click. Used for
     * the panel's self-protection, dependency-blocking, and date-parsing checks, since clicking
     * the real buttons would open a blocking JOptionPane dialog with no one there to close it.
     * Reflection wraps whatever the method itself throws inside an InvocationTargetException, so
     * that wrapper is unwrapped here and the real exception (e.g. IllegalArgumentException) is
     * rethrown, letting assertThrows see the exception a caller would actually catch.
     */
    private static Object invokePrivateMethod(Object target, String methodName, Class<?>[] paramTypes, Object... args)
            throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw e;
        }
    }

    // ---- tests: registration, search/read ----

    private void testRegisterAndReloadAllFourRoles() throws Exception {
        try (Fixture fixture = new Fixture()) {
            AdminStaff admin = fixture.registerAdmin("admin1");
            MedicalManager manager = fixture.registerManager("manager1");
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            Patient patient = fixture.registerPatient("patient1", "1990-01-01");

            UserManager reloaded = new UserManager(fixture.usersFile);
            assertEquals(4, reloaded.getUsers().size(), "all four registered users must reload");

            AdminStaff reloadedAdmin = (AdminStaff) reloaded.findById(admin.getUserId());
            assertNotNull(reloadedAdmin, "Admin Staff must reload");
            assertEquals(admin.getStaffId(), reloadedAdmin.getStaffId(), "Admin Staff ID must survive reload");

            MedicalManager reloadedManager = (MedicalManager) reloaded.findById(manager.getUserId());
            assertNotNull(reloadedManager, "Medical Manager must reload");

            Doctor reloadedDoctor = reloaded.findDoctorById(doctor.getUserId());
            assertNotNull(reloadedDoctor, "Doctor must reload");
            assertEquals("Cardiology", reloadedDoctor.getSpecialty(), "Doctor specialty must survive reload");

            Patient reloadedPatient = reloaded.findPatientById(patient.getUserId());
            assertNotNull(reloadedPatient, "Patient must reload");
            assertEquals(dob("1990-01-01"), reloadedPatient.getDateOfBirth(), "Patient date of birth must survive reload");
        }
    }

    private void testDuplicateUsernameRejectedCaseInsensitiveAtRegistration() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.registerAdmin("admin1");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.registerManager("ADMIN1"),
                    "a duplicate username must be rejected regardless of letter case");
        }
    }

    private void testRegisterRejectsBlankRequiredFields() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.registerUser(
                            new AdminStaff(null, "   ", "pass1234", "0111111111", "a@test.com", "ADMIN_STAFF", null)),
                    "a blank username must be rejected");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.registerUser(
                            new AdminStaff(null, "admin1", "", "0111111111", "a@test.com", "ADMIN_STAFF", null)),
                    "a blank password must be rejected");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.registerUser(
                            new Patient(null, "patient1", "pass1234", "0111111111", "a@test.com", "PATIENT", null, null)),
                    "a Patient with no date of birth must be rejected");
        }
    }

    private void testFindByIdAndUsernameCaseInsensitive() throws Exception {
        try (Fixture fixture = new Fixture()) {
            AdminStaff admin = fixture.registerAdmin("admin1");
            assertEquals(admin.getUserId(), fixture.userManager.findByUsername("ADMIN1").getUserId(),
                    "findByUsername must be case-insensitive");
            assertNotNull(fixture.userManager.findById(admin.getUserId()), "findById must find the registered user");
        }
    }

    private void testSearchPanelFiltersByUsernameRoleAndIdCaseInsensitive() throws Exception {
        try (Fixture fixture = new Fixture()) {
            Doctor doctor = fixture.registerDoctor("houseMd", "Cardiology");
            fixture.registerPatient("patient1", "1990-01-01");
            UserManagementPanel panel = fixture.newPanel();

            JTextField searchField = (JTextField) getPrivateField(panel, "searchField");
            DefaultTableModel tableModel = (DefaultTableModel) getPrivateField(panel, "tableModel");

            searchField.setText(doctor.getUserId().toLowerCase());
            invokePrivateMethod(panel, "applySearch", new Class<?>[0]);
            assertEquals(1, tableModel.getRowCount(), "searching by lowercased ID must find the Doctor");

            searchField.setText("doctor");
            invokePrivateMethod(panel, "applySearch", new Class<?>[0]);
            assertEquals(1, tableModel.getRowCount(), "searching by role text must find only the Doctor");

            searchField.setText("HOUSEMD");
            invokePrivateMethod(panel, "applySearch", new Class<?>[0]);
            assertEquals(1, tableModel.getRowCount(), "searching by uppercased username must still find the Doctor");
        }
    }

    // ---- tests: update ----

    private void testUpdateManagedUserChangesUsernamePhoneEmail() throws Exception {
        try (Fixture fixture = new Fixture()) {
            AdminStaff admin = fixture.registerAdmin("admin1");
            fixture.userManager.updateManagedUser(admin.getUserId(), "adminRenamed", "0199999999", "renamed@test.com",
                    null, null);
            assertEquals("adminRenamed", admin.getUsername(), "username must be updated");
            assertEquals("0199999999", admin.getPhone(), "phone must be updated");
            assertEquals("renamed@test.com", admin.getEmail(), "email must be updated");
        }
    }

    private void testUpdateManagedUserMayKeepOwnUsername() throws Exception {
        try (Fixture fixture = new Fixture()) {
            AdminStaff admin = fixture.registerAdmin("admin1");
            fixture.userManager.updateManagedUser(admin.getUserId(), "admin1", "0199999999", "same@test.com", null, null);
            assertEquals("admin1", admin.getUsername(), "a user may keep their own username while updating other fields");
        }
    }

    private void testUpdateManagedUserRejectsDuplicateUsernameCaseInsensitive() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.registerAdmin("admin1");
            AdminStaff second = fixture.registerAdmin("admin2");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.updateManagedUser(
                            second.getUserId(), "ADMIN1", "0199999999", "x@test.com", null, null),
                    "renaming to another user's username must be rejected regardless of letter case");
        }
    }

    private void testUpdateManagedUserRejectsInvalidPhone() throws Exception {
        try (Fixture fixture = new Fixture()) {
            AdminStaff admin = fixture.registerAdmin("admin1");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.updateManagedUser(
                            admin.getUserId(), "admin1", "abc", "x@test.com", null, null),
                    "an invalid phone must be rejected");
        }
    }

    private void testUpdateManagedUserRejectsInvalidEmail() throws Exception {
        try (Fixture fixture = new Fixture()) {
            AdminStaff admin = fixture.registerAdmin("admin1");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.updateManagedUser(
                            admin.getUserId(), "admin1", "0199999999", "not-an-email", null, null),
                    "an invalid email must be rejected");
        }
    }

    private void testUpdateManagedUserDoesNotChangeRoleOrId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            String originalUserId = doctor.getUserId();
            String originalDoctorId = doctor.getDoctorId();
            fixture.userManager.updateManagedUser(doctor.getUserId(), "doctorRenamed", "0199999999", "d@test.com",
                    "Cardiology", null);
            assertEquals(originalUserId, doctor.getUserId(), "userId must never change through editing");
            assertEquals(originalDoctorId, doctor.getDoctorId(), "the role-specific ID must never change through editing");
            assertEquals("DOCTOR", doctor.getUserType(), "role must never change through editing");
        }
    }

    private void testUpdateDoctorSpecialtyValidAndInvalid() throws Exception {
        try (Fixture fixture = new Fixture()) {
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            fixture.userManager.updateDoctorSpecialty(doctor.getDoctorId(), "Neurology");
            assertEquals("Neurology", doctor.getSpecialty(), "a valid specialty update must be applied");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.updateDoctorSpecialty(doctor.getDoctorId(), "Wizardry"),
                    "an unsupported specialty must be rejected");
        }
    }

    private void testUpdateManagedUserValidPatientDateOfBirthAndRejectsNull() throws Exception {
        try (Fixture fixture = new Fixture()) {
            Patient patient = fixture.registerPatient("patient1", "1990-01-01");
            fixture.userManager.updateManagedUser(patient.getUserId(), "patient1", "0144444444", "patient1@test.com",
                    null, dob("1985-05-20"));
            assertEquals(dob("1985-05-20"), patient.getDateOfBirth(), "a valid date of birth update must be applied");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.updateManagedUser(patient.getUserId(), "patient1", "0144444444",
                            "patient1@test.com", null, null),
                    "a null date of birth must be rejected");
        }
    }

    /** Reviewer-reported correction: a bad Patient DOB must not leave a saved username/phone/email change behind. */
    private void testInvalidPatientDateOfBirthLeavesEveryFieldUnchanged() throws Exception {
        try (Fixture fixture = new Fixture()) {
            Patient patient = fixture.registerPatient("patient1", "1990-01-01");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.updateManagedUser(patient.getUserId(), "renamedPatient", "0199999999",
                            "renamed@test.com", null, null),
                    "a missing date of birth must be rejected for a Patient");
            assertEquals("patient1", patient.getUsername(), "username must be unchanged after a failed update");
            assertEquals("0144444444", patient.getPhone(), "phone must be unchanged after a failed update");
            assertEquals("patient1@test.com", patient.getEmail(), "email must be unchanged after a failed update");
            assertEquals(dob("1990-01-01"), patient.getDateOfBirth(), "date of birth must be unchanged after a failed update");
        }
    }

    /** Reviewer-reported correction: a bad Doctor specialty must not leave a saved username/phone/email change behind. */
    private void testInvalidDoctorSpecialtyLeavesEveryFieldUnchanged() throws Exception {
        try (Fixture fixture = new Fixture()) {
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.updateManagedUser(doctor.getUserId(), "renamedDoctor", "0199999999",
                            "renamed@test.com", "Wizardry", null),
                    "an unsupported specialty must be rejected");
            assertEquals("doctor1", doctor.getUsername(), "username must be unchanged after a failed update");
            assertEquals("0133333333", doctor.getPhone(), "phone must be unchanged after a failed update");
            assertEquals("doctor1@test.com", doctor.getEmail(), "email must be unchanged after a failed update");
            assertEquals("Cardiology", doctor.getSpecialty(), "specialty must be unchanged after a failed update");
        }
    }

    private void testSuccessfulManagedUpdatePersistsAllFieldsAfterReload() throws Exception {
        try (Fixture fixture = new Fixture()) {
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            fixture.userManager.updateManagedUser(doctor.getUserId(), "doctorRenamed", "0199999999",
                    "renamed@test.com", "Neurology", null);

            UserManager reloaded = new UserManager(fixture.usersFile);
            Doctor reloadedDoctor = reloaded.findDoctorById(doctor.getUserId());
            assertNotNull(reloadedDoctor, "the doctor must still reload");
            assertEquals("doctorRenamed", reloadedDoctor.getUsername(), "username must persist after reload");
            assertEquals("0199999999", reloadedDoctor.getPhone(), "phone must persist after reload");
            assertEquals("renamed@test.com", reloadedDoctor.getEmail(), "email must persist after reload");
            assertEquals("Neurology", reloadedDoctor.getSpecialty(), "specialty must persist after reload");
        }
    }

    // ---- tests: password reset ----

    private void testResetPasswordPersistsAfterReload() throws Exception {
        try (Fixture fixture = new Fixture()) {
            AdminStaff admin = fixture.registerAdmin("admin1");
            fixture.userManager.resetPassword(admin.getUserId(), "newPass99");
            UserManager reloaded = new UserManager(fixture.usersFile);
            User loggedIn = reloaded.login("admin1", "newPass99");
            assertNotNull(loggedIn, "login with the reset password must succeed after reload");
        }
    }

    private void testResetPasswordRejectsBlankPassword() throws Exception {
        try (Fixture fixture = new Fixture()) {
            AdminStaff admin = fixture.registerAdmin("admin1");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.resetPassword(admin.getUserId(), "   "),
                    "a blank new password must be rejected");
        }
    }

    // ---- tests: deactivation/reactivation and login ----

    private void testDeactivationPreventsLogin() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.registerAdmin("admin1");
            fixture.userManager.deactivateUser(fixture.userManager.findByUsername("admin1").getUserId());
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.login("admin1", "pass1234"),
                    "a deactivated account must not be able to log in");
        }
    }

    private void testReactivationRestoresLogin() throws Exception {
        try (Fixture fixture = new Fixture()) {
            AdminStaff admin = fixture.registerAdmin("admin1");
            fixture.userManager.deactivateUser(admin.getUserId());
            fixture.userManager.reactivateUser(admin.getUserId());
            User loggedIn = fixture.userManager.login("admin1", "pass1234");
            assertNotNull(loggedIn, "login must succeed again after reactivation");
        }
    }

    // ---- tests: delete and relationship integrity ----

    private void testDeletingDoctorRemovesFromManagerAndAllowsCleanDeletion() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalManager manager = fixture.registerManager("manager1");
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            fixture.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());
            assertTrue(manager.getManagedDoctors().contains(doctor), "the manager must list the assigned doctor first");

            fixture.userManager.deleteUser(doctor.getUserId());
            assertNull(fixture.userManager.findById(doctor.getUserId()), "the doctor must actually be deleted");
            assertTrue(!manager.getManagedDoctors().contains(doctor),
                    "the manager must no longer list a deleted doctor");
        }
    }

    private void testDeletingManagerUnassignsAllDoctors() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalManager manager = fixture.registerManager("manager1");
            Doctor doctorA = fixture.registerDoctor("doctorA", "Cardiology");
            Doctor doctorB = fixture.registerDoctor("doctorB", "Neurology");
            fixture.userManager.assignDoctorToManager(doctorA.getDoctorId(), manager.getManagerId());
            fixture.userManager.assignDoctorToManager(doctorB.getDoctorId(), manager.getManagerId());

            fixture.userManager.deleteUser(manager.getUserId());
            assertNull(fixture.userManager.findById(manager.getUserId()), "the manager must actually be deleted");
            assertNull(doctorA.getShiftManager(), "doctorA must no longer point at the deleted manager");
            assertNull(doctorB.getShiftManager(), "doctorB must no longer point at the deleted manager");
        }
    }

    // ---- tests: doctor-to-Medical-Manager assignment (Step 5) ----

    private void testAssignDoctorToManagerInitialAssignment() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalManager manager = fixture.registerManager("manager1");
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            fixture.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());
            assertEquals(manager.getManagerId(), doctor.getShiftManager().getManagerId(),
                    "the doctor must point at the newly assigned manager");
            assertTrue(manager.getManagedDoctors().contains(doctor), "the manager must list the newly assigned doctor");
        }
    }

    private void testReassignDoctorToDifferentManagerRemovesFromPrevious() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalManager managerA = fixture.registerManager("managerA");
            MedicalManager managerB = fixture.registerManager("managerB");
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            fixture.userManager.assignDoctorToManager(doctor.getDoctorId(), managerA.getManagerId());

            fixture.userManager.assignDoctorToManager(doctor.getDoctorId(), managerB.getManagerId());
            assertEquals(managerB.getManagerId(), doctor.getShiftManager().getManagerId(),
                    "the doctor must now point at the new manager");
            assertTrue(!managerA.getManagedDoctors().contains(doctor),
                    "the previous manager must no longer list this doctor");
            assertTrue(managerB.getManagedDoctors().contains(doctor), "the new manager must list this doctor");
        }
    }

    private void testUnassignDoctorRemovesFromManager() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalManager manager = fixture.registerManager("manager1");
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            fixture.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());

            fixture.userManager.assignDoctorToManager(doctor.getDoctorId(), null);
            assertNull(doctor.getShiftManager(), "the doctor must no longer point at any manager");
            assertTrue(!manager.getManagedDoctors().contains(doctor), "the manager must no longer list this doctor");
        }
    }

    private void testReassignSameManagerDoesNotDuplicate() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalManager manager = fixture.registerManager("manager1");
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            fixture.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());
            fixture.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());

            int occurrences = 0;
            for (Doctor managedDoctor : manager.getManagedDoctors()) {
                if (managedDoctor.getDoctorId().equals(doctor.getDoctorId())) {
                    occurrences++;
                }
            }
            assertEquals(1, occurrences, "reassigning to the same manager must not create a duplicate entry");
        }
    }

    private void testAssignmentPersistsAfterReload() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalManager manager = fixture.registerManager("manager1");
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            fixture.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());

            UserManager reloaded = new UserManager(fixture.usersFile);
            Doctor reloadedDoctor = reloaded.findDoctorById(doctor.getDoctorId());
            MedicalManager reloadedManager = reloaded.findMedicalManagerById(manager.getManagerId());
            assertNotNull(reloadedDoctor.getShiftManager(), "the assignment must survive reload");
            assertEquals(reloadedManager.getManagerId(), reloadedDoctor.getShiftManager().getManagerId(),
                    "the reloaded doctor must point at the reloaded manager");
            assertTrue(reloadedManager.getManagedDoctors().contains(reloadedDoctor),
                    "the reloaded manager must list the reloaded doctor");
        }
    }

    private void testUnassignmentPersistsAfterReload() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalManager manager = fixture.registerManager("manager1");
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            fixture.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());
            fixture.userManager.assignDoctorToManager(doctor.getDoctorId(), null);

            UserManager reloaded = new UserManager(fixture.usersFile);
            Doctor reloadedDoctor = reloaded.findDoctorById(doctor.getDoctorId());
            assertNull(reloadedDoctor.getShiftManager(), "the unassignment must survive reload");
        }
    }

    private void testAssignRejectsInvalidDoctorId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalManager manager = fixture.registerManager("manager1");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.assignDoctorToManager("UNKNOWN999", manager.getManagerId()),
                    "an unknown doctor ID must be rejected with a clear error");
        }
    }

    private void testAssignRejectsNonDoctorId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalManager manager = fixture.registerManager("manager1");
            Patient patient = fixture.registerPatient("patient1", "1990-01-01");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.assignDoctorToManager(patient.getUserId(), manager.getManagerId()),
                    "a Patient ID must not be accepted as the doctor being assigned");
        }
    }

    private void testAssignRejectsInvalidManagerId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.assignDoctorToManager(doctor.getDoctorId(), "UNKNOWN999"),
                    "an unknown manager ID must be rejected with a clear error");
        }
    }

    private void testAssignRejectsNonManagerId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            Patient patient = fixture.registerPatient("patient1", "1990-01-01");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.assignDoctorToManager(doctor.getDoctorId(), patient.getUserId()),
                    "a Patient ID must not be accepted as the Medical Manager being assigned to");
        }
    }

    private void testAssignRejectsInactiveDoctor() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalManager manager = fixture.registerManager("manager1");
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            fixture.userManager.deactivateUser(doctor.getUserId());
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId()),
                    "an inactive doctor must not be newly assigned to a manager");
            assertNull(doctor.getShiftManager(), "the rejected assignment must not have taken effect");
        }
    }

    private void testAssignRejectsInactiveManager() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalManager manager = fixture.registerManager("manager1");
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            fixture.userManager.deactivateUser(manager.getUserId());
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId()),
                    "a doctor must not be newly assigned to an inactive manager");
            assertNull(doctor.getShiftManager(), "the rejected assignment must not have taken effect");
        }
    }

    private void testUnassignAllowedForInactiveDoctor() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalManager manager = fixture.registerManager("manager1");
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            fixture.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());
            fixture.userManager.deactivateUser(doctor.getUserId());

            fixture.userManager.assignDoctorToManager(doctor.getDoctorId(), null);
            assertNull(doctor.getShiftManager(), "unassigning an inactive doctor's existing relationship must still work");
            assertTrue(!manager.getManagedDoctors().contains(doctor),
                    "the manager must no longer list the inactive, now-unassigned doctor");
        }
    }

    private void testDoctorWithAppointmentBlocksDeletionViaPanel() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalManager manager = fixture.registerManager("manager1");
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            Patient patient = fixture.registerPatient("patient1", "1990-01-01");
            fixture.bookAppointment(doctor, patient, manager.getManagerId(), "2027-06-01", "09:00", "17:00");

            UserManagementPanel panel = fixture.newPanel();
            Object reason = invokePrivateMethod(panel, "findDependencyBlockingDeletion", new Class<?>[]{User.class}, doctor);
            assertNotNull(reason, "a doctor with an existing appointment must not be safely deletable");
        }
    }

    private void testPatientWithAppointmentBlocksDeletionViaPanel() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalManager manager = fixture.registerManager("manager1");
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            Patient patient = fixture.registerPatient("patient1", "1990-01-01");
            fixture.bookAppointment(doctor, patient, manager.getManagerId(), "2027-06-01", "09:00", "17:00");

            UserManagementPanel panel = fixture.newPanel();
            Object reason = invokePrivateMethod(panel, "findDependencyBlockingDeletion", new Class<?>[]{User.class}, patient);
            assertNotNull(reason, "a patient with an existing appointment must not be safely deletable");
        }
    }

    private void testAdminStaffWithManagedAssetBlocksDeletionViaPanel() throws Exception {
        try (Fixture fixture = new Fixture()) {
            AdminStaff admin = fixture.registerAdmin("admin1");
            fixture.hospitalAssetManager.createAsset("Lab Room 1", "LAB", "Ground Floor", true, admin.getStaffId());

            UserManagementPanel panel = fixture.newPanel();
            Object reason = invokePrivateMethod(panel, "findDependencyBlockingDeletion", new Class<?>[]{User.class}, admin);
            assertNotNull(reason, "an Admin Staff managing a hospital asset must not be safely deletable");
        }
    }

    private void testCleanUserHasNoDependencyBlockingDeletion() throws Exception {
        try (Fixture fixture = new Fixture()) {
            Doctor doctor = fixture.registerDoctor("doctor1", "Cardiology");
            UserManagementPanel panel = fixture.newPanel();
            Object reason = invokePrivateMethod(panel, "findDependencyBlockingDeletion", new Class<?>[]{User.class}, doctor);
            assertNull(reason, "a doctor with no dependent records must be safely deletable");
        }
    }

    // ---- tests: self-protection ----

    private void testCannotSelectSelfForDeletionOrDeactivation() throws Exception {
        try (Fixture fixture = new Fixture()) {
            AdminStaff admin = fixture.registerAdmin("admin1");
            fixture.registerManager("manager1");
            fixture.sessionManager.login("admin1", "pass1234");

            UserManagementPanel panel = fixture.newPanel();
            boolean isSelf = (Boolean) invokePrivateMethod(panel, "isCurrentUser", new Class<?>[]{String.class}, admin.getUserId());
            assertTrue(isSelf, "the logged-in Admin's own ID must be recognized as the current user");

            String otherId = fixture.userManager.findByUsername("manager1").getUserId();
            boolean isOther = (Boolean) invokePrivateMethod(panel, "isCurrentUser", new Class<?>[]{String.class}, otherId);
            assertTrue(!isOther, "another user's ID must not be recognized as the current user");
        }
    }

    // ---- tests: users.txt loading hardening ----

    private void testMissingFileLoadsEmpty() throws Exception {
        try (Fixture fixture = new Fixture()) {
            File missingFile = new File(fixture.tempDir.toFile(), "does-not-exist.txt");
            UserManager freshManager = new UserManager(missingFile);
            assertTrue(freshManager.getUsers().isEmpty(), "a missing users file must load as an empty list");
        }
    }

    private void testLoadSkipsMalformedRowTooFewFields() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "TOO_FEW|ADMIN_STAFF|admin1");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertTrue(freshManager.getUsers().isEmpty(), "a row with too few fields must be skipped, not crash");
        }
    }

    private void testLoadSkipsRowWithTooManyFields() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "AS001|ADMIN_STAFF|admin1|pass1234|0111111111|a@test.com|AS001||true||EXTRA");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertTrue(freshManager.getUsers().isEmpty(), "a row with more than ten fields must be skipped, not crash");
        }
    }

    private void testLoadSkipsBlankUserId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "   |ADMIN_STAFF|admin1|pass1234|0111111111|a@test.com|AS001||true|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertTrue(freshManager.getUsers().isEmpty(), "a row with a blank user ID must be skipped, not crash");
        }
    }

    private void testLoadSkipsBlankRole() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "AS001|   |admin1|pass1234|0111111111|a@test.com|AS001||true|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertTrue(freshManager.getUsers().isEmpty(), "a row with a blank role must be skipped, not crash");
        }
    }

    private void testLoadSkipsBlankUsername() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "AS001|ADMIN_STAFF|   |pass1234|0111111111|a@test.com|AS001||true|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertTrue(freshManager.getUsers().isEmpty(), "a row with a blank username must be skipped, not crash");
        }
    }

    private void testLoadSkipsBlankPassword() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "AS001|ADMIN_STAFF|admin1|   |0111111111|a@test.com|AS001||true|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertTrue(freshManager.getUsers().isEmpty(), "a row with a blank password must be skipped, not crash");
        }
    }

    private void testLoadSkipsBlankPhone() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "AS001|ADMIN_STAFF|admin1|pass1234|   |a@test.com|AS001||true|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertTrue(freshManager.getUsers().isEmpty(), "a row with a blank phone must be skipped, not crash");
        }
    }

    private void testLoadSkipsBlankEmail() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "AS001|ADMIN_STAFF|admin1|pass1234|0111111111|   |AS001||true|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertTrue(freshManager.getUsers().isEmpty(), "a row with a blank email must be skipped, not crash");
        }
    }

    private void testLoadSkipsUnknownRole() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "XX001|SUPER_USER|admin1|pass1234|0111111111|a@test.com|XX001||true|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertTrue(freshManager.getUsers().isEmpty(), "a row with an unknown role must be skipped, not crash");
        }
    }

    private void testLoadSkipsDuplicateUserId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "AS001|ADMIN_STAFF|admin1|pass1234|0111111111|a@test.com|AS001||true|",
                    "AS001|ADMIN_STAFF|admin2|pass1234|0111111111|b@test.com|AS001||true|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertEquals(1, freshManager.getUsers().size(), "a duplicate user ID must be skipped, keeping only the first");
            assertEquals("admin1", freshManager.findById("AS001").getUsername(),
                    "the first occurrence of a duplicate ID is the one kept");
        }
    }

    private void testLoadSkipsDuplicateUsernameCaseInsensitive() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "AS001|ADMIN_STAFF|admin1|pass1234|0111111111|a@test.com|AS001||true|",
                    "AS002|ADMIN_STAFF|ADMIN1|pass1234|0111111111|b@test.com|AS002||true|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertEquals(1, freshManager.getUsers().size(),
                    "a duplicate username must be skipped case-insensitively, keeping only the first");
            assertNotNull(freshManager.findById("AS001"), "the first occurrence of a duplicate username is the one kept");
            assertNull(freshManager.findById("AS002"), "the later duplicate-username row must not load");
        }
    }

    private void testLoadSkipsInvalidActiveValue() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "AS001|ADMIN_STAFF|admin1|pass1234|0111111111|a@test.com|AS001||maybe|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertTrue(freshManager.getUsers().isEmpty(),
                    "a row with an active value other than true/false must be skipped, not crash");
        }
    }

    private void testLoadDefaultsActiveTrueWhenFieldMissing() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2",
                    "AS001|ADMIN_STAFF|admin1|pass1234|0111111111|a@test.com|AS001|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            User user = freshManager.findById("AS001");
            assertNotNull(user, "a legacy 8-field row (no active/doctorManagerId columns) must still load");
            assertTrue(user.isActive(), "a legacy row with no active column must default to active");
        }
    }

    /**
     * Reviewer-reported correction: once a ninth field exists at all, a blank value in it is
     * malformed, not "unset" - it must be skipped, not silently defaulted to true.
     */
    private void testLoadSkipsBlankActiveFieldWhenColumnPresent() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "AS001|ADMIN_STAFF|admin1|pass1234|0111111111|a@test.com|AS001|   |   |");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertTrue(freshManager.getUsers().isEmpty(),
                    "a present-but-blank active column must be skipped, not defaulted to true");
        }
    }

    private void testLoadSkipsInvalidDoctorSpecialty() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "DR001|DOCTOR|doctor1|pass1234|0111111111|d@test.com|DR001|Wizardry|true|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertTrue(freshManager.getUsers().isEmpty(), "a doctor row with an invalid specialty must be skipped, not crash");
        }
    }

    private void testLoadSkipsBlankPatientDateOfBirth() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "PT001|PATIENT|patient1|pass1234|0111111111|p@test.com|PT001||true|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertTrue(freshManager.getUsers().isEmpty(), "a patient row with a blank date of birth must be skipped, not crash");
        }
    }

    private void testLoadSkipsInvalidPatientDateOfBirth() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "PT001|PATIENT|patient1|pass1234|0111111111|p@test.com|PT001|2026-02-30|true|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertTrue(freshManager.getUsers().isEmpty(),
                    "a patient row with an impossible calendar date must be skipped, not silently rolled over");
        }
    }

    /** Reviewer-reported correction: DateFormat.parse() accepts a valid prefix with trailing junk unless guarded. */
    private void testLoadSkipsPatientDateOfBirthWithTrailingCharacters() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "PT001|PATIENT|patient1|pass1234|0111111111|p@test.com|PT001|2000-01-01junk|true|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertTrue(freshManager.getUsers().isEmpty(),
                    "a date of birth with trailing characters must be skipped, not truncated and accepted");
        }
    }

    /** Reviewer-reported correction: a non-canonical shape (single-digit month/day) must not be accepted. */
    private void testLoadSkipsNonCanonicalPatientDateOfBirth() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "PT001|PATIENT|patient1|pass1234|0111111111|p@test.com|PT001|2000-1-1|true|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            assertTrue(freshManager.getUsers().isEmpty(),
                    "a non-canonical date shape (single-digit month/day) must be skipped");
        }
    }

    private void testAdminDateParsingRejectsTrailingCharacters() throws Exception {
        try (Fixture fixture = new Fixture()) {
            UserManagementPanel panel = fixture.newPanel();
            assertThrows(IllegalArgumentException.class,
                    () -> invokePrivateMethod(panel, "parseDateOfBirth", new Class<?>[]{String.class}, "2000-01-01junk"),
                    "Admin registration/edit date parsing must reject trailing characters after a valid prefix");
        }
    }

    private void testAdminDateParsingRejectsNonCanonicalForm() throws Exception {
        try (Fixture fixture = new Fixture()) {
            UserManagementPanel panel = fixture.newPanel();
            assertThrows(IllegalArgumentException.class,
                    () -> invokePrivateMethod(panel, "parseDateOfBirth", new Class<?>[]{String.class}, "2000-1-1"),
                    "Admin registration/edit date parsing must reject a non-canonical date shape");
        }
    }

    private void testLoadSkipsDoctorManagerLinkToNonManagerUser() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "DR001|DOCTOR|doctor1|pass1234|0111111111|d@test.com|DR001|Cardiology|true|PT001",
                    "PT001|PATIENT|patient1|pass1234|0122222222|p@test.com|PT001|1990-01-01|true|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            Doctor doctor = freshManager.findDoctorById("DR001");
            assertNotNull(doctor, "the doctor row itself must still load");
            assertNull(doctor.getShiftManager(),
                    "a doctorManagerId pointing at a non-Medical-Manager user must not be resolved");
        }
    }

    /** Reviewer-required Step 5 coverage: a link to a manager ID that does not exist at all. */
    private void testLoadSkipsDoctorManagerLinkToUnknownManagerId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "DR001|DOCTOR|doctor1|pass1234|0111111111|d@test.com|DR001|Cardiology|true|MM999");
            UserManager freshManager = new UserManager(fixture.usersFile);
            Doctor doctor = freshManager.findDoctorById("DR001");
            assertNotNull(doctor, "the doctor row itself must still load");
            assertNull(doctor.getShiftManager(),
                    "a doctorManagerId pointing at a manager ID that does not exist must not crash and must leave the doctor unassigned");
        }
    }

    /** Reviewer-required Step 5 coverage: a relationship to an inactive manager survives loading unchanged. */
    private void testLoadResolvesLinkToInactiveManager() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "DR001|DOCTOR|doctor1|pass1234|0111111111|d@test.com|DR001|Cardiology|true|MM001",
                    "MM001|MEDICAL_MANAGER|manager1|pass1234|0122222222|m@test.com|MM001||false|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            Doctor doctor = freshManager.findDoctorById("DR001");
            assertNotNull(doctor.getShiftManager(),
                    "a persisted relationship to a manager must survive loading even if that manager is inactive - "
                            + "deactivation must never silently remove an existing assignment");
        }
    }

    private void testLoadResolvesDoctorManagerLinkRegardlessOfRowOrder() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "DR001|DOCTOR|doctor1|pass1234|0111111111|d@test.com|DR001|Cardiology|true|MM001",
                    "MM001|MEDICAL_MANAGER|manager1|pass1234|0122222222|m@test.com|MM001||true|");
            UserManager freshManager = new UserManager(fixture.usersFile);
            Doctor doctor = freshManager.findDoctorById("DR001");
            MedicalManager manager = freshManager.findMedicalManagerById("MM001");
            assertNotNull(doctor.getShiftManager(), "the doctor-manager link must resolve even though the manager's row comes after the doctor's");
            assertTrue(manager.getManagedDoctors().contains(doctor), "the manager must list the doctor after loading");
        }
    }

    private void testValidLegacyRecordWithoutTrailingColumnsStillLoads() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2",
                    "PT001|PATIENT|patient1|pass1234|0111111111|p@test.com|PT001|1995-06-15");
            UserManager freshManager = new UserManager(fixture.usersFile);
            Patient patient = freshManager.findPatientById("PT001");
            assertNotNull(patient, "a valid legacy 8-field patient record must still load");
            assertEquals(dob("1995-06-15"), patient.getDateOfBirth(), "the loaded date of birth must match the file");
            assertTrue(patient.isActive(), "a legacy row with no active column must default to active");
        }
    }

    // ---- tests: GUI checks ----

    private void testUserTableDisplaysRequiredColumns() throws Exception {
        try (Fixture fixture = new Fixture()) {
            UserManagementPanel panel = fixture.newPanel();
            JTable userTable = (JTable) getPrivateField(panel, "userTable");
            TableModel model = userTable.getModel();
            List<String> columns = new ArrayList<>();
            for (int i = 0; i < model.getColumnCount(); i++) {
                columns.add(model.getColumnName(i));
            }
            assertTrue(columns.contains("ID"), "user table must show the user ID");
            assertTrue(columns.contains("Role"), "user table must show the role");
            assertTrue(columns.contains("Username"), "user table must show the username");
            assertTrue(columns.contains("Phone"), "user table must show the phone");
            assertTrue(columns.contains("Email"), "user table must show the email");
            assertTrue(columns.contains("Active"), "user table must show the active status");
        }
    }

    private void testUserManagementButtonsHaveActionListeners() throws Exception {
        try (Fixture fixture = new Fixture()) {
            UserManagementPanel panel = fixture.newPanel();
            String[] buttonFields = {
                    "registerButton", "editButton", "resetPasswordButton", "toggleStatusButton",
                    "deleteButton", "backButton", "searchButton", "clearButton"
            };
            for (String fieldName : buttonFields) {
                JButton button = (JButton) getPrivateField(panel, fieldName);
                assertNotNull(button, fieldName + " must exist");
                assertTrue(button.getActionListeners().length > 0, fieldName + " must have an action listener wired");
            }
        }
    }

    // ---- tests: DoctorManagementPanel (Step 5) ----

    private void testDoctorManagementTableDisplaysRequiredColumns() throws Exception {
        try (Fixture fixture = new Fixture()) {
            DoctorManagementPanel panel = fixture.newDoctorPanel();
            JTable doctorTable = (JTable) getPrivateField(panel, "doctorTable");
            TableModel model = doctorTable.getModel();
            List<String> columns = new ArrayList<>();
            for (int i = 0; i < model.getColumnCount(); i++) {
                columns.add(model.getColumnName(i));
            }
            assertTrue(columns.contains("ID"), "doctor table must show the doctor ID");
            assertTrue(columns.contains("Assigned Manager"), "doctor table must show the assigned manager");
            assertTrue(columns.contains("Active"), "doctor table must show active status");
        }
    }

    private void testDoctorManagementButtonsHaveActionListeners() throws Exception {
        try (Fixture fixture = new Fixture()) {
            DoctorManagementPanel panel = fixture.newDoctorPanel();
            String[] buttonFields = {
                    "viewButton", "assignManagerButton", "unassignManagerButton",
                    "updateSpecialtyButton", "backButton", "searchButton", "clearButton"
            };
            for (String fieldName : buttonFields) {
                JButton button = (JButton) getPrivateField(panel, fieldName);
                assertNotNull(button, fieldName + " must exist");
                assertTrue(button.getActionListeners().length > 0, fieldName + " must have an action listener wired");
            }
        }
    }

    private void testDoctorSearchMatchesIdUsernameSpecialtyManagerAndAssignedState() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalManager manager = fixture.registerManager("managerOne");
            Doctor assignedDoctor = fixture.registerDoctor("houseMd", "Cardiology");
            Doctor unassignedDoctor = fixture.registerDoctor("wilsonMd", "Neurology");
            fixture.userManager.assignDoctorToManager(assignedDoctor.getDoctorId(), manager.getManagerId());

            DoctorManagementPanel panel = fixture.newDoctorPanel();
            JTextField searchField = (JTextField) getPrivateField(panel, "searchField");
            DefaultTableModel tableModel = (DefaultTableModel) getPrivateField(panel, "tableModel");

            searchField.setText(assignedDoctor.getDoctorId().toLowerCase());
            invokePrivateMethod(panel, "applySearch", new Class<?>[0]);
            assertEquals(1, tableModel.getRowCount(), "searching by Doctor ID must find the matching doctor");

            searchField.setText("cardiology");
            invokePrivateMethod(panel, "applySearch", new Class<?>[0]);
            assertEquals(1, tableModel.getRowCount(), "searching by specialty must find the matching doctor");

            searchField.setText("managerone");
            invokePrivateMethod(panel, "applySearch", new Class<?>[0]);
            assertEquals(1, tableModel.getRowCount(), "searching by the assigned manager's username must find that doctor");

            searchField.setText(manager.getManagerId().toLowerCase());
            invokePrivateMethod(panel, "applySearch", new Class<?>[0]);
            assertEquals(1, tableModel.getRowCount(), "searching by the assigned manager's ID must find that doctor");

            searchField.setText("unassigned");
            invokePrivateMethod(panel, "applySearch", new Class<?>[0]);
            assertEquals(1, tableModel.getRowCount(), "searching for \"unassigned\" must find only the unassigned doctor");
        }
    }

    private void testAssignmentDialogOffersOnlyActiveManagers() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalManager activeManager = fixture.registerManager("activeManager");
            MedicalManager inactiveManager = fixture.registerManager("inactiveManager");
            fixture.userManager.deactivateUser(inactiveManager.getUserId());

            DoctorManagementPanel panel = fixture.newDoctorPanel();
            @SuppressWarnings("unchecked")
            List<MedicalManager> activeManagers =
                    (List<MedicalManager>) invokePrivateMethod(panel, "getActiveMedicalManagers", new Class<?>[0]);
            assertTrue(activeManagers.contains(activeManager), "an active Medical Manager must be offered");
            assertTrue(!activeManagers.contains(inactiveManager), "an inactive Medical Manager must not be offered");
        }
    }
}
