package hms.manager;

import hms.model.HospitalAsset;
import hms.model.HospitalAssetType;
import hms.model.MedicalRecord;
import hms.model.MedicalRequest;
import hms.ui.admin.HospitalAssetManagementPanel;

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
 * Standalone, dependency-free Step 3 tests for HospitalAssetManager (all six
 * asset types, duplicate-name validation, loaded-record validation) and the
 * allocation-history safeguards enforced in the Admin asset workflow. Same
 * hand-rolled runner style as the Step 1/Step 2 test files, since the project
 * has no JUnit on its classpath. Every test works against its own temporary
 * directory (never the real data/ directory) and deletes it afterward. Test
 * dates are fixed strings, not the current clock.
 *
 * The request/type compatibility scenarios (LAB->LAB, X_RAY->XRAY_ROOM,
 * SPECIALIZED_IMAGING->IMAGING_ROOM, legacy IMAGING->IMAGING_ROOM, and the
 * two "wrong room type rejected" cases) are already covered by the updated
 * AssetAllocationManagerTest and are not duplicated here.
 *
 * Run with (from the project root, after compiling src to build/classes):
 *   javac -d build/test-classes -cp build/classes test/HMS/manager/HospitalAssetManagerTest.java
 *   java -cp build/classes;build/test-classes hms.manager.HospitalAssetManagerTest
 */
public class HospitalAssetManagerTest {

    private static final SimpleDateFormat DT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int passed = 0;
    private int failed = 0;

    public static void main(String[] args) throws Exception {
        HospitalAssetManagerTest suite = new HospitalAssetManagerTest();

        // ---- asset types ----
        suite.run("testConsultationRoomTypeAccepted", suite::testConsultationRoomTypeAccepted);
        suite.run("testClinicTypeAccepted", suite::testClinicTypeAccepted);
        suite.run("testWardTypeAccepted", suite::testWardTypeAccepted);
        suite.run("testLabTypeAccepted", suite::testLabTypeAccepted);
        suite.run("testXrayRoomTypeAccepted", suite::testXrayRoomTypeAccepted);
        suite.run("testImagingRoomTypeAccepted", suite::testImagingRoomTypeAccepted);
        suite.run("testUnknownTypeRejected", suite::testUnknownTypeRejected);

        // ---- creation/update validation ----
        suite.run("testBlankNameRejected", suite::testBlankNameRejected);
        suite.run("testBlankLocationRejected", suite::testBlankLocationRejected);
        suite.run("testDuplicateNameRejectedCaseInsensitive", suite::testDuplicateNameRejectedCaseInsensitive);
        suite.run("testDuplicateNameRejectedWhenSpacesDiffer", suite::testDuplicateNameRejectedWhenSpacesDiffer);
        suite.run("testUpdateMayKeepOwnName", suite::testUpdateMayKeepOwnName);
        suite.run("testUpdateCannotTakeAnotherAssetsName", suite::testUpdateCannotTakeAnotherAssetsName);
        suite.run("testInvalidAdminIdRejected", suite::testInvalidAdminIdRejected);
        suite.run("testNonAdminUserIdRejected", suite::testNonAdminUserIdRejected);
        suite.run("testCreateWithPaddedTypeStoresCanonicalType", suite::testCreateWithPaddedTypeStoresCanonicalType);
        suite.run("testUpdateWithPaddedTypeStoresCanonicalType", suite::testUpdateWithPaddedTypeStoresCanonicalType);

        // ---- loading ----
        suite.run("testMissingFileLoadsEmpty", suite::testMissingFileLoadsEmpty);
        suite.run("testValidLegacyRecordLoads", suite::testValidLegacyRecordLoads);
        suite.run("testImagingRoomLegacyRecordLoads", suite::testImagingRoomLegacyRecordLoads);
        suite.run("testLoadSkipsUnknownType", suite::testLoadSkipsUnknownType);
        suite.run("testLoadSkipsInvalidAvailability", suite::testLoadSkipsInvalidAvailability);
        suite.run("testLoadSkipsDuplicateId", suite::testLoadSkipsDuplicateId);
        suite.run("testLoadSkipsDuplicateName", suite::testLoadSkipsDuplicateName);
        suite.run("testLoadSkipsMalformedRowWithoutCrashing", suite::testLoadSkipsMalformedRowWithoutCrashing);
        suite.run("testLoadSkipsBlankAssetId", suite::testLoadSkipsBlankAssetId);
        suite.run("testLoadSkipsBlankLocation", suite::testLoadSkipsBlankLocation);
        suite.run("testLoadSkipsRowWithExtraFields", suite::testLoadSkipsRowWithExtraFields);
        suite.run("testLoadSkipsUnknownManagingStaffId", suite::testLoadSkipsUnknownManagingStaffId);
        suite.run("testLoadSkipsNonAdminManagingStaffId", suite::testLoadSkipsNonAdminManagingStaffId);

        // ---- allocation-history protection ----
        suite.run("testAssetWithoutHistoryMayBeDeleted", suite::testAssetWithoutHistoryMayBeDeleted);
        suite.run("testAssetWithScheduledAllocationCannotBeDeleted",
                suite::testAssetWithScheduledAllocationCannotBeDeleted);
        suite.run("testAssetWithCompletedAllocationCannotBeDeleted",
                suite::testAssetWithCompletedAllocationCannotBeDeleted);
        suite.run("testAssetWithCancelledAllocationCannotBeDeleted",
                suite::testAssetWithCancelledAllocationCannotBeDeleted);
        suite.run("testAssetWithoutHistoryMayChangeType", suite::testAssetWithoutHistoryMayChangeType);
        suite.run("testAssetWithHistoryCannotChangeType", suite::testAssetWithHistoryCannotChangeType);
        suite.run("testAssetWithHistoryMayChangeNameAndLocation", suite::testAssetWithHistoryMayChangeNameAndLocation);
        suite.run("testAssetWithHistoryMayChangeAvailability", suite::testAssetWithHistoryMayChangeAvailability);
        suite.run("testBookingDoesNotAutoChangeAvailability", suite::testBookingDoesNotAutoChangeAvailability);

        // ---- GUI/component checks ----
        suite.run("testAssetTypeListIncludesAllSixTypes", suite::testAssetTypeListIncludesAllSixTypes);
        suite.run("testClinicIsSelectable", suite::testClinicIsSelectable);
        suite.run("testXrayRoomIsSelectable", suite::testXrayRoomIsSelectable);
        suite.run("testAssetTableDisplaysRequiredColumns", suite::testAssetTableDisplaysRequiredColumns);
        suite.run("testAssetButtonsHaveActionListeners", suite::testAssetButtonsHaveActionListeners);

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

    /** One doctor, one patient, one admin, one medical manager, one appointment. */
    private static final class Fixture implements AutoCloseable {
        final Path tempDir;
        final UserManager userManager;
        final AppointmentManager appointmentManager;
        final ClinicalManager clinicalManager;
        final HospitalAssetManager hospitalAssetManager;
        final AssetAllocationManager allocationManager;
        final ConsultationRateManager rateManager;
        final InsuranceNetworkManager insuranceManager;
        final File assetsFile;

        Fixture() throws IOException {
            tempDir = Files.createTempDirectory("hms-hospitalasset-test-");
            File usersFile = new File(tempDir.toFile(), "users.txt");
            File shiftsFile = new File(tempDir.toFile(), "doctor_shifts.txt");
            File appointmentsFile = new File(tempDir.toFile(), "appointments.txt");
            File typesFile = new File(tempDir.toFile(), "assessment_types.txt");
            File recordsFile = new File(tempDir.toFile(), "medical_records.txt");
            File prescriptionsFile = new File(tempDir.toFile(), "prescriptions.txt");
            File requestsFile = new File(tempDir.toFile(), "medical_requests.txt");
            File resultsFile = new File(tempDir.toFile(), "assessment_results.txt");
            File feedbackFile = new File(tempDir.toFile(), "feedback.txt");
            File allocationsFile = new File(tempDir.toFile(), "asset_allocations.txt");
            File ratesFile = new File(tempDir.toFile(), "consultation_rates.txt");
            File insuranceFile = new File(tempDir.toFile(), "insurance_networks.txt");
            assetsFile = new File(tempDir.toFile(), "hospital_assets.txt");

            writeFile(usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "DR001|DOCTOR|doctor1|pass1234|0111111111|doctor1@test.com|DR001|Cardiology|true|",
                    "PT001|PATIENT|patient1|pass1234|0122222222|patient1@test.com|PT001|1990-01-01|true|",
                    "AS001|ADMIN_STAFF|admin1|pass1234|0133333333|admin1@test.com|AS001||true|",
                    "MM001|MEDICAL_MANAGER|manager1|pass1234|0144444444|manager1@test.com|MM001||true|");
            writeFile(appointmentsFile,
                    "#appointmentId|patientId|doctorId|appointmentDate|appointmentTime|status",
                    "AP001|PT001|DR001|2026-01-10|09:00|SCHEDULED");

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
        }

        HospitalAsset createAsset(String name, String type, String location, boolean available) {
            return hospitalAssetManager.createAsset(name, type, location, available, "AS001");
        }

        /** Opens a record, submits a request, approves it, and allocates it to the given asset. */
        MedicalRequest createAllocatedRequest(String requestType, String assetId, String start, String end)
                throws IOException {
            MedicalRecord record = clinicalManager.getOrOpenRecord("AP001", "DR001");
            MedicalRequest request = clinicalManager.addRequest(record.getRecordId(), requestType, "Test request");
            clinicalManager.approveRequest(request.getRequestId(), "AS001");
            allocationManager.createAllocation(request.getRequestId(), assetId, "AS001", dt(start), dt(end));
            return request;
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

    // ---- tests: asset types ----

    private void testConsultationRoomTypeAccepted() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("Room 1", "CONSULTATION_ROOM", "Ground Floor", true);
            assertEquals("CONSULTATION_ROOM", asset.getAssetType(), "CONSULTATION_ROOM is an accepted asset type");
        }
    }

    private void testClinicTypeAccepted() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("Dermatology Clinic", "CLINIC", "Block B", true);
            assertEquals("CLINIC", asset.getAssetType(), "CLINIC is an accepted asset type");
        }
    }

    private void testWardTypeAccepted() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("Ward 3", "WARD", "Third Floor", true);
            assertEquals("WARD", asset.getAssetType(), "WARD is an accepted asset type");
        }
    }

    private void testLabTypeAccepted() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("Lab 1", "LAB", "Ground Floor", true);
            assertEquals("LAB", asset.getAssetType(), "LAB is an accepted asset type");
        }
    }

    private void testXrayRoomTypeAccepted() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("X-Ray Room 1", "XRAY_ROOM", "First Floor", true);
            assertEquals("XRAY_ROOM", asset.getAssetType(), "XRAY_ROOM is an accepted asset type");
        }
    }

    private void testImagingRoomTypeAccepted() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("Imaging Room 1", "IMAGING_ROOM", "First Floor", true);
            assertEquals("IMAGING_ROOM", asset.getAssetType(), "IMAGING_ROOM is an accepted asset type");
        }
    }

    private void testUnknownTypeRejected() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.createAsset("Mystery Room", "SPACESHIP_HANGAR", "Roof", true),
                    "an unsupported asset type must be rejected");
        }
    }

    // ---- tests: creation/update validation ----

    private void testBlankNameRejected() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.createAsset("   ", "LAB", "Ground Floor", true),
                    "a blank asset name must be rejected");
        }
    }

    private void testBlankLocationRejected() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.createAsset("Lab 1", "LAB", "   ", true),
                    "a blank location must be rejected");
        }
    }

    private void testDuplicateNameRejectedCaseInsensitive() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.createAsset("Lab Room 1", "LAB", "Ground Floor", true);
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.createAsset("lab room 1", "LAB", "Basement", true),
                    "a duplicate name must be rejected regardless of letter case");
        }
    }

    private void testDuplicateNameRejectedWhenSpacesDiffer() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.createAsset("Lab Room 1", "LAB", "Ground Floor", true);
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.createAsset("  Lab Room 1  ", "LAB", "Basement", true),
                    "a duplicate name must be rejected even with extra surrounding spaces");
        }
    }

    private void testUpdateMayKeepOwnName() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("Lab Room 1", "LAB", "Ground Floor", true);
            fixture.hospitalAssetManager.updateAsset(asset.getAssetId(), "Lab Room 1", "LAB", "First Floor");
            assertEquals("First Floor", asset.getLocation(), "an asset may keep its own name while updating other fields");
        }
    }

    private void testUpdateCannotTakeAnotherAssetsName() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.createAsset("Lab Room 1", "LAB", "Ground Floor", true);
            HospitalAsset second = fixture.createAsset("Lab Room 2", "LAB", "First Floor", true);
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.hospitalAssetManager.updateAsset(second.getAssetId(), "Lab Room 1", "LAB", "First Floor"),
                    "an asset must not be renamed to another asset's name");
        }
    }

    private void testInvalidAdminIdRejected() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.hospitalAssetManager.createAsset("Lab Room 1", "LAB", "Ground Floor", true, "UNKNOWN999"),
                    "an unknown admin ID must be rejected");
        }
    }

    private void testNonAdminUserIdRejected() throws Exception {
        try (Fixture fixture = new Fixture()) {
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.hospitalAssetManager.createAsset("Lab Room 1", "LAB", "Ground Floor", true, "DR001"),
                    "a Doctor ID must not be accepted as the creating admin");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.hospitalAssetManager.createAsset("Lab Room 1", "LAB", "Ground Floor", true, "PT001"),
                    "a Patient ID must not be accepted as the creating admin");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.hospitalAssetManager.createAsset("Lab Room 1", "LAB", "Ground Floor", true, "MM001"),
                    "a Medical Manager ID must not be accepted as the creating admin");
        }
    }

    private void testCreateWithPaddedTypeStoresCanonicalType() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("Lab Room 1", "  LAB  ", "Ground Floor", true);
            assertEquals("LAB", asset.getAssetType(), "a type with surrounding whitespace must be stored canonically");
        }
    }

    private void testUpdateWithPaddedTypeStoresCanonicalType() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("Room 1", "LAB", "Ground Floor", true);
            fixture.hospitalAssetManager.updateAsset(asset.getAssetId(), "Room 1", "  CLINIC  ", "Ground Floor");
            assertEquals("CLINIC", asset.getAssetType(), "updating with a padded type must store the canonical type");
        }
    }

    // ---- tests: loading ----

    private void testMissingFileLoadsEmpty() throws Exception {
        try (Fixture fixture = new Fixture()) {
            File missingFile = new File(fixture.tempDir.toFile(), "does-not-exist.txt");
            HospitalAssetManager freshManager = new HospitalAssetManager(fixture.userManager, missingFile);
            assertTrue(freshManager.getAssets().isEmpty(), "a missing asset file must load as an empty list");
        }
    }

    private void testValidLegacyRecordLoads() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.assetsFile,
                    "#assetId|assetName|assetType|location|availability|managedByStaffId",
                    "HA001|Lab Room 1|LAB|Ground Floor|true|AS001");
            HospitalAssetManager freshManager = new HospitalAssetManager(fixture.userManager, fixture.assetsFile);
            HospitalAsset asset = freshManager.findById("HA001");
            assertNotNull(asset, "a valid legacy record must load");
            assertEquals("LAB", asset.getAssetType(), "loaded asset type matches the file");
            assertTrue(asset.isAvailability(), "loaded availability matches the file");
        }
    }

    private void testImagingRoomLegacyRecordLoads() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.assetsFile,
                    "#assetId|assetName|assetType|location|availability|managedByStaffId",
                    "HA002|Imaging Room 1|IMAGING_ROOM|First Floor|true|");
            HospitalAssetManager freshManager = new HospitalAssetManager(fixture.userManager, fixture.assetsFile);
            HospitalAsset asset = freshManager.findById("HA002");
            assertNotNull(asset, "an existing IMAGING_ROOM record must remain valid after Step 3");
            assertEquals("IMAGING_ROOM", asset.getAssetType(), "loaded type is IMAGING_ROOM");
        }
    }

    private void testLoadSkipsUnknownType() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.assetsFile,
                    "#assetId|assetName|assetType|location|availability|managedByStaffId",
                    "HA003|Mystery Room|SPACESHIP_HANGAR|Roof|true|");
            HospitalAssetManager freshManager = new HospitalAssetManager(fixture.userManager, fixture.assetsFile);
            assertTrue(freshManager.getAssets().isEmpty(), "a row with an unknown type must be skipped, not crash");
        }
    }

    private void testLoadSkipsInvalidAvailability() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.assetsFile,
                    "#assetId|assetName|assetType|location|availability|managedByStaffId",
                    "HA004|Lab Room 1|LAB|Ground Floor|maybe|");
            HospitalAssetManager freshManager = new HospitalAssetManager(fixture.userManager, fixture.assetsFile);
            assertTrue(freshManager.getAssets().isEmpty(),
                    "a row with an availability value other than true/false must be skipped, not crash");
        }
    }

    private void testLoadSkipsDuplicateId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.assetsFile,
                    "#assetId|assetName|assetType|location|availability|managedByStaffId",
                    "HA005|Lab Room 1|LAB|Ground Floor|true|",
                    "HA005|Lab Room 2|LAB|First Floor|true|");
            HospitalAssetManager freshManager = new HospitalAssetManager(fixture.userManager, fixture.assetsFile);
            assertEquals(1, freshManager.getAssets().size(), "a duplicate asset ID must be skipped, keeping only the first");
            assertEquals("Lab Room 1", freshManager.findById("HA005").getAssetName(),
                    "the first occurrence of a duplicate ID is the one kept");
        }
    }

    private void testLoadSkipsDuplicateName() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.assetsFile,
                    "#assetId|assetName|assetType|location|availability|managedByStaffId",
                    "HA006|Lab Room 1|LAB|Ground Floor|true|",
                    "HA007|Lab Room 1|LAB|First Floor|true|");
            HospitalAssetManager freshManager = new HospitalAssetManager(fixture.userManager, fixture.assetsFile);
            assertEquals(1, freshManager.getAssets().size(),
                    "a duplicate asset name must be skipped case-insensitively, keeping only the first");
            assertNotNull(freshManager.findById("HA006"), "the first occurrence of a duplicate name is the one kept");
            assertNull(freshManager.findById("HA007"), "the later duplicate-name row must not load");
        }
    }

    private void testLoadSkipsMalformedRowWithoutCrashing() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.assetsFile,
                    "#assetId|assetName|assetType|location|availability|managedByStaffId",
                    "TOO_FEW_FIELDS|LAB",
                    "HA008|Lab Room 1|LAB|Ground Floor|true|");
            HospitalAssetManager freshManager = new HospitalAssetManager(fixture.userManager, fixture.assetsFile);
            assertEquals(1, freshManager.getAssets().size(), "only the structurally valid row should load");
            assertNotNull(freshManager.findById("HA008"), "the valid row must still load");
        }
    }

    private void testLoadSkipsBlankAssetId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.assetsFile,
                    "#assetId|assetName|assetType|location|availability|managedByStaffId",
                    "   |Lab Room 1|LAB|Ground Floor|true|");
            HospitalAssetManager freshManager = new HospitalAssetManager(fixture.userManager, fixture.assetsFile);
            assertTrue(freshManager.getAssets().isEmpty(), "a row with a blank asset ID must be skipped, not crash");
        }
    }

    private void testLoadSkipsBlankLocation() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.assetsFile,
                    "#assetId|assetName|assetType|location|availability|managedByStaffId",
                    "HA009|Lab Room 1|LAB|   |true|");
            HospitalAssetManager freshManager = new HospitalAssetManager(fixture.userManager, fixture.assetsFile);
            assertTrue(freshManager.getAssets().isEmpty(), "a row with a blank location must be skipped, not crash");
        }
    }

    private void testLoadSkipsRowWithExtraFields() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.assetsFile,
                    "#assetId|assetName|assetType|location|availability|managedByStaffId",
                    "HA010|Lab Room 1|LAB|Ground Floor|true||EXTRA");
            HospitalAssetManager freshManager = new HospitalAssetManager(fixture.userManager, fixture.assetsFile);
            assertTrue(freshManager.getAssets().isEmpty(), "a row with more than six fields must be skipped, not crash");
        }
    }

    private void testLoadSkipsUnknownManagingStaffId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.assetsFile,
                    "#assetId|assetName|assetType|location|availability|managedByStaffId",
                    "HA011|Lab Room 1|LAB|Ground Floor|true|UNKNOWN999");
            HospitalAssetManager freshManager = new HospitalAssetManager(fixture.userManager, fixture.assetsFile);
            assertTrue(freshManager.getAssets().isEmpty(),
                    "a row naming an unknown managing staff ID must be skipped, not loaded without its manager");
        }
    }

    private void testLoadSkipsNonAdminManagingStaffId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.assetsFile,
                    "#assetId|assetName|assetType|location|availability|managedByStaffId",
                    "HA012|Lab Room 1|LAB|Ground Floor|true|DR001",
                    "HA013|Lab Room 2|LAB|First Floor|true|PT001",
                    "HA014|Lab Room 3|LAB|Second Floor|true|MM001");
            HospitalAssetManager freshManager = new HospitalAssetManager(fixture.userManager, fixture.assetsFile);
            assertTrue(freshManager.getAssets().isEmpty(),
                    "a row naming a Doctor, Patient, or Medical Manager as the managing staff ID must be skipped");
        }
    }

    // ---- tests: allocation-history protection ----

    private void testAssetWithoutHistoryMayBeDeleted() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("Lab Room 1", "LAB", "Ground Floor", true);
            assertTrue(!fixture.allocationManager.hasAllocationHistoryForAsset(asset.getAssetId()),
                    "a fresh asset has no allocation history");
            fixture.allocationManager.validateAssetCanBeDeleted(asset.getAssetId());
            fixture.hospitalAssetManager.deleteAsset(asset.getAssetId());
            assertNull(fixture.hospitalAssetManager.findById(asset.getAssetId()), "the asset must actually be deleted");
        }
    }

    private void testAssetWithScheduledAllocationCannotBeDeleted() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("Lab Room 1", "LAB", "Ground Floor", true);
            fixture.createAllocatedRequest("LAB", asset.getAssetId(), "2026-03-01 09:00:00", "2026-03-01 10:00:00");
            assertTrue(fixture.allocationManager.hasAllocationHistoryForAsset(asset.getAssetId()),
                    "a SCHEDULED allocation counts as allocation history");
            assertThrows(IllegalStateException.class,
                    () -> fixture.allocationManager.validateAssetCanBeDeleted(asset.getAssetId()),
                    "an asset with a SCHEDULED allocation must not be deletable");
            assertNotNull(fixture.hospitalAssetManager.findById(asset.getAssetId()), "the asset must still exist");
        }
    }

    private void testAssetWithCompletedAllocationCannotBeDeleted() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("Lab Room 1", "LAB", "Ground Floor", true);
            MedicalRequest request = fixture.createAllocatedRequest("LAB", asset.getAssetId(),
                    "2026-03-01 09:00:00", "2026-03-01 10:00:00");
            String allocationId = fixture.allocationManager.getAllocationsForRequest(request.getRequestId()).get(0)
                    .getAllocationId();
            fixture.allocationManager.completeAllocation(allocationId);
            assertThrows(IllegalStateException.class,
                    () -> fixture.allocationManager.validateAssetCanBeDeleted(asset.getAssetId()),
                    "an asset with a COMPLETED allocation must not be deletable");
        }
    }

    private void testAssetWithCancelledAllocationCannotBeDeleted() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("Lab Room 1", "LAB", "Ground Floor", true);
            MedicalRequest request = fixture.createAllocatedRequest("LAB", asset.getAssetId(),
                    "2026-03-01 09:00:00", "2026-03-01 10:00:00");
            String allocationId = fixture.allocationManager.getAllocationsForRequest(request.getRequestId()).get(0)
                    .getAllocationId();
            fixture.allocationManager.cancelAllocation(allocationId, "Patient rescheduled");
            assertThrows(IllegalStateException.class,
                    () -> fixture.allocationManager.validateAssetCanBeDeleted(asset.getAssetId()),
                    "an asset with a CANCELLED allocation must still not be deletable - history is preserved");
        }
    }

    private void testAssetWithoutHistoryMayChangeType() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("Room 1", "LAB", "Ground Floor", true);
            fixture.allocationManager.validateAssetTypeCanChange(asset.getAssetId(), asset.getAssetType(), "CLINIC");
            fixture.hospitalAssetManager.updateAsset(asset.getAssetId(), "Room 1", "CLINIC", "Ground Floor");
            assertEquals("CLINIC", asset.getAssetType(), "an asset with no allocation history may change type");
        }
    }

    private void testAssetWithHistoryCannotChangeType() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("Lab Room 1", "LAB", "Ground Floor", true);
            fixture.createAllocatedRequest("LAB", asset.getAssetId(), "2026-03-01 09:00:00", "2026-03-01 10:00:00");
            assertThrows(IllegalStateException.class,
                    () -> fixture.allocationManager.validateAssetTypeCanChange(
                            asset.getAssetId(), asset.getAssetType(), "CLINIC"),
                    "an asset with allocation history must not be allowed to change type");
            assertEquals("LAB", asset.getAssetType(), "the asset's type must remain unchanged");
        }
    }

    private void testAssetWithHistoryMayChangeNameAndLocation() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("Lab Room 1", "LAB", "Ground Floor", true);
            fixture.createAllocatedRequest("LAB", asset.getAssetId(), "2026-03-01 09:00:00", "2026-03-01 10:00:00");
            fixture.allocationManager.validateAssetTypeCanChange(asset.getAssetId(), asset.getAssetType(), "LAB");
            fixture.hospitalAssetManager.updateAsset(asset.getAssetId(), "Main Lab", "LAB", "Second Floor");
            assertEquals("Main Lab", asset.getAssetName(), "name may still change when type is unchanged");
            assertEquals("Second Floor", asset.getLocation(), "location may still change when type is unchanged");
        }
    }

    private void testAssetWithHistoryMayChangeAvailability() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("Lab Room 1", "LAB", "Ground Floor", true);
            fixture.createAllocatedRequest("LAB", asset.getAssetId(), "2026-03-01 09:00:00", "2026-03-01 10:00:00");
            fixture.hospitalAssetManager.setAvailability(asset.getAssetId(), false);
            assertTrue(!asset.isAvailability(), "operational availability may still change for an asset with history");
        }
    }

    private void testBookingDoesNotAutoChangeAvailability() throws Exception {
        try (Fixture fixture = new Fixture()) {
            HospitalAsset asset = fixture.createAsset("Lab Room 1", "LAB", "Ground Floor", true);
            fixture.createAllocatedRequest("LAB", asset.getAssetId(), "2026-03-01 09:00:00", "2026-03-01 10:00:00");
            assertTrue(asset.isAvailability(),
                    "creating a booking must not automatically mark the asset operationally unavailable");
        }
    }

    // ---- tests: GUI/component checks ----

    private void testAssetTypeListIncludesAllSixTypes() throws Exception {
        List<String> types = HospitalAssetManager.getValidTypes();
        assertEquals(6, types.size(), "there must be exactly six supported asset types");
        for (HospitalAssetType type : HospitalAssetType.values()) {
            assertTrue(types.contains(type.name()), "the type selector list must include " + type.name());
        }
    }

    private void testClinicIsSelectable() throws Exception {
        assertTrue(HospitalAssetManager.getValidTypes().contains("CLINIC"), "CLINIC must be a selectable asset type");
    }

    private void testXrayRoomIsSelectable() throws Exception {
        assertTrue(HospitalAssetManager.getValidTypes().contains("XRAY_ROOM"), "XRAY_ROOM must be a selectable asset type");
    }

    /**
     * Safe, headless component-level check: constructing the Admin asset panel needs no display,
     * and does not open the modal Add/Edit or confirm dialogs (those only appear once a button
     * is clicked). Inspects the real table model, the same one Admin sees.
     */
    private void testAssetTableDisplaysRequiredColumns() throws Exception {
        try (Fixture fixture = new Fixture()) {
            SessionManager sessionManager = new SessionManager(fixture.userManager);
            sessionManager.login("admin1", "pass1234");

            HospitalAssetManagementPanel panel = new HospitalAssetManagementPanel(
                    fixture.hospitalAssetManager, fixture.userManager, fixture.appointmentManager,
                    fixture.clinicalManager, fixture.rateManager, fixture.insuranceManager,
                    fixture.allocationManager, sessionManager, null, () -> { });

            JTable assetTable = (JTable) getPrivateField(panel, "assetTable");
            TableModel model = assetTable.getModel();
            List<String> columns = new ArrayList<>();
            for (int i = 0; i < model.getColumnCount(); i++) {
                columns.add(model.getColumnName(i));
            }
            assertTrue(columns.contains("ID"), "asset table must show the asset ID");
            assertTrue(columns.contains("Name"), "asset table must show the asset name");
            assertTrue(columns.contains("Type"), "asset table must show the asset type");
            assertTrue(columns.contains("Location"), "asset table must show the location");
            assertTrue(columns.contains("Available"), "asset table must show operational availability");
        }
    }

    private void testAssetButtonsHaveActionListeners() throws Exception {
        try (Fixture fixture = new Fixture()) {
            SessionManager sessionManager = new SessionManager(fixture.userManager);
            sessionManager.login("admin1", "pass1234");

            HospitalAssetManagementPanel panel = new HospitalAssetManagementPanel(
                    fixture.hospitalAssetManager, fixture.userManager, fixture.appointmentManager,
                    fixture.clinicalManager, fixture.rateManager, fixture.insuranceManager,
                    fixture.allocationManager, sessionManager, null, () -> { });

            String[] buttonFields = {"addButton", "editButton", "toggleButton", "deleteButton", "backButton"};
            for (String fieldName : buttonFields) {
                JButton button = (JButton) getPrivateField(panel, fieldName);
                assertNotNull(button, fieldName + " must exist");
                assertTrue(button.getActionListeners().length > 0, fieldName + " must have an action listener wired");
            }
        }
    }
}
