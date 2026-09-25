package hms.manager;

import hms.model.AdminStaff;
import hms.model.ConsultationRate;
import hms.model.Doctor;
import hms.ui.admin.ConsultationRateManagementPanel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * Standalone, dependency-free Step 6 tests for ConsultationRateManager (CRUD,
 * validation, deterministic current-active-rate selection, and hardened
 * consultation_rates.txt loading) and the headless-safe checks on
 * ConsultationRateManagementPanel. Same hand-rolled runner style as the
 * other Step test files, since the project has no JUnit on its classpath.
 * Every test works against its own temporary directory (never the real
 * data/ directory) and deletes it afterward.
 *
 * Run with (from the project root, after compiling src to build/classes):
 *   javac -d build/test-classes -cp build/classes test/HMS/manager/ConsultationRateManagerTest.java
 *   java -cp build/classes;build/test-classes hms.manager.ConsultationRateManagerTest
 */
public class ConsultationRateManagerTest {

    private static final SimpleDateFormat DT = new SimpleDateFormat("yyyy-MM-dd");
    static {
        DT.setLenient(false);
    }

    private int passed = 0;
    private int failed = 0;

    public static void main(String[] args) throws Exception {
        ConsultationRateManagerTest suite = new ConsultationRateManagerTest();

        // ---- CRUD ----
        suite.run("testCreateRate", suite::testCreateRate);
        suite.run("testUpdateRate", suite::testUpdateRate);
        suite.run("testToggleActive", suite::testToggleActive);
        suite.run("testDeleteRate", suite::testDeleteRate);
        suite.run("testPersistenceSurvivesReload", suite::testPersistenceSurvivesReload);

        // ---- effective-date encapsulation (defensive copies) ----
        suite.run("testMutatingDateGivenToCreateRateDoesNotAlterStoredRate",
                suite::testMutatingDateGivenToCreateRateDoesNotAlterStoredRate);
        suite.run("testMutatingDateGivenToUpdateRateDoesNotAlterStoredRate",
                suite::testMutatingDateGivenToUpdateRateDoesNotAlterStoredRate);
        suite.run("testMutatingReturnedEffectiveDateDoesNotAlterRate", suite::testMutatingReturnedEffectiveDateDoesNotAlterRate);
        suite.run("testDefensiveCopyDoesNotAffectPersistedReloadedValue",
                suite::testDefensiveCopyDoesNotAffectPersistedReloadedValue);

        // ---- validation ----
        suite.run("testCreateTrimsConsultationType", suite::testCreateTrimsConsultationType);
        suite.run("testUpdateTrimsConsultationType", suite::testUpdateTrimsConsultationType);
        suite.run("testCreateRejectsBlankType", suite::testCreateRejectsBlankType);
        suite.run("testCreateRejectsNegativeAmount", suite::testCreateRejectsNegativeAmount);
        suite.run("testCreateRejectsNaNAmount", suite::testCreateRejectsNaNAmount);
        suite.run("testCreateRejectsInfiniteAmount", suite::testCreateRejectsInfiniteAmount);
        suite.run("testCreateAcceptsZeroAmount", suite::testCreateAcceptsZeroAmount);
        suite.run("testCreateRejectsNullEffectiveDate", suite::testCreateRejectsNullEffectiveDate);
        suite.run("testCreateRejectsUnknownConfiguringUser", suite::testCreateRejectsUnknownConfiguringUser);
        suite.run("testCreateRejectsNonAdminConfiguringUser", suite::testCreateRejectsNonAdminConfiguringUser);
        suite.run("testInvalidUpdateLeavesOriginalValuesUnchanged", suite::testInvalidUpdateLeavesOriginalValuesUnchanged);

        // ---- current-rate selection ----
        suite.run("testCurrentActiveRateSelectsLatestEligibleRate", suite::testCurrentActiveRateSelectsLatestEligibleRate);
        suite.run("testCurrentActiveRateExcludesFutureRate", suite::testCurrentActiveRateExcludesFutureRate);
        suite.run("testCurrentActiveRateExcludesInactiveRate", suite::testCurrentActiveRateExcludesInactiveRate);
        suite.run("testCurrentActiveRateTieBreaksByRateId", suite::testCurrentActiveRateTieBreaksByRateId);
        suite.run("testCurrentActiveRateTieBreakHandlesFourDigitRateIds",
                suite::testCurrentActiveRateTieBreakHandlesFourDigitRateIds);
        suite.run("testCurrentActiveRateReturnsNullWhenNoneEligible", suite::testCurrentActiveRateReturnsNullWhenNoneEligible);
        suite.run("testNoArgCurrentActiveRateMatchesAsOfNow", suite::testNoArgCurrentActiveRateMatchesAsOfNow);

        // ---- loading hardening ----
        suite.run("testMissingFileLoadsEmpty", suite::testMissingFileLoadsEmpty);
        suite.run("testLoadSkipsMalformedRowTooFewFields", suite::testLoadSkipsMalformedRowTooFewFields);
        suite.run("testLoadSkipsRowWithTooManyFields", suite::testLoadSkipsRowWithTooManyFields);
        suite.run("testLoadSkipsBlankRateId", suite::testLoadSkipsBlankRateId);
        suite.run("testLoadSkipsBlankConsultationType", suite::testLoadSkipsBlankConsultationType);
        suite.run("testLoadSkipsDuplicateRateId", suite::testLoadSkipsDuplicateRateId);
        suite.run("testLoadSkipsNonNumericAmount", suite::testLoadSkipsNonNumericAmount);
        suite.run("testLoadSkipsNaNAmount", suite::testLoadSkipsNaNAmount);
        suite.run("testLoadSkipsNegativeAmount", suite::testLoadSkipsNegativeAmount);
        suite.run("testLoadSkipsInvalidDate", suite::testLoadSkipsInvalidDate);
        suite.run("testLoadSkipsDateWithTrailingCharacters", suite::testLoadSkipsDateWithTrailingCharacters);
        suite.run("testLoadSkipsNonCanonicalDate", suite::testLoadSkipsNonCanonicalDate);
        suite.run("testLoadSkipsInvalidActiveValue", suite::testLoadSkipsInvalidActiveValue);
        suite.run("testLoadSkipsUnknownConfiguredByStaffId", suite::testLoadSkipsUnknownConfiguredByStaffId);
        suite.run("testLoadSkipsNonAdminConfiguredByStaffId", suite::testLoadSkipsNonAdminConfiguredByStaffId);
        suite.run("testLoadKeepsEmptyConfiguredByForLegacyRows", suite::testLoadKeepsEmptyConfiguredByForLegacyRows);
        suite.run("testValidRecordLoads", suite::testValidRecordLoads);

        // ---- GUI checks ----
        suite.run("testRateTableDisplaysRequiredColumns", suite::testRateTableDisplaysRequiredColumns);
        suite.run("testRateManagementButtonsHaveActionListeners", suite::testRateManagementButtonsHaveActionListeners);

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

    private static Date dt(String value) {
        try {
            return DT.parse(value);
        } catch (ParseException e) {
            throw new IllegalStateException("Bad fixture date: " + value, e);
        }
    }

    /** A full, wired-together environment, backed only by temporary files - never data/. */
    private static final class Fixture implements AutoCloseable {
        final Path tempDir;
        final File usersFile;
        final File ratesFile;
        final UserManager userManager;
        final AppointmentManager appointmentManager;
        final ClinicalManager clinicalManager;
        final HospitalAssetManager hospitalAssetManager;
        final AssetAllocationManager allocationManager;
        final InsuranceNetworkManager insuranceManager;
        final ConsultationRateManager rateManager;
        final SessionManager sessionManager;
        final AdminStaff admin;

        Fixture() throws IOException {
            tempDir = Files.createTempDirectory("hms-consultationrate-test-");
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
            File insuranceFile = new File(tempDir.toFile(), "insurance_networks.txt");
            ratesFile = new File(tempDir.toFile(), "consultation_rates.txt");

            userManager = new UserManager(usersFile);
            DoctorShiftManager shiftManager = new DoctorShiftManager(userManager, shiftsFile);
            appointmentManager = new AppointmentManager(userManager, shiftManager, appointmentsFile);
            AssessmentTypeManager typeManager = new AssessmentTypeManager(userManager, typesFile);
            clinicalManager = new ClinicalManager(userManager, appointmentManager, typeManager,
                    recordsFile, prescriptionsFile, requestsFile, resultsFile, feedbackFile);
            hospitalAssetManager = new HospitalAssetManager(userManager, assetsFile);
            allocationManager = new AssetAllocationManager(clinicalManager, hospitalAssetManager, userManager,
                    allocationsFile);
            insuranceManager = new InsuranceNetworkManager(userManager, insuranceFile);
            rateManager = new ConsultationRateManager(userManager, ratesFile);
            sessionManager = new SessionManager(userManager);

            admin = (AdminStaff) userManager.registerUser(
                    new AdminStaff(null, "admin1", "pass1234", "0111111111", "admin1@test.com", "ADMIN_STAFF", null));
        }

        ConsultationRateManagementPanel newPanel() {
            return new ConsultationRateManagementPanel(rateManager, userManager, appointmentManager, clinicalManager,
                    hospitalAssetManager, insuranceManager, allocationManager, sessionManager, null, () -> { });
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

    private void testCreateRate() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ConsultationRate rate = fixture.rateManager.createRate(
                    "General Checkup", 50.0, dt("2026-01-01"), fixture.admin.getStaffId());
            assertEquals("General Checkup", rate.getConsultationType(), "consultation type must be stored");
            assertEquals(50.0, rate.getBaseAmount(), "base amount must be stored");
            assertEquals(dt("2026-01-01"), rate.getEffectiveDate(), "effective date must be stored");
            assertTrue(rate.isActive(), "a newly created rate must start active");
            assertEquals(fixture.admin.getStaffId(), rate.getConfiguredBy().getStaffId(), "configuring admin must be recorded");
        }
    }

    private void testUpdateRate() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ConsultationRate rate = fixture.rateManager.createRate(
                    "General Checkup", 50.0, dt("2026-01-01"), fixture.admin.getStaffId());
            fixture.rateManager.updateRate(rate.getRateId(), "Follow Up", 75.0, dt("2026-02-01"));
            assertEquals("Follow Up", rate.getConsultationType(), "consultation type must be updated");
            assertEquals(75.0, rate.getBaseAmount(), "base amount must be updated");
            assertEquals(dt("2026-02-01"), rate.getEffectiveDate(), "effective date must be updated");
        }
    }

    private void testToggleActive() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ConsultationRate rate = fixture.rateManager.createRate(
                    "General Checkup", 50.0, dt("2026-01-01"), fixture.admin.getStaffId());
            fixture.rateManager.setActive(rate.getRateId(), false);
            assertTrue(!rate.isActive(), "the rate must be deactivated");
            fixture.rateManager.setActive(rate.getRateId(), true);
            assertTrue(rate.isActive(), "the rate must be reactivated");
        }
    }

    private void testDeleteRate() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ConsultationRate rate = fixture.rateManager.createRate(
                    "General Checkup", 50.0, dt("2026-01-01"), fixture.admin.getStaffId());
            fixture.rateManager.deleteRate(rate.getRateId());
            assertNull(fixture.rateManager.findById(rate.getRateId()), "the rate must actually be deleted");
        }
    }

    private void testPersistenceSurvivesReload() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ConsultationRate rate = fixture.rateManager.createRate(
                    "General Checkup", 50.0, dt("2026-01-01"), fixture.admin.getStaffId());
            ConsultationRateManager reloaded = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            ConsultationRate reloadedRate = reloaded.findById(rate.getRateId());
            assertNotNull(reloadedRate, "the rate must reload");
            assertEquals("General Checkup", reloadedRate.getConsultationType(), "consultation type must survive reload");
            assertEquals(50.0, reloadedRate.getBaseAmount(), "base amount must survive reload");
            assertEquals(dt("2026-01-01"), reloadedRate.getEffectiveDate(), "effective date must survive reload");
            assertTrue(reloadedRate.isActive(), "active status must survive reload");
            assertEquals(fixture.admin.getStaffId(), reloadedRate.getConfiguredBy().getStaffId(),
                    "configuring admin must survive reload");
        }
    }

    // ---- tests: effective-date encapsulation (defensive copies) ----

    /** Reviewer-reported correction: createRate() must not keep a live reference to the caller's Date. */
    private void testMutatingDateGivenToCreateRateDoesNotAlterStoredRate() throws Exception {
        try (Fixture fixture = new Fixture()) {
            Date suppliedDate = dt("2026-01-01");
            ConsultationRate rate = fixture.rateManager.createRate(
                    "General Checkup", 50.0, suppliedDate, fixture.admin.getStaffId());

            suppliedDate.setTime(dt("2099-12-31").getTime());

            assertEquals(dt("2026-01-01"), rate.getEffectiveDate(),
                    "mutating the Date passed to createRate() must not change the stored rate's effective date");
        }
    }

    /** Reviewer-reported correction: updateRate() must not keep a live reference to the caller's Date. */
    private void testMutatingDateGivenToUpdateRateDoesNotAlterStoredRate() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ConsultationRate rate = fixture.rateManager.createRate(
                    "General Checkup", 50.0, dt("2026-01-01"), fixture.admin.getStaffId());
            Date suppliedDate = dt("2026-06-01");
            fixture.rateManager.updateRate(rate.getRateId(), "Follow Up", 75.0, suppliedDate);

            suppliedDate.setTime(dt("2099-12-31").getTime());

            assertEquals(dt("2026-06-01"), rate.getEffectiveDate(),
                    "mutating the Date passed to updateRate() must not change the stored rate's effective date");
        }
    }

    /** Reviewer-reported correction: getEffectiveDate() must not return a live reference to the internal Date. */
    private void testMutatingReturnedEffectiveDateDoesNotAlterRate() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ConsultationRate rate = fixture.rateManager.createRate(
                    "General Checkup", 50.0, dt("2026-01-01"), fixture.admin.getStaffId());

            Date returnedDate = rate.getEffectiveDate();
            returnedDate.setTime(dt("2099-12-31").getTime());

            assertEquals(dt("2026-01-01"), rate.getEffectiveDate(),
                    "mutating the Date returned by getEffectiveDate() must not change the rate's stored value");
        }
    }

    /** Confirms the defensive-copy change did not break normal save/reload behavior. */
    private void testDefensiveCopyDoesNotAffectPersistedReloadedValue() throws Exception {
        try (Fixture fixture = new Fixture()) {
            Date suppliedDate = dt("2026-01-01");
            ConsultationRate rate = fixture.rateManager.createRate(
                    "General Checkup", 50.0, suppliedDate, fixture.admin.getStaffId());
            suppliedDate.setTime(dt("2099-12-31").getTime());

            ConsultationRateManager reloaded = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            ConsultationRate reloadedRate = reloaded.findById(rate.getRateId());
            assertEquals(dt("2026-01-01"), reloadedRate.getEffectiveDate(),
                    "the persisted and reloaded effective date must reflect what was true at save time, "
                            + "not a later mutation of the caller's Date object");
        }
    }

    // ---- tests: validation ----

    private void testCreateTrimsConsultationType() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ConsultationRate rate = fixture.rateManager.createRate(
                    "  General Checkup  ", 50.0, dt("2026-01-01"), fixture.admin.getStaffId());
            assertEquals("General Checkup", rate.getConsultationType(), "consultation type must be stored trimmed");
        }
    }

    private void testUpdateTrimsConsultationType() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ConsultationRate rate = fixture.rateManager.createRate(
                    "General Checkup", 50.0, dt("2026-01-01"), fixture.admin.getStaffId());
            fixture.rateManager.updateRate(rate.getRateId(), "  Follow Up  ", 60.0, dt("2026-02-01"));
            assertEquals("Follow Up", rate.getConsultationType(), "an updated consultation type must be stored trimmed");
        }
    }

    private void testCreateRejectsBlankType() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.rateManager.createRate("   ", 50.0, dt("2026-01-01"), fixture.admin.getStaffId()),
                    "a blank consultation type must be rejected");
        }
    }

    private void testCreateRejectsNegativeAmount() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.rateManager.createRate("General Checkup", -10.0, dt("2026-01-01"), fixture.admin.getStaffId()),
                    "a negative base amount must be rejected");
        }
    }

    private void testCreateRejectsNaNAmount() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.rateManager.createRate(
                            "General Checkup", Double.NaN, dt("2026-01-01"), fixture.admin.getStaffId()),
                    "a NaN base amount must be rejected");
        }
    }

    private void testCreateRejectsInfiniteAmount() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.rateManager.createRate("General Checkup", Double.POSITIVE_INFINITY, dt("2026-01-01"),
                            fixture.admin.getStaffId()),
                    "positive infinity must be rejected");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.rateManager.createRate("General Checkup", Double.NEGATIVE_INFINITY, dt("2026-01-01"),
                            fixture.admin.getStaffId()),
                    "negative infinity must be rejected");
        }
    }

    /** Documents the coursework decision: a rate may legitimately be free of charge, so zero is valid. */
    private void testCreateAcceptsZeroAmount() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ConsultationRate rate = fixture.rateManager.createRate(
                    "Complimentary Checkup", 0.0, dt("2026-01-01"), fixture.admin.getStaffId());
            assertEquals(0.0, rate.getBaseAmount(), "zero must be accepted as a valid base amount");
        }
    }

    private void testCreateRejectsNullEffectiveDate() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.rateManager.createRate("General Checkup", 50.0, null, fixture.admin.getStaffId()),
                    "a null effective date must be rejected");
        }
    }

    private void testCreateRejectsUnknownConfiguringUser() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.rateManager.createRate("General Checkup", 50.0, dt("2026-01-01"), "UNKNOWN999"),
                    "an unknown configuring user ID must be rejected");
        }
    }

    private void testCreateRejectsNonAdminConfiguringUser() throws Exception {
        try (Fixture fixture = new Fixture()) {
            Doctor doctor = (Doctor) fixture.userManager.registerUser(
                    new Doctor(null, "doctor1", "pass1234", "0122222222", "doctor1@test.com", "DOCTOR", null, "Cardiology"));
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.rateManager.createRate("General Checkup", 50.0, dt("2026-01-01"), doctor.getUserId()),
                    "a Doctor ID must not be accepted as the configuring Admin Staff");
        }
    }

    private void testInvalidUpdateLeavesOriginalValuesUnchanged() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ConsultationRate rate = fixture.rateManager.createRate(
                    "General Checkup", 50.0, dt("2026-01-01"), fixture.admin.getStaffId());
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.rateManager.updateRate(rate.getRateId(), "   ", 75.0, dt("2026-02-01")),
                    "an update with a blank consultation type must be rejected");
            assertEquals("General Checkup", rate.getConsultationType(), "consultation type must be unchanged after a failed update");
            assertEquals(50.0, rate.getBaseAmount(), "base amount must be unchanged after a failed update");
            assertEquals(dt("2026-01-01"), rate.getEffectiveDate(), "effective date must be unchanged after a failed update");
        }
    }

    // ---- tests: current-rate selection ----

    private void testCurrentActiveRateSelectsLatestEligibleRate() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.rateManager.createRate("Early Rate", 40.0, dt("2026-01-01"), fixture.admin.getStaffId());
            ConsultationRate later = fixture.rateManager.createRate("Later Rate", 60.0, dt("2026-06-01"), fixture.admin.getStaffId());
            ConsultationRate selected = fixture.rateManager.getCurrentActiveRate(dt("2026-12-01"));
            assertEquals(later.getRateId(), selected.getRateId(), "the latest eligible rate must be selected");
        }
    }

    private void testCurrentActiveRateExcludesFutureRate() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ConsultationRate current = fixture.rateManager.createRate(
                    "Current Rate", 40.0, dt("2026-01-01"), fixture.admin.getStaffId());
            fixture.rateManager.createRate("Future Rate", 90.0, dt("2026-12-01"), fixture.admin.getStaffId());
            ConsultationRate selected = fixture.rateManager.getCurrentActiveRate(dt("2026-06-01"));
            assertEquals(current.getRateId(), selected.getRateId(), "a future-effective rate must not be selected");
        }
    }

    private void testCurrentActiveRateExcludesInactiveRate() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ConsultationRate active = fixture.rateManager.createRate(
                    "Active Rate", 40.0, dt("2026-01-01"), fixture.admin.getStaffId());
            ConsultationRate inactive = fixture.rateManager.createRate(
                    "Inactive Rate", 90.0, dt("2026-06-01"), fixture.admin.getStaffId());
            fixture.rateManager.setActive(inactive.getRateId(), false);
            ConsultationRate selected = fixture.rateManager.getCurrentActiveRate(dt("2026-12-01"));
            assertEquals(active.getRateId(), selected.getRateId(), "an inactive rate must never be selected");
        }
    }

    /** Documents the deterministic tie-break rule: on equal effective dates, the greater (newer) rate ID wins. */
    private void testCurrentActiveRateTieBreaksByRateId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.rateManager.createRate("First Rate", 40.0, dt("2026-01-01"), fixture.admin.getStaffId());
            ConsultationRate second = fixture.rateManager.createRate(
                    "Second Rate", 60.0, dt("2026-01-01"), fixture.admin.getStaffId());
            ConsultationRate selected = fixture.rateManager.getCurrentActiveRate(dt("2026-12-01"));
            assertEquals(second.getRateId(), selected.getRateId(),
                    "on a tied effective date, the most recently created (greater ID) rate must win");
        }
    }

    /**
     * Reviewer-reported correction: lexicographic comparison would wrongly rank "CR999" above
     * "CR1000" (since '9' sorts after '1'). The tie-break must compare the numeric suffix as a
     * number, so CR1000 - the later-created rate - correctly wins.
     */
    private void testCurrentActiveRateTieBreakHandlesFourDigitRateIds() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.ratesFile,
                    "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId",
                    "CR999|Older By Id|40.0|2026-01-01|true|",
                    "CR1000|Newer By Id|60.0|2026-01-01|true|");
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            ConsultationRate selected = freshManager.getCurrentActiveRate(dt("2026-12-01"));
            assertEquals("CR1000", selected.getRateId(),
                    "CR1000 must be treated as numerically newer than CR999, not lexicographically older");
        }
    }

    private void testCurrentActiveRateReturnsNullWhenNoneEligible() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ConsultationRate rate = fixture.rateManager.createRate(
                    "Future Rate", 40.0, dt("2026-12-01"), fixture.admin.getStaffId());
            fixture.rateManager.setActive(rate.getRateId(), false);
            assertNull(fixture.rateManager.getCurrentActiveRate(dt("2026-01-01")),
                    "no rate must be selected when none are both active and eligible");
        }
    }

    private void testNoArgCurrentActiveRateMatchesAsOfNow() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ConsultationRate rate = fixture.rateManager.createRate(
                    "Long Standing Rate", 40.0, dt("2000-01-01"), fixture.admin.getStaffId());
            ConsultationRate selected = fixture.rateManager.getCurrentActiveRate();
            assertNotNull(selected, "the no-arg overload must select an eligible rate using the real current time");
            assertEquals(rate.getRateId(), selected.getRateId(), "the no-arg overload must match getCurrentActiveRate(new Date())");
        }
    }

    // ---- tests: loading hardening ----

    private void testMissingFileLoadsEmpty() throws Exception {
        try (Fixture fixture = new Fixture()) {
            File missingFile = new File(fixture.tempDir.toFile(), "does-not-exist.txt");
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, missingFile);
            assertTrue(freshManager.getRates().isEmpty(), "a missing rates file must load as an empty list");
        }
    }

    private void testLoadSkipsMalformedRowTooFewFields() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.ratesFile,
                    "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId",
                    "TOO_FEW|General Checkup|50.0");
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            assertTrue(freshManager.getRates().isEmpty(), "a row with too few fields must be skipped, not crash");
        }
    }

    private void testLoadSkipsRowWithTooManyFields() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.ratesFile,
                    "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId",
                    "CR001|General Checkup|50.0|2026-01-01|true||EXTRA");
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            assertTrue(freshManager.getRates().isEmpty(), "a row with more than six fields must be skipped, not crash");
        }
    }

    private void testLoadSkipsBlankRateId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.ratesFile,
                    "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId",
                    "   |General Checkup|50.0|2026-01-01|true|");
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            assertTrue(freshManager.getRates().isEmpty(), "a row with a blank rate ID must be skipped, not crash");
        }
    }

    private void testLoadSkipsBlankConsultationType() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.ratesFile,
                    "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId",
                    "CR001|   |50.0|2026-01-01|true|");
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            assertTrue(freshManager.getRates().isEmpty(), "a row with a blank consultation type must be skipped, not crash");
        }
    }

    private void testLoadSkipsDuplicateRateId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.ratesFile,
                    "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId",
                    "CR001|General Checkup|50.0|2026-01-01|true|",
                    "CR001|Follow Up|75.0|2026-02-01|true|");
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            assertEquals(1, freshManager.getRates().size(), "a duplicate rate ID must be skipped, keeping only the first");
            assertEquals("General Checkup", freshManager.findById("CR001").getConsultationType(),
                    "the first occurrence of a duplicate ID is the one kept");
        }
    }

    private void testLoadSkipsNonNumericAmount() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.ratesFile,
                    "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId",
                    "CR001|General Checkup|not-a-number|2026-01-01|true|");
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            assertTrue(freshManager.getRates().isEmpty(), "a row with a non-numeric amount must be skipped, not crash");
        }
    }

    private void testLoadSkipsNaNAmount() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.ratesFile,
                    "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId",
                    "CR001|General Checkup|NaN|2026-01-01|true|");
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            assertTrue(freshManager.getRates().isEmpty(),
                    "a row whose amount parses to NaN must be skipped - Double.parseDouble(\"NaN\") does not throw");
        }
    }

    private void testLoadSkipsNegativeAmount() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.ratesFile,
                    "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId",
                    "CR001|General Checkup|-10.0|2026-01-01|true|");
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            assertTrue(freshManager.getRates().isEmpty(), "a row with a negative amount must be skipped, not crash");
        }
    }

    private void testLoadSkipsInvalidDate() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.ratesFile,
                    "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId",
                    "CR001|General Checkup|50.0|2026-02-30|true|");
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            assertTrue(freshManager.getRates().isEmpty(),
                    "a row with an impossible calendar date must be skipped, not silently rolled over");
        }
    }

    private void testLoadSkipsDateWithTrailingCharacters() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.ratesFile,
                    "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId",
                    "CR001|General Checkup|50.0|2026-01-01junk|true|");
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            assertTrue(freshManager.getRates().isEmpty(),
                    "a date with trailing characters must be skipped, not truncated and accepted");
        }
    }

    private void testLoadSkipsNonCanonicalDate() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.ratesFile,
                    "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId",
                    "CR001|General Checkup|50.0|2026-1-1|true|");
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            assertTrue(freshManager.getRates().isEmpty(), "a non-canonical date shape (single-digit month/day) must be skipped");
        }
    }

    private void testLoadSkipsInvalidActiveValue() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.ratesFile,
                    "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId",
                    "CR001|General Checkup|50.0|2026-01-01|maybe|");
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            assertTrue(freshManager.getRates().isEmpty(),
                    "a row with an active value other than true/false must be skipped, not crash");
        }
    }

    private void testLoadSkipsUnknownConfiguredByStaffId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.ratesFile,
                    "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId",
                    "CR001|General Checkup|50.0|2026-01-01|true|UNKNOWN999");
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            assertTrue(freshManager.getRates().isEmpty(),
                    "a row naming an unknown configuring staff ID must be skipped, not loaded without its configurer");
        }
    }

    private void testLoadSkipsNonAdminConfiguredByStaffId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.userManager.registerUser(
                    new Doctor(null, "doctor1", "pass1234", "0122222222", "doctor1@test.com", "DOCTOR", null, "Cardiology"));
            Doctor doctor = fixture.userManager.findDoctorById(
                    fixture.userManager.findByUsername("doctor1").getUserId());
            writeFile(fixture.ratesFile,
                    "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId",
                    "CR001|General Checkup|50.0|2026-01-01|true|" + doctor.getDoctorId());
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            assertTrue(freshManager.getRates().isEmpty(),
                    "a row naming a Doctor as the configuring staff ID must be skipped");
        }
    }

    private void testLoadKeepsEmptyConfiguredByForLegacyRows() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.ratesFile,
                    "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId",
                    "CR001|General Checkup|50.0|2026-01-01|true|");
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            ConsultationRate rate = freshManager.findById("CR001");
            assertNotNull(rate, "a legacy row with an empty configuredByStaffId must still load");
            assertNull(rate.getConfiguredBy(), "an empty configuredByStaffId must leave configuredBy unset, not crash");
        }
    }

    private void testValidRecordLoads() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.ratesFile,
                    "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId",
                    "CR001|General Checkup|50.0|2026-01-01|true|" + fixture.admin.getStaffId());
            ConsultationRateManager freshManager = new ConsultationRateManager(fixture.userManager, fixture.ratesFile);
            ConsultationRate rate = freshManager.findById("CR001");
            assertNotNull(rate, "a valid record must load");
            assertEquals("General Checkup", rate.getConsultationType(), "loaded consultation type matches the file");
            assertEquals(50.0, rate.getBaseAmount(), "loaded base amount matches the file");
            assertEquals(dt("2026-01-01"), rate.getEffectiveDate(), "loaded effective date matches the file");
            assertTrue(rate.isActive(), "loaded active status matches the file");
            assertEquals(fixture.admin.getStaffId(), rate.getConfiguredBy().getStaffId(), "loaded configuring admin matches the file");
        }
    }

    // ---- tests: GUI checks ----

    private void testRateTableDisplaysRequiredColumns() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ConsultationRateManagementPanel panel = fixture.newPanel();
            JTable rateTable = (JTable) getPrivateField(panel, "rateTable");
            TableModel model = rateTable.getModel();
            List<String> columns = new ArrayList<>();
            for (int i = 0; i < model.getColumnCount(); i++) {
                columns.add(model.getColumnName(i));
            }
            assertTrue(columns.contains("ID"), "rate table must show the rate ID");
            assertTrue(columns.contains("Type"), "rate table must show the consultation type");
            assertTrue(columns.contains("Base Amount"), "rate table must show the base amount");
            assertTrue(columns.contains("Effective Date"), "rate table must show the effective date");
            assertTrue(columns.contains("Active"), "rate table must show active status");
            assertTrue(columns.contains("Configured By"), "rate table must show who configured the rate");
        }
    }

    private void testRateManagementButtonsHaveActionListeners() throws Exception {
        try (Fixture fixture = new Fixture()) {
            ConsultationRateManagementPanel panel = fixture.newPanel();
            String[] buttonFields = {"addButton", "editButton", "toggleButton", "deleteButton", "backButton"};
            for (String fieldName : buttonFields) {
                JButton button = (JButton) getPrivateField(panel, fieldName);
                assertNotNull(button, fieldName + " must exist");
                assertTrue(button.getActionListeners().length > 0, fieldName + " must have an action listener wired");
            }
        }
    }
}
