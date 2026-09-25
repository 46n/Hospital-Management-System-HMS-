package hms.manager;

import hms.model.AdminStaff;
import hms.model.Doctor;
import hms.model.InsuranceNetwork;
import hms.ui.admin.InsuranceNetworkManagementPanel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * Standalone, dependency-free Step 7 tests for InsuranceNetworkManager (CRUD,
 * validation, duplicate provider+plan handling, and hardened
 * insurance_networks.txt loading) and the headless-safe checks on
 * InsuranceNetworkManagementPanel. Same hand-rolled runner style as the
 * other Step test files, since the project has no JUnit on its classpath.
 * Every test works against its own temporary directory (never the real
 * data/ directory) and deletes it afterward.
 *
 * Run with (from the project root, after compiling src to build/classes):
 *   javac -d build/test-classes -cp build/classes test/HMS/manager/InsuranceNetworkManagerTest.java
 *   java -cp build/classes;build/test-classes hms.manager.InsuranceNetworkManagerTest
 */
public class InsuranceNetworkManagerTest {

    private int passed = 0;
    private int failed = 0;

    public static void main(String[] args) throws Exception {
        InsuranceNetworkManagerTest suite = new InsuranceNetworkManagerTest();

        // ---- CRUD ----
        suite.run("testCreateNetwork", suite::testCreateNetwork);
        suite.run("testUpdateNetwork", suite::testUpdateNetwork);
        suite.run("testToggleActive", suite::testToggleActive);
        suite.run("testDeleteNetwork", suite::testDeleteNetwork);
        suite.run("testPersistenceSurvivesReload", suite::testPersistenceSurvivesReload);

        // ---- validation ----
        suite.run("testCreateTrimsProviderAndPlan", suite::testCreateTrimsProviderAndPlan);
        suite.run("testUpdateTrimsProviderAndPlan", suite::testUpdateTrimsProviderAndPlan);
        suite.run("testCreateRejectsBlankProvider", suite::testCreateRejectsBlankProvider);
        suite.run("testCreateRejectsBlankPlan", suite::testCreateRejectsBlankPlan);
        suite.run("testCreateAcceptsCoverageBoundaryZero", suite::testCreateAcceptsCoverageBoundaryZero);
        suite.run("testCreateAcceptsCoverageBoundaryHundred", suite::testCreateAcceptsCoverageBoundaryHundred);
        suite.run("testCreateRejectsNegativeCoverage", suite::testCreateRejectsNegativeCoverage);
        suite.run("testCreateRejectsCoverageAboveHundred", suite::testCreateRejectsCoverageAboveHundred);
        suite.run("testCreateRejectsNaNCoverage", suite::testCreateRejectsNaNCoverage);
        suite.run("testCreateRejectsInfiniteCoverage", suite::testCreateRejectsInfiniteCoverage);
        suite.run("testCreateRejectsUnknownManagingUser", suite::testCreateRejectsUnknownManagingUser);
        suite.run("testCreateRejectsNonAdminManagingUser", suite::testCreateRejectsNonAdminManagingUser);
        suite.run("testCreateRejectsDuplicateProviderPlanCaseInsensitive",
                suite::testCreateRejectsDuplicateProviderPlanCaseInsensitive);
        suite.run("testUpdateRejectsDuplicateProviderPlanFromAnotherNetwork",
                suite::testUpdateRejectsDuplicateProviderPlanFromAnotherNetwork);
        suite.run("testUpdateMayKeepOwnProviderPlan", suite::testUpdateMayKeepOwnProviderPlan);
        suite.run("testInvalidUpdateLeavesOriginalValuesUnchanged", suite::testInvalidUpdateLeavesOriginalValuesUnchanged);

        // ---- loading hardening ----
        suite.run("testMissingFileLoadsEmpty", suite::testMissingFileLoadsEmpty);
        suite.run("testLoadSkipsMalformedRowTooFewFields", suite::testLoadSkipsMalformedRowTooFewFields);
        suite.run("testLoadSkipsRowWithTooManyFields", suite::testLoadSkipsRowWithTooManyFields);
        suite.run("testLoadSkipsBlankNetworkId", suite::testLoadSkipsBlankNetworkId);
        suite.run("testLoadSkipsBlankProviderName", suite::testLoadSkipsBlankProviderName);
        suite.run("testLoadSkipsBlankPlanName", suite::testLoadSkipsBlankPlanName);
        suite.run("testLoadSkipsDuplicateNetworkId", suite::testLoadSkipsDuplicateNetworkId);
        suite.run("testLoadSkipsDuplicateProviderPlanCombination", suite::testLoadSkipsDuplicateProviderPlanCombination);
        suite.run("testLoadSkipsNonNumericCoverage", suite::testLoadSkipsNonNumericCoverage);
        suite.run("testLoadSkipsNaNCoverage", suite::testLoadSkipsNaNCoverage);
        suite.run("testLoadSkipsNegativeCoverage", suite::testLoadSkipsNegativeCoverage);
        suite.run("testLoadSkipsCoverageAboveHundred", suite::testLoadSkipsCoverageAboveHundred);
        suite.run("testLoadSkipsInvalidActiveValue", suite::testLoadSkipsInvalidActiveValue);
        suite.run("testLoadSkipsUnknownManagedByStaffId", suite::testLoadSkipsUnknownManagedByStaffId);
        suite.run("testLoadSkipsNonAdminManagedByStaffId", suite::testLoadSkipsNonAdminManagedByStaffId);
        suite.run("testLoadKeepsEmptyManagedByForLegacyRows", suite::testLoadKeepsEmptyManagedByForLegacyRows);
        suite.run("testValidRecordLoads", suite::testValidRecordLoads);

        // ---- GUI checks ----
        suite.run("testNetworkTableDisplaysRequiredColumns", suite::testNetworkTableDisplaysRequiredColumns);
        suite.run("testNetworkManagementButtonsHaveActionListeners", suite::testNetworkManagementButtonsHaveActionListeners);

        // ---- persistence failure ----
        suite.run("testCreateNetworkWriteFailureLeavesNoPhantom", suite::testCreateNetworkWriteFailureLeavesNoPhantom);
        suite.run("testUpdateNetworkWriteFailureRestoresOriginalFields",
                suite::testUpdateNetworkWriteFailureRestoresOriginalFields);
        suite.run("testSetActiveWriteFailureRestoresPreviousValue", suite::testSetActiveWriteFailureRestoresPreviousValue);
        suite.run("testDeleteNetworkWriteFailureRestoresRecord", suite::testDeleteNetworkWriteFailureRestoresRecord);
        suite.run("testExistingValidNetworkFileUnchangedAfterFailedWrite",
                suite::testExistingValidNetworkFileUnchangedAfterFailedWrite);
        suite.run("testUnreadableNetworkFileIsReportedAsLoadFailure", suite::testUnreadableNetworkFileIsReportedAsLoadFailure);

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

    /** A full, wired-together environment, backed only by temporary files - never data/. */
    private static final class Fixture implements AutoCloseable {
        final Path tempDir;
        final File usersFile;
        final File insuranceFile;
        final UserManager userManager;
        final AppointmentManager appointmentManager;
        final ClinicalManager clinicalManager;
        final HospitalAssetManager hospitalAssetManager;
        final AssetAllocationManager allocationManager;
        final ConsultationRateManager rateManager;
        final InsuranceNetworkManager insuranceManager;
        final SessionManager sessionManager;
        final AdminStaff admin;

        Fixture() throws IOException {
            tempDir = Files.createTempDirectory("hms-insurancenetwork-test-");
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
            insuranceFile = new File(tempDir.toFile(), "insurance_networks.txt");

            userManager = new UserManager(usersFile);
            DoctorShiftManager shiftManager = new DoctorShiftManager(userManager, shiftsFile);
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

            admin = (AdminStaff) userManager.registerUser(
                    new AdminStaff(null, "admin1", "pass1234", "0111111111", "admin1@test.com", "ADMIN_STAFF", null));
        }

        InsuranceNetworkManagementPanel newPanel() {
            return new InsuranceNetworkManagementPanel(insuranceManager, userManager, appointmentManager,
                    clinicalManager, hospitalAssetManager, rateManager, allocationManager, sessionManager, null,
                    () -> { });
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

    // ---- tests: CRUD ----

    private void testCreateNetwork() throws Exception {
        try (Fixture fixture = new Fixture()) {
            InsuranceNetwork network = fixture.insuranceManager.createNetwork(
                    "HealthCo", "Gold Plan", 80.0, fixture.admin.getStaffId());
            assertEquals("HealthCo", network.getProviderName(), "provider name must be stored");
            assertEquals("Gold Plan", network.getPlanName(), "plan name must be stored");
            assertEquals(80.0, network.getCoveragePercentage(), "coverage percentage must be stored");
            assertTrue(network.isActive(), "a newly created network must start active");
            assertEquals(fixture.admin.getStaffId(), network.getManagedBy().getStaffId(), "managing admin must be recorded");
        }
    }

    private void testUpdateNetwork() throws Exception {
        try (Fixture fixture = new Fixture()) {
            InsuranceNetwork network = fixture.insuranceManager.createNetwork(
                    "HealthCo", "Gold Plan", 80.0, fixture.admin.getStaffId());
            fixture.insuranceManager.updateNetwork(network.getNetworkId(), "MediCare Plus", "Silver Plan", 60.0);
            assertEquals("MediCare Plus", network.getProviderName(), "provider name must be updated");
            assertEquals("Silver Plan", network.getPlanName(), "plan name must be updated");
            assertEquals(60.0, network.getCoveragePercentage(), "coverage percentage must be updated");
        }
    }

    private void testToggleActive() throws Exception {
        try (Fixture fixture = new Fixture()) {
            InsuranceNetwork network = fixture.insuranceManager.createNetwork(
                    "HealthCo", "Gold Plan", 80.0, fixture.admin.getStaffId());
            fixture.insuranceManager.setActive(network.getNetworkId(), false);
            assertTrue(!network.isActive(), "the network must be deactivated");
            fixture.insuranceManager.setActive(network.getNetworkId(), true);
            assertTrue(network.isActive(), "the network must be reactivated");
        }
    }

    private void testDeleteNetwork() throws Exception {
        try (Fixture fixture = new Fixture()) {
            InsuranceNetwork network = fixture.insuranceManager.createNetwork(
                    "HealthCo", "Gold Plan", 80.0, fixture.admin.getStaffId());
            fixture.insuranceManager.deleteNetwork(network.getNetworkId());
            assertNull(fixture.insuranceManager.findById(network.getNetworkId()), "the network must actually be deleted");
        }
    }

    private void testPersistenceSurvivesReload() throws Exception {
        try (Fixture fixture = new Fixture()) {
            InsuranceNetwork network = fixture.insuranceManager.createNetwork(
                    "HealthCo", "Gold Plan", 80.0, fixture.admin.getStaffId());
            InsuranceNetworkManager reloaded = new InsuranceNetworkManager(fixture.userManager, fixture.insuranceFile);
            InsuranceNetwork reloadedNetwork = reloaded.findById(network.getNetworkId());
            assertNotNull(reloadedNetwork, "the network must reload");
            assertEquals("HealthCo", reloadedNetwork.getProviderName(), "provider name must survive reload");
            assertEquals("Gold Plan", reloadedNetwork.getPlanName(), "plan name must survive reload");
            assertEquals(80.0, reloadedNetwork.getCoveragePercentage(), "coverage percentage must survive reload");
            assertTrue(reloadedNetwork.isActive(), "active status must survive reload");
            assertEquals(fixture.admin.getStaffId(), reloadedNetwork.getManagedBy().getStaffId(),
                    "managing admin must survive reload");
        }
    }

    // ---- tests: validation ----

    private void testCreateTrimsProviderAndPlan() throws Exception {
        try (Fixture fixture = new Fixture()) {
            InsuranceNetwork network = fixture.insuranceManager.createNetwork(
                    "  HealthCo  ", "  Gold Plan  ", 80.0, fixture.admin.getStaffId());
            assertEquals("HealthCo", network.getProviderName(), "provider name must be stored trimmed");
            assertEquals("Gold Plan", network.getPlanName(), "plan name must be stored trimmed");
        }
    }

    private void testUpdateTrimsProviderAndPlan() throws Exception {
        try (Fixture fixture = new Fixture()) {
            InsuranceNetwork network = fixture.insuranceManager.createNetwork(
                    "HealthCo", "Gold Plan", 80.0, fixture.admin.getStaffId());
            fixture.insuranceManager.updateNetwork(network.getNetworkId(), "  MediCare Plus  ", "  Silver Plan  ", 60.0);
            assertEquals("MediCare Plus", network.getProviderName(), "an updated provider name must be stored trimmed");
            assertEquals("Silver Plan", network.getPlanName(), "an updated plan name must be stored trimmed");
        }
    }

    private void testCreateRejectsBlankProvider() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.insuranceManager.createNetwork("   ", "Gold Plan", 80.0, fixture.admin.getStaffId()),
                    "a blank provider name must be rejected");
        }
    }

    private void testCreateRejectsBlankPlan() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.insuranceManager.createNetwork("HealthCo", "   ", 80.0, fixture.admin.getStaffId()),
                    "a blank plan name must be rejected");
        }
    }

    private void testCreateAcceptsCoverageBoundaryZero() throws Exception {
        try (Fixture fixture = new Fixture()) {
            InsuranceNetwork network = fixture.insuranceManager.createNetwork(
                    "HealthCo", "Gold Plan", 0.0, fixture.admin.getStaffId());
            assertEquals(0.0, network.getCoveragePercentage(), "0 must be accepted as a valid coverage percentage");
        }
    }

    private void testCreateAcceptsCoverageBoundaryHundred() throws Exception {
        try (Fixture fixture = new Fixture()) {
            InsuranceNetwork network = fixture.insuranceManager.createNetwork(
                    "HealthCo", "Gold Plan", 100.0, fixture.admin.getStaffId());
            assertEquals(100.0, network.getCoveragePercentage(), "100 must be accepted as a valid coverage percentage");
        }
    }

    private void testCreateRejectsNegativeCoverage() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.insuranceManager.createNetwork("HealthCo", "Gold Plan", -1.0, fixture.admin.getStaffId()),
                    "a negative coverage percentage must be rejected");
        }
    }

    private void testCreateRejectsCoverageAboveHundred() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.insuranceManager.createNetwork(
                            "HealthCo", "Gold Plan", 100.1, fixture.admin.getStaffId()),
                    "a coverage percentage above 100 must be rejected");
        }
    }

    private void testCreateRejectsNaNCoverage() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.insuranceManager.createNetwork(
                            "HealthCo", "Gold Plan", Double.NaN, fixture.admin.getStaffId()),
                    "a NaN coverage percentage must be rejected");
        }
    }

    private void testCreateRejectsInfiniteCoverage() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.insuranceManager.createNetwork(
                            "HealthCo", "Gold Plan", Double.POSITIVE_INFINITY, fixture.admin.getStaffId()),
                    "positive infinity must be rejected");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.insuranceManager.createNetwork(
                            "HealthCo", "Gold Plan", Double.NEGATIVE_INFINITY, fixture.admin.getStaffId()),
                    "negative infinity must be rejected");
        }
    }

    private void testCreateRejectsUnknownManagingUser() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.insuranceManager.createNetwork("HealthCo", "Gold Plan", 80.0, "UNKNOWN999"),
                    "an unknown managing user ID must be rejected");
        }
    }

    private void testCreateRejectsNonAdminManagingUser() throws Exception {
        try (Fixture fixture = new Fixture()) {
            Doctor doctor = (Doctor) fixture.userManager.registerUser(
                    new Doctor(null, "doctor1", "pass1234", "0122222222", "doctor1@test.com", "DOCTOR", null, "Cardiology"));
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.insuranceManager.createNetwork("HealthCo", "Gold Plan", 80.0, doctor.getUserId()),
                    "a Doctor ID must not be accepted as the managing Admin Staff");
        }
    }

    private void testCreateRejectsDuplicateProviderPlanCaseInsensitive() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.insuranceManager.createNetwork("HealthCo", "Gold Plan", 80.0, fixture.admin.getStaffId());
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.insuranceManager.createNetwork(
                            "  healthco  ", "  GOLD PLAN  ", 50.0, fixture.admin.getStaffId()),
                    "a duplicate provider+plan combination must be rejected regardless of case or surrounding spaces");
        }
    }

    private void testUpdateRejectsDuplicateProviderPlanFromAnotherNetwork() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.insuranceManager.createNetwork("HealthCo", "Gold Plan", 80.0, fixture.admin.getStaffId());
            InsuranceNetwork second = fixture.insuranceManager.createNetwork(
                    "MediCare Plus", "Silver Plan", 60.0, fixture.admin.getStaffId());
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.insuranceManager.updateNetwork(second.getNetworkId(), "HealthCo", "Gold Plan", 60.0),
                    "renaming to another network's provider+plan combination must be rejected");
        }
    }

    private void testUpdateMayKeepOwnProviderPlan() throws Exception {
        try (Fixture fixture = new Fixture()) {
            InsuranceNetwork network = fixture.insuranceManager.createNetwork(
                    "HealthCo", "Gold Plan", 80.0, fixture.admin.getStaffId());
            fixture.insuranceManager.updateNetwork(network.getNetworkId(), "HealthCo", "Gold Plan", 90.0);
            assertEquals(90.0, network.getCoveragePercentage(),
                    "a network may keep its own provider+plan combination while updating other fields");
        }
    }

    private void testInvalidUpdateLeavesOriginalValuesUnchanged() throws Exception {
        try (Fixture fixture = new Fixture()) {
            InsuranceNetwork network = fixture.insuranceManager.createNetwork(
                    "HealthCo", "Gold Plan", 80.0, fixture.admin.getStaffId());
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.insuranceManager.updateNetwork(network.getNetworkId(), "   ", "Silver Plan", 60.0),
                    "an update with a blank provider name must be rejected");
            assertEquals("HealthCo", network.getProviderName(), "provider name must be unchanged after a failed update");
            assertEquals("Gold Plan", network.getPlanName(), "plan name must be unchanged after a failed update");
            assertEquals(80.0, network.getCoveragePercentage(), "coverage percentage must be unchanged after a failed update");
        }
    }

    // ---- tests: loading hardening ----

    private void testMissingFileLoadsEmpty() throws Exception {
        try (Fixture fixture = new Fixture()) {
            File missingFile = new File(fixture.tempDir.toFile(), "does-not-exist.txt");
            InsuranceNetworkManager freshManager = new InsuranceNetworkManager(fixture.userManager, missingFile);
            assertTrue(freshManager.getNetworks().isEmpty(), "a missing insurance networks file must load as an empty list");
        }
    }

    private void testLoadSkipsMalformedRowTooFewFields() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.insuranceFile,
                    "#networkId|providerName|planName|coveragePercentage|active|managedByStaffId",
                    "TOO_FEW|HealthCo|Gold Plan");
            InsuranceNetworkManager freshManager = new InsuranceNetworkManager(fixture.userManager, fixture.insuranceFile);
            assertTrue(freshManager.getNetworks().isEmpty(), "a row with too few fields must be skipped, not crash");
        }
    }

    private void testLoadSkipsRowWithTooManyFields() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.insuranceFile,
                    "#networkId|providerName|planName|coveragePercentage|active|managedByStaffId",
                    "IN001|HealthCo|Gold Plan|80.0|true||EXTRA");
            InsuranceNetworkManager freshManager = new InsuranceNetworkManager(fixture.userManager, fixture.insuranceFile);
            assertTrue(freshManager.getNetworks().isEmpty(), "a row with more than six fields must be skipped, not crash");
        }
    }

    private void testLoadSkipsBlankNetworkId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.insuranceFile,
                    "#networkId|providerName|planName|coveragePercentage|active|managedByStaffId",
                    "   |HealthCo|Gold Plan|80.0|true|");
            InsuranceNetworkManager freshManager = new InsuranceNetworkManager(fixture.userManager, fixture.insuranceFile);
            assertTrue(freshManager.getNetworks().isEmpty(), "a row with a blank network ID must be skipped, not crash");
        }
    }

    private void testLoadSkipsBlankProviderName() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.insuranceFile,
                    "#networkId|providerName|planName|coveragePercentage|active|managedByStaffId",
                    "IN001|   |Gold Plan|80.0|true|");
            InsuranceNetworkManager freshManager = new InsuranceNetworkManager(fixture.userManager, fixture.insuranceFile);
            assertTrue(freshManager.getNetworks().isEmpty(), "a row with a blank provider name must be skipped, not crash");
        }
    }

    private void testLoadSkipsBlankPlanName() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.insuranceFile,
                    "#networkId|providerName|planName|coveragePercentage|active|managedByStaffId",
                    "IN001|HealthCo|   |80.0|true|");
            InsuranceNetworkManager freshManager = new InsuranceNetworkManager(fixture.userManager, fixture.insuranceFile);
            assertTrue(freshManager.getNetworks().isEmpty(), "a row with a blank plan name must be skipped, not crash");
        }
    }

    private void testLoadSkipsDuplicateNetworkId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.insuranceFile,
                    "#networkId|providerName|planName|coveragePercentage|active|managedByStaffId",
                    "IN001|HealthCo|Gold Plan|80.0|true|",
                    "IN001|MediCare Plus|Silver Plan|60.0|true|");
            InsuranceNetworkManager freshManager = new InsuranceNetworkManager(fixture.userManager, fixture.insuranceFile);
            assertEquals(1, freshManager.getNetworks().size(), "a duplicate network ID must be skipped, keeping only the first");
            assertEquals("HealthCo", freshManager.findById("IN001").getProviderName(),
                    "the first occurrence of a duplicate ID is the one kept");
        }
    }

    private void testLoadSkipsDuplicateProviderPlanCombination() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.insuranceFile,
                    "#networkId|providerName|planName|coveragePercentage|active|managedByStaffId",
                    "IN001|HealthCo|Gold Plan|80.0|true|",
                    "IN002|healthco|GOLD PLAN|50.0|true|");
            InsuranceNetworkManager freshManager = new InsuranceNetworkManager(fixture.userManager, fixture.insuranceFile);
            assertEquals(1, freshManager.getNetworks().size(),
                    "a duplicate provider+plan combination must be skipped case-insensitively, keeping only the first");
            assertNotNull(freshManager.findById("IN001"), "the first occurrence of a duplicate combination is the one kept");
            assertNull(freshManager.findById("IN002"), "the later duplicate-combination row must not load");
        }
    }

    private void testLoadSkipsNonNumericCoverage() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.insuranceFile,
                    "#networkId|providerName|planName|coveragePercentage|active|managedByStaffId",
                    "IN001|HealthCo|Gold Plan|not-a-number|true|");
            InsuranceNetworkManager freshManager = new InsuranceNetworkManager(fixture.userManager, fixture.insuranceFile);
            assertTrue(freshManager.getNetworks().isEmpty(), "a row with a non-numeric coverage percentage must be skipped, not crash");
        }
    }

    private void testLoadSkipsNaNCoverage() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.insuranceFile,
                    "#networkId|providerName|planName|coveragePercentage|active|managedByStaffId",
                    "IN001|HealthCo|Gold Plan|NaN|true|");
            InsuranceNetworkManager freshManager = new InsuranceNetworkManager(fixture.userManager, fixture.insuranceFile);
            assertTrue(freshManager.getNetworks().isEmpty(),
                    "a row whose coverage parses to NaN must be skipped - Double.parseDouble(\"NaN\") does not throw");
        }
    }

    private void testLoadSkipsNegativeCoverage() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.insuranceFile,
                    "#networkId|providerName|planName|coveragePercentage|active|managedByStaffId",
                    "IN001|HealthCo|Gold Plan|-5.0|true|");
            InsuranceNetworkManager freshManager = new InsuranceNetworkManager(fixture.userManager, fixture.insuranceFile);
            assertTrue(freshManager.getNetworks().isEmpty(), "a row with a negative coverage percentage must be skipped, not crash");
        }
    }

    private void testLoadSkipsCoverageAboveHundred() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.insuranceFile,
                    "#networkId|providerName|planName|coveragePercentage|active|managedByStaffId",
                    "IN001|HealthCo|Gold Plan|150.0|true|");
            InsuranceNetworkManager freshManager = new InsuranceNetworkManager(fixture.userManager, fixture.insuranceFile);
            assertTrue(freshManager.getNetworks().isEmpty(),
                    "a row with a coverage percentage above 100 must be skipped, not crash");
        }
    }

    private void testLoadSkipsInvalidActiveValue() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.insuranceFile,
                    "#networkId|providerName|planName|coveragePercentage|active|managedByStaffId",
                    "IN001|HealthCo|Gold Plan|80.0|maybe|");
            InsuranceNetworkManager freshManager = new InsuranceNetworkManager(fixture.userManager, fixture.insuranceFile);
            assertTrue(freshManager.getNetworks().isEmpty(),
                    "a row with an active value other than true/false must be skipped, not crash");
        }
    }

    private void testLoadSkipsUnknownManagedByStaffId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.insuranceFile,
                    "#networkId|providerName|planName|coveragePercentage|active|managedByStaffId",
                    "IN001|HealthCo|Gold Plan|80.0|true|UNKNOWN999");
            InsuranceNetworkManager freshManager = new InsuranceNetworkManager(fixture.userManager, fixture.insuranceFile);
            assertTrue(freshManager.getNetworks().isEmpty(),
                    "a row naming an unknown managing staff ID must be skipped, not loaded without its manager");
        }
    }

    private void testLoadSkipsNonAdminManagedByStaffId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.userManager.registerUser(
                    new Doctor(null, "doctor1", "pass1234", "0122222222", "doctor1@test.com", "DOCTOR", null, "Cardiology"));
            Doctor doctor = fixture.userManager.findDoctorById(
                    fixture.userManager.findByUsername("doctor1").getUserId());
            writeFile(fixture.insuranceFile,
                    "#networkId|providerName|planName|coveragePercentage|active|managedByStaffId",
                    "IN001|HealthCo|Gold Plan|80.0|true|" + doctor.getDoctorId());
            InsuranceNetworkManager freshManager = new InsuranceNetworkManager(fixture.userManager, fixture.insuranceFile);
            assertTrue(freshManager.getNetworks().isEmpty(),
                    "a row naming a Doctor as the managing staff ID must be skipped");
        }
    }

    private void testLoadKeepsEmptyManagedByForLegacyRows() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.insuranceFile,
                    "#networkId|providerName|planName|coveragePercentage|active|managedByStaffId",
                    "IN001|HealthCo|Gold Plan|80.0|true|");
            InsuranceNetworkManager freshManager = new InsuranceNetworkManager(fixture.userManager, fixture.insuranceFile);
            InsuranceNetwork network = freshManager.findById("IN001");
            assertNotNull(network, "a legacy row with an empty managedByStaffId must still load");
            assertNull(network.getManagedBy(), "an empty managedByStaffId must leave managedBy unset, not crash");
        }
    }

    private void testValidRecordLoads() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.insuranceFile,
                    "#networkId|providerName|planName|coveragePercentage|active|managedByStaffId",
                    "IN001|HealthCo|Gold Plan|80.0|true|" + fixture.admin.getStaffId());
            InsuranceNetworkManager freshManager = new InsuranceNetworkManager(fixture.userManager, fixture.insuranceFile);
            InsuranceNetwork network = freshManager.findById("IN001");
            assertNotNull(network, "a valid record must load");
            assertEquals("HealthCo", network.getProviderName(), "loaded provider name matches the file");
            assertEquals("Gold Plan", network.getPlanName(), "loaded plan name matches the file");
            assertEquals(80.0, network.getCoveragePercentage(), "loaded coverage percentage matches the file");
            assertTrue(network.isActive(), "loaded active status matches the file");
            assertEquals(fixture.admin.getStaffId(), network.getManagedBy().getStaffId(), "loaded managing admin matches the file");
        }
    }

    // ---- tests: GUI checks ----

    private void testNetworkTableDisplaysRequiredColumns() throws Exception {
        try (Fixture fixture = new Fixture()) {
            InsuranceNetworkManagementPanel panel = fixture.newPanel();
            JTable networkTable = (JTable) getPrivateField(panel, "networkTable");
            TableModel model = networkTable.getModel();
            List<String> columns = new ArrayList<>();
            for (int i = 0; i < model.getColumnCount(); i++) {
                columns.add(model.getColumnName(i));
            }
            assertTrue(columns.contains("ID"), "network table must show the network ID");
            assertTrue(columns.contains("Provider"), "network table must show the provider");
            assertTrue(columns.contains("Plan"), "network table must show the plan");
            assertTrue(columns.contains("Coverage %"), "network table must show the coverage percentage");
            assertTrue(columns.contains("Active"), "network table must show active status");
            assertTrue(columns.contains("Managed By"), "network table must show who manages the network");
        }
    }

    private void testNetworkManagementButtonsHaveActionListeners() throws Exception {
        try (Fixture fixture = new Fixture()) {
            InsuranceNetworkManagementPanel panel = fixture.newPanel();
            String[] buttonFields = {"addButton", "editButton", "toggleButton", "deleteButton", "backButton"};
            for (String fieldName : buttonFields) {
                JButton button = (JButton) getPrivateField(panel, fieldName);
                assertNotNull(button, fieldName + " must exist");
                assertTrue(button.getActionListeners().length > 0, fieldName + " must have an action listener wired");
            }
        }
    }

    // ---- tests: persistence failure (Step 8) ----

    private void testCreateNetworkWriteFailureLeavesNoPhantom() throws Exception {
        try (Fixture fixture = new Fixture()) {
            try (FileOutputStream lock = new FileOutputStream(fixture.insuranceFile, true)) {
                assertThrows(IllegalStateException.class,
                        () -> fixture.insuranceManager.createNetwork(
                                "HealthCo", "Gold Plan", 80.0, fixture.admin.getStaffId()),
                        "createNetwork must propagate a save failure instead of reporting success");
            }
            assertTrue(fixture.insuranceManager.getNetworks().isEmpty(),
                    "a failed createNetwork must leave no phantom network in memory");
        }
    }

    private void testUpdateNetworkWriteFailureRestoresOriginalFields() throws Exception {
        try (Fixture fixture = new Fixture()) {
            InsuranceNetwork network = fixture.insuranceManager.createNetwork(
                    "HealthCo", "Gold Plan", 80.0, fixture.admin.getStaffId());
            try (FileOutputStream lock = new FileOutputStream(fixture.insuranceFile, true)) {
                assertThrows(IllegalStateException.class,
                        () -> fixture.insuranceManager.updateNetwork(
                                network.getNetworkId(), "MediCare Plus", "Silver Plan", 60.0),
                        "updateNetwork must propagate a save failure instead of reporting success");
            }
            assertEquals("HealthCo", network.getProviderName(), "provider must be restored after a failed update");
            assertEquals("Gold Plan", network.getPlanName(), "plan must be restored after a failed update");
            assertEquals(80.0, network.getCoveragePercentage(), "coverage must be restored after a failed update");
        }
    }

    private void testSetActiveWriteFailureRestoresPreviousValue() throws Exception {
        try (Fixture fixture = new Fixture()) {
            InsuranceNetwork network = fixture.insuranceManager.createNetwork(
                    "HealthCo", "Gold Plan", 80.0, fixture.admin.getStaffId());
            try (FileOutputStream lock = new FileOutputStream(fixture.insuranceFile, true)) {
                assertThrows(IllegalStateException.class,
                        () -> fixture.insuranceManager.setActive(network.getNetworkId(), false),
                        "setActive must propagate a save failure instead of reporting success");
            }
            assertTrue(network.isActive(), "active status must be restored to true after a failed toggle");
        }
    }

    private void testDeleteNetworkWriteFailureRestoresRecord() throws Exception {
        try (Fixture fixture = new Fixture()) {
            InsuranceNetwork network = fixture.insuranceManager.createNetwork(
                    "HealthCo", "Gold Plan", 80.0, fixture.admin.getStaffId());
            try (FileOutputStream lock = new FileOutputStream(fixture.insuranceFile, true)) {
                assertThrows(IllegalStateException.class,
                        () -> fixture.insuranceManager.deleteNetwork(network.getNetworkId()),
                        "deleteNetwork must propagate a save failure instead of reporting success");
            }
            assertNotNull(fixture.insuranceManager.findById(network.getNetworkId()),
                    "a failed delete must leave the network in place");
        }
    }

    private void testExistingValidNetworkFileUnchangedAfterFailedWrite() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.insuranceManager.createNetwork("HealthCo", "Gold Plan", 80.0, fixture.admin.getStaffId());
            List<String> originalContent = Files.readAllLines(fixture.insuranceFile.toPath(), StandardCharsets.UTF_8);

            try (FileOutputStream lock = new FileOutputStream(fixture.insuranceFile, true)) {
                assertThrows(IllegalStateException.class,
                        () -> fixture.insuranceManager.createNetwork(
                                "MediCare Plus", "Silver Plan", 60.0, fixture.admin.getStaffId()),
                        "the second create must fail while the file is locked");
            }

            List<String> contentAfterFailure = Files.readAllLines(fixture.insuranceFile.toPath(), StandardCharsets.UTF_8);
            assertEquals(originalContent, contentAfterFailure,
                    "the existing valid insurance network file must be unchanged after a failed write");
        }
    }

    private void testUnreadableNetworkFileIsReportedAsLoadFailure() throws Exception {
        try (Fixture fixture = new Fixture()) {
            File directoryInPlaceOfFile = new File(fixture.tempDir.toFile(), "networks_as_directory.txt");
            assertTrue(directoryInPlaceOfFile.mkdirs(), "test setup: could not create the directory standing in for the data file");
            assertThrows(IllegalStateException.class,
                    () -> new InsuranceNetworkManager(fixture.userManager, directoryInPlaceOfFile),
                    "a data file that is actually a directory must be reported as a load failure, not an empty network list");
        }
    }
}
