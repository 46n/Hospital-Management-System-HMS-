package hms.manager;

import hms.model.AssetAllocation;
import hms.model.HospitalAsset;
import hms.model.MedicalRecord;
import hms.model.MedicalRequest;
import hms.ui.admin.MedicalRequestManagementPanel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableModel;

/**
 * Standalone, dependency-free lifecycle tests for the Step 2 hospital-asset
 * allocation workflow (AssetAllocationManager plus the ClinicalManager
 * request-status hooks it drives). Same hand-rolled runner style as
 * ClinicalManagerMedicalRequestTest, since the project has no JUnit on its
 * classpath. Every test works against its own temporary directory (never the
 * real data/ directory) and deletes it afterward. Test dates are fixed
 * strings, not the current clock.
 *
 * Run with (from the project root, after compiling src to build/classes):
 *   javac -d build/test-classes -cp build/classes test/HMS/manager/AssetAllocationManagerTest.java
 *   java -cp build/classes;build/test-classes hms.manager.AssetAllocationManagerTest
 */
public class AssetAllocationManagerTest {

    private static final SimpleDateFormat DT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int passed = 0;
    private int failed = 0;

    public static void main(String[] args) throws Exception {
        AssetAllocationManagerTest suite = new AssetAllocationManagerTest();

        suite.run("testCreateAllocationForLabRequest", suite::testCreateAllocationForLabRequest);
        suite.run("testCreateAllocationForXRayRequest", suite::testCreateAllocationForXRayRequest);
        suite.run("testRejectXRayRequestWithImagingRoomAsset", suite::testRejectXRayRequestWithImagingRoomAsset);
        suite.run("testCreateAllocationForSpecializedImagingRequest",
                suite::testCreateAllocationForSpecializedImagingRequest);
        suite.run("testRejectSpecializedImagingRequestWithXRayRoomAsset",
                suite::testRejectSpecializedImagingRequestWithXRayRoomAsset);
        suite.run("testCreateAllocationForLegacyImagingRequest", suite::testCreateAllocationForLegacyImagingRequest);
        suite.run("testRejectIncompatibleAssetType", suite::testRejectIncompatibleAssetType);
        suite.run("testRejectAllocationOfPendingRequest", suite::testRejectAllocationOfPendingRequest);
        suite.run("testRejectAllocationOfRejectedRequest", suite::testRejectAllocationOfRejectedRequest);
        suite.run("testRejectUnavailableAsset", suite::testRejectUnavailableAsset);
        suite.run("testRejectMissingStartTime", suite::testRejectMissingStartTime);
        suite.run("testRejectMissingEndTime", suite::testRejectMissingEndTime);
        suite.run("testRejectEndBeforeStart", suite::testRejectEndBeforeStart);
        suite.run("testRejectEqualStartAndEndTimes", suite::testRejectEqualStartAndEndTimes);
        suite.run("testRejectOverlappingAllocationsOnSameAsset", suite::testRejectOverlappingAllocationsOnSameAsset);
        suite.run("testAllowBackToBackAllocations", suite::testAllowBackToBackAllocations);
        suite.run("testAllowOverlappingTimesForDifferentAssets", suite::testAllowOverlappingTimesForDifferentAssets);
        suite.run("testPreventSecondActiveAllocationForSameRequest",
                suite::testPreventSecondActiveAllocationForSameRequest);
        suite.run("testCompletedAllocationBlocksOverlappingAllocation",
                suite::testCompletedAllocationBlocksOverlappingAllocation);
        suite.run("testCompletedAllocationDoesNotBlockNonOverlappingTime",
                suite::testCompletedAllocationDoesNotBlockNonOverlappingTime);
        suite.run("testCancelledAllocationFreesTimeSlot", suite::testCancelledAllocationFreesTimeSlot);
        suite.run("testCancellationRequiresReason", suite::testCancellationRequiresReason);
        suite.run("testCompletedAllocationCannotBeCancelled", suite::testCompletedAllocationCannotBeCancelled);
        suite.run("testCancelledAllocationCannotBeCompleted", suite::testCancelledAllocationCannotBeCompleted);
        suite.run("testRepeatedCompletionIsRejected", suite::testRepeatedCompletionIsRejected);
        suite.run("testRepeatedCancellationIsRejected", suite::testRepeatedCancellationIsRejected);
        suite.run("testCancellingAllocationWithMissingLinkedRequestIsRejected",
                suite::testCancellingAllocationWithMissingLinkedRequestIsRejected);
        suite.run("testCompletingAllocationWithMissingLinkedRequestIsRejected",
                suite::testCompletingAllocationWithMissingLinkedRequestIsRejected);
        suite.run("testCancellingAllocationWhenRequestIsNotAllocatedIsRejected",
                suite::testCancellingAllocationWhenRequestIsNotAllocatedIsRejected);
        suite.run("testCompletingAllocationWhenRequestIsNotAllocatedIsRejected",
                suite::testCompletingAllocationWhenRequestIsNotAllocatedIsRejected);
        suite.run("testCreateAllocationWithValidAdminSucceeds", suite::testCreateAllocationWithValidAdminSucceeds);
        suite.run("testCreateAllocationRejectsDoctorId", suite::testCreateAllocationRejectsDoctorId);
        suite.run("testCreateAllocationRejectsPatientId", suite::testCreateAllocationRejectsPatientId);
        suite.run("testCreateAllocationRejectsMedicalManagerId", suite::testCreateAllocationRejectsMedicalManagerId);
        suite.run("testCreateAllocationRejectsUnknownAdminId", suite::testCreateAllocationRejectsUnknownAdminId);
        suite.run("testModelRejectsUnknownStatus", suite::testModelRejectsUnknownStatus);
        suite.run("testModelRejectsCancelledWithoutReason", suite::testModelRejectsCancelledWithoutReason);
        suite.run("testModelRejectsNonCancelledWithReason", suite::testModelRejectsNonCancelledWithReason);
        suite.run("testDateFieldsAreDefensivelyCopied", suite::testDateFieldsAreDefensivelyCopied);
        suite.run("testRequestBecomesAllocatedAfterSuccess", suite::testRequestBecomesAllocatedAfterSuccess);
        suite.run("testRequestReturnsToApprovedAfterCancellation", suite::testRequestReturnsToApprovedAfterCancellation);
        suite.run("testRequestBecomesCompletedAfterCompletion", suite::testRequestBecomesCompletedAfterCompletion);
        suite.run("testPersistenceSurvivesReload", suite::testPersistenceSurvivesReload);
        suite.run("testMalformedAllocationRowsDoNotCrashLoading", suite::testMalformedAllocationRowsDoNotCrashLoading);
        suite.run("testLoadSkipsUnknownStatus", suite::testLoadSkipsUnknownStatus);
        suite.run("testLoadSkipsCancelledWithoutReason", suite::testLoadSkipsCancelledWithoutReason);
        suite.run("testLoadSkipsNonCancelledRecordWithReason", suite::testLoadSkipsNonCancelledRecordWithReason);
        suite.run("testLoadSkipsDuplicateAllocationIds", suite::testLoadSkipsDuplicateAllocationIds);
        suite.run("testAllocationWriteFailureDoesNotLeavePhantomAllocation",
                suite::testAllocationWriteFailureDoesNotLeavePhantomAllocation);
        suite.run("testRequestWriteFailureLeavesConsistentState", suite::testRequestWriteFailureLeavesConsistentState);
        suite.run("testAdminGuiExposesAllocationControlsAndStatusFilter",
                suite::testAdminGuiExposesAllocationControlsAndStatusFilter);
        suite.run("testUnreadableAllocationFileIsReportedAsLoadFailure",
                suite::testUnreadableAllocationFileIsReportedAsLoadFailure);

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

    /** One doctor, one patient, one admin, one appointment, and a small fixed asset roster. */
    private static final class Fixture implements AutoCloseable {
        final Path tempDir;
        final UserManager userManager;
        final AppointmentManager appointmentManager;
        final ClinicalManager clinicalManager;
        final HospitalAssetManager hospitalAssetManager;
        final AssetAllocationManager allocationManager;
        final ConsultationRateManager rateManager;
        final InsuranceNetworkManager insuranceManager;
        final File requestsFile;
        final File allocationsFile;

        Fixture() throws IOException {
            tempDir = Files.createTempDirectory("hms-allocation-test-");
            File usersFile = new File(tempDir.toFile(), "users.txt");
            File shiftsFile = new File(tempDir.toFile(), "doctor_shifts.txt");
            File appointmentsFile = new File(tempDir.toFile(), "appointments.txt");
            File typesFile = new File(tempDir.toFile(), "assessment_types.txt");
            File recordsFile = new File(tempDir.toFile(), "medical_records.txt");
            File prescriptionsFile = new File(tempDir.toFile(), "prescriptions.txt");
            requestsFile = new File(tempDir.toFile(), "medical_requests.txt");
            File resultsFile = new File(tempDir.toFile(), "assessment_results.txt");
            File feedbackFile = new File(tempDir.toFile(), "feedback.txt");
            File assetsFile = new File(tempDir.toFile(), "hospital_assets.txt");
            allocationsFile = new File(tempDir.toFile(), "asset_allocations.txt");
            File ratesFile = new File(tempDir.toFile(), "consultation_rates.txt");
            File insuranceFile = new File(tempDir.toFile(), "insurance_networks.txt");

            writeFile(usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "DR001|DOCTOR|doctor1|pass1234|0111111111|doctor1@test.com|DR001|Cardiology|true|",
                    "PT001|PATIENT|patient1|pass1234|0122222222|patient1@test.com|PT001|1990-01-01|true|",
                    "AS001|ADMIN_STAFF|admin1|pass1234|0133333333|admin1@test.com|AS001||true|",
                    "MM001|MEDICAL_MANAGER|manager1|pass1234|0144444444|manager1@test.com|MM001||true|");
            writeFile(appointmentsFile,
                    "#appointmentId|patientId|doctorId|appointmentDate|appointmentTime|status",
                    "AP001|PT001|DR001|2026-01-10|09:00|SCHEDULED");
            writeFile(assetsFile,
                    "#assetId|assetName|assetType|location|availability|managedByStaffId",
                    "HA001|Lab Room 1|LAB|Ground Floor|true|",
                    "HA002|Imaging Room 1|IMAGING_ROOM|First Floor|true|",
                    "HA003|Old Lab|LAB|Basement|false|",
                    "HA004|Lab Room 2|LAB|Ground Floor|true|",
                    "HA005|X-Ray Room 1|XRAY_ROOM|First Floor|true|");

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

        MedicalRequest createApprovedRequest(String type, String details) throws IOException {
            MedicalRecord record = clinicalManager.getOrOpenRecord("AP001", "DR001");
            MedicalRequest request = clinicalManager.addRequest(record.getRecordId(), type, details);
            clinicalManager.approveRequest(request.getRequestId(), "AS001");
            return request;
        }

        /** Rebuilds the whole manager stack from the same on-disk files, to test reload/persistence. */
        Reopened reopen() throws IOException {
            File usersFile = new File(tempDir.toFile(), "users.txt");
            File shiftsFile = new File(tempDir.toFile(), "doctor_shifts.txt");
            File appointmentsFile = new File(tempDir.toFile(), "appointments.txt");
            File typesFile = new File(tempDir.toFile(), "assessment_types.txt");
            File recordsFile = new File(tempDir.toFile(), "medical_records.txt");
            File prescriptionsFile = new File(tempDir.toFile(), "prescriptions.txt");
            File resultsFile = new File(tempDir.toFile(), "assessment_results.txt");
            File feedbackFile = new File(tempDir.toFile(), "feedback.txt");
            File assetsFile = new File(tempDir.toFile(), "hospital_assets.txt");

            UserManager freshUsers = new UserManager(usersFile);
            DoctorShiftManager freshShifts = new DoctorShiftManager(freshUsers, shiftsFile);
            AppointmentManager freshAppointments = new AppointmentManager(freshUsers, freshShifts, appointmentsFile);
            AssessmentTypeManager freshTypes = new AssessmentTypeManager(freshUsers, typesFile);
            ClinicalManager freshClinical = new ClinicalManager(freshUsers, freshAppointments, freshTypes,
                    recordsFile, prescriptionsFile, requestsFile, resultsFile, feedbackFile);
            HospitalAssetManager freshAssets = new HospitalAssetManager(freshUsers, assetsFile);
            AssetAllocationManager freshAllocations = new AssetAllocationManager(freshClinical, freshAssets,
                    freshUsers, allocationsFile);
            return new Reopened(freshClinical, freshAllocations);
        }

        @Override
        public void close() throws IOException {
            deleteRecursively(tempDir);
        }
    }

    private static final class Reopened {
        final ClinicalManager clinicalManager;
        final AssetAllocationManager allocationManager;

        Reopened(ClinicalManager clinicalManager, AssetAllocationManager allocationManager) {
            this.clinicalManager = clinicalManager;
            this.allocationManager = allocationManager;
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

    private static AssetAllocation findById(List<AssetAllocation> allocations, String allocationId) {
        for (AssetAllocation allocation : allocations) {
            if (allocation.getAllocationId().equals(allocationId)) {
                return allocation;
            }
        }
        return null;
    }

    // ---- tests: compatibility mapping ----

    private void testCreateAllocationForLabRequest() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            AssetAllocation allocation = fixture.allocationManager.createAllocation(
                    request.getRequestId(), "HA001", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));
            assertEquals("HA001", allocation.getAssetId(), "LAB request allocated to the LAB asset");
            assertEquals(AssetAllocation.STATUS_SCHEDULED, allocation.getAllocationStatus(), "new allocation is SCHEDULED");
        }
    }

    private void testCreateAllocationForXRayRequest() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("X_RAY", "Right wrist");
            AssetAllocation allocation = fixture.allocationManager.createAllocation(
                    request.getRequestId(), "HA005", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 09:30:00"));
            assertEquals("HA005", allocation.getAssetId(), "X_RAY request allocated to the XRAY_ROOM asset");
        }
    }

    private void testRejectXRayRequestWithImagingRoomAsset() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("X_RAY", "Right wrist");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.allocationManager.createAllocation(request.getRequestId(), "HA002", "AS001",
                            dt("2026-03-01 09:00:00"), dt("2026-03-01 09:30:00")),
                    "a new X_RAY allocation must require XRAY_ROOM, not IMAGING_ROOM");
        }
    }

    private void testRejectSpecializedImagingRequestWithXRayRoomAsset() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("SPECIALIZED_IMAGING", "MRI lumbar spine");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.allocationManager.createAllocation(request.getRequestId(), "HA005", "AS001",
                            dt("2026-03-01 09:00:00"), dt("2026-03-01 09:45:00")),
                    "a SPECIALIZED_IMAGING allocation must require IMAGING_ROOM, not XRAY_ROOM");
        }
    }

    private void testCreateAllocationForSpecializedImagingRequest() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("SPECIALIZED_IMAGING", "MRI lumbar spine");
            AssetAllocation allocation = fixture.allocationManager.createAllocation(
                    request.getRequestId(), "HA002", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 09:45:00"));
            assertEquals("HA002", allocation.getAssetId(),
                    "SPECIALIZED_IMAGING request allocated to the IMAGING_ROOM asset");
        }
    }

    private void testCreateAllocationForLegacyImagingRequest() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("IMAGING", "Legacy chest scan");
            AssetAllocation allocation = fixture.allocationManager.createAllocation(
                    request.getRequestId(), "HA002", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 09:30:00"));
            assertEquals("HA002", allocation.getAssetId(), "legacy IMAGING request allocated to the IMAGING_ROOM asset");
        }
    }

    private void testRejectIncompatibleAssetType() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.allocationManager.createAllocation(request.getRequestId(), "HA002", "AS001",
                            dt("2026-03-01 09:00:00"), dt("2026-03-01 09:30:00")),
                    "a LAB request must not be allocated an IMAGING_ROOM asset");
        }
    }

    // ---- tests: request status gate ----

    private void testRejectAllocationOfPendingRequest() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRecord record = fixture.clinicalManager.getOrOpenRecord("AP001", "DR001");
            MedicalRequest request = fixture.clinicalManager.addRequest(record.getRecordId(), "LAB", "Lipid panel");
            assertThrows(IllegalStateException.class,
                    () -> fixture.allocationManager.createAllocation(request.getRequestId(), "HA001", "AS001",
                            dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00")),
                    "a PENDING request must not be allocated");
        }
    }

    private void testRejectAllocationOfRejectedRequest() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRecord record = fixture.clinicalManager.getOrOpenRecord("AP001", "DR001");
            MedicalRequest request = fixture.clinicalManager.addRequest(record.getRecordId(), "LAB", "Lipid panel");
            fixture.clinicalManager.rejectRequest(request.getRequestId(), "AS001", "Not needed");
            assertThrows(IllegalStateException.class,
                    () -> fixture.allocationManager.createAllocation(request.getRequestId(), "HA001", "AS001",
                            dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00")),
                    "a REJECTED request must not be allocated");
        }
    }

    // ---- tests: asset validation ----

    private void testRejectUnavailableAsset() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.allocationManager.createAllocation(request.getRequestId(), "HA003", "AS001",
                            dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00")),
                    "an operationally unavailable asset must not be allocated");
        }
    }

    // ---- tests: date/time validation ----

    private void testRejectMissingStartTime() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.allocationManager.createAllocation(request.getRequestId(), "HA001", "AS001",
                            null, dt("2026-03-01 10:00:00")),
                    "a missing start time must be rejected");
        }
    }

    private void testRejectMissingEndTime() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.allocationManager.createAllocation(request.getRequestId(), "HA001", "AS001",
                            dt("2026-03-01 09:00:00"), null),
                    "a missing end time must be rejected");
        }
    }

    private void testRejectEndBeforeStart() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.allocationManager.createAllocation(request.getRequestId(), "HA001", "AS001",
                            dt("2026-03-01 10:00:00"), dt("2026-03-01 09:00:00")),
                    "end before start must be rejected");
        }
    }

    private void testRejectEqualStartAndEndTimes() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.allocationManager.createAllocation(request.getRequestId(), "HA001", "AS001",
                            dt("2026-03-01 09:00:00"), dt("2026-03-01 09:00:00")),
                    "equal start and end times must be rejected");
        }
    }

    // ---- tests: collision detection ----

    private void testRejectOverlappingAllocationsOnSameAsset() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest first = fixture.createApprovedRequest("LAB", "Full blood count");
            fixture.allocationManager.createAllocation(first.getRequestId(), "HA001", "AS001",
                    dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));

            MedicalRequest second = fixture.createApprovedRequest("LAB", "Lipid panel");
            assertThrows(IllegalStateException.class,
                    () -> fixture.allocationManager.createAllocation(second.getRequestId(), "HA001", "AS001",
                            dt("2026-03-01 09:30:00"), dt("2026-03-01 10:30:00")),
                    "an overlapping time range on the same asset must be rejected");
        }
    }

    private void testAllowBackToBackAllocations() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest first = fixture.createApprovedRequest("LAB", "Full blood count");
            fixture.allocationManager.createAllocation(first.getRequestId(), "HA001", "AS001",
                    dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));

            MedicalRequest second = fixture.createApprovedRequest("LAB", "Lipid panel");
            AssetAllocation secondAllocation = fixture.allocationManager.createAllocation(
                    second.getRequestId(), "HA001", "AS001", dt("2026-03-01 10:00:00"), dt("2026-03-01 11:00:00"));
            assertEquals(AssetAllocation.STATUS_SCHEDULED, secondAllocation.getAllocationStatus(),
                    "a back-to-back allocation starting exactly when the first ends must be allowed");
        }
    }

    private void testAllowOverlappingTimesForDifferentAssets() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest first = fixture.createApprovedRequest("LAB", "Full blood count");
            fixture.allocationManager.createAllocation(first.getRequestId(), "HA001", "AS001",
                    dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));

            MedicalRequest second = fixture.createApprovedRequest("LAB", "Lipid panel");
            AssetAllocation secondAllocation = fixture.allocationManager.createAllocation(
                    second.getRequestId(), "HA004", "AS001", dt("2026-03-01 09:30:00"), dt("2026-03-01 10:30:00"));
            assertEquals("HA004", secondAllocation.getAssetId(),
                    "an overlapping time range on a different asset must be allowed");
        }
    }

    private void testPreventSecondActiveAllocationForSameRequest() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            fixture.allocationManager.createAllocation(request.getRequestId(), "HA001", "AS001",
                    dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));
            assertThrows(IllegalStateException.class,
                    () -> fixture.allocationManager.createAllocation(request.getRequestId(), "HA004", "AS001",
                            dt("2026-03-01 11:00:00"), dt("2026-03-01 12:00:00")),
                    "a request with an active allocation must not receive a second one");
        }
    }

    private void testCancelledAllocationFreesTimeSlot() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest first = fixture.createApprovedRequest("LAB", "Full blood count");
            AssetAllocation firstAllocation = fixture.allocationManager.createAllocation(
                    first.getRequestId(), "HA001", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));
            fixture.allocationManager.cancelAllocation(firstAllocation.getAllocationId(), "Patient rescheduled");

            MedicalRequest second = fixture.createApprovedRequest("LAB", "Lipid panel");
            AssetAllocation secondAllocation = fixture.allocationManager.createAllocation(
                    second.getRequestId(), "HA001", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));
            assertEquals(AssetAllocation.STATUS_SCHEDULED, secondAllocation.getAllocationStatus(),
                    "a cancelled allocation must free its time slot for reuse");
        }
    }

    private void testCompletedAllocationBlocksOverlappingAllocation() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest first = fixture.createApprovedRequest("LAB", "Full blood count");
            AssetAllocation firstAllocation = fixture.allocationManager.createAllocation(
                    first.getRequestId(), "HA001", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));
            fixture.allocationManager.completeAllocation(firstAllocation.getAllocationId());

            MedicalRequest second = fixture.createApprovedRequest("LAB", "Lipid panel");
            assertThrows(IllegalStateException.class,
                    () -> fixture.allocationManager.createAllocation(second.getRequestId(), "HA001", "AS001",
                            dt("2026-03-01 09:30:00"), dt("2026-03-01 10:30:00")),
                    "a completed allocation must still block an overlapping time on the same asset");
        }
    }

    private void testCompletedAllocationDoesNotBlockNonOverlappingTime() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest first = fixture.createApprovedRequest("LAB", "Full blood count");
            AssetAllocation firstAllocation = fixture.allocationManager.createAllocation(
                    first.getRequestId(), "HA001", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));
            fixture.allocationManager.completeAllocation(firstAllocation.getAllocationId());

            MedicalRequest second = fixture.createApprovedRequest("LAB", "Lipid panel");
            AssetAllocation secondAllocation = fixture.allocationManager.createAllocation(
                    second.getRequestId(), "HA001", "AS001", dt("2026-03-01 11:00:00"), dt("2026-03-01 12:00:00"));
            assertEquals(AssetAllocation.STATUS_SCHEDULED, secondAllocation.getAllocationStatus(),
                    "a non-overlapping time after a completed allocation must be allowed");
        }
    }

    // ---- tests: cancel/complete rules ----

    private void testCancellationRequiresReason() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            AssetAllocation allocation = fixture.allocationManager.createAllocation(
                    request.getRequestId(), "HA001", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.allocationManager.cancelAllocation(allocation.getAllocationId(), ""),
                    "cancelling with a blank reason must be rejected");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.allocationManager.cancelAllocation(allocation.getAllocationId(), null),
                    "cancelling with a null reason must be rejected");
        }
    }

    private void testCompletedAllocationCannotBeCancelled() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            AssetAllocation allocation = fixture.allocationManager.createAllocation(
                    request.getRequestId(), "HA001", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));
            fixture.allocationManager.completeAllocation(allocation.getAllocationId());
            assertThrows(IllegalStateException.class,
                    () -> fixture.allocationManager.cancelAllocation(allocation.getAllocationId(), "Too late"),
                    "a completed allocation must not be cancellable");
        }
    }

    private void testCancelledAllocationCannotBeCompleted() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            AssetAllocation allocation = fixture.allocationManager.createAllocation(
                    request.getRequestId(), "HA001", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));
            fixture.allocationManager.cancelAllocation(allocation.getAllocationId(), "Patient rescheduled");
            assertThrows(IllegalStateException.class,
                    () -> fixture.allocationManager.completeAllocation(allocation.getAllocationId()),
                    "a cancelled allocation must not be completable");
        }
    }

    private void testRepeatedCompletionIsRejected() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            AssetAllocation allocation = fixture.allocationManager.createAllocation(
                    request.getRequestId(), "HA001", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));
            fixture.allocationManager.completeAllocation(allocation.getAllocationId());
            assertThrows(IllegalStateException.class,
                    () -> fixture.allocationManager.completeAllocation(allocation.getAllocationId()),
                    "completing an already-completed allocation must be rejected (repeated click)");
        }
    }

    private void testRepeatedCancellationIsRejected() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            AssetAllocation allocation = fixture.allocationManager.createAllocation(
                    request.getRequestId(), "HA001", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));
            fixture.allocationManager.cancelAllocation(allocation.getAllocationId(), "Patient rescheduled");
            assertThrows(IllegalStateException.class,
                    () -> fixture.allocationManager.cancelAllocation(allocation.getAllocationId(), "Again"),
                    "cancelling an already-cancelled allocation must be rejected (repeated click)");
        }
    }

    // ---- tests: linked-request prevalidation ----

    private void testCancellingAllocationWithMissingLinkedRequestIsRejected() throws Exception {
        try (Fixture fixture = new Fixture()) {
            // A hand-written allocation referencing a request ID ClinicalManager never loaded -
            // AssetAllocationManager doesn't itself validate requestId existence at load time.
            writeFile(fixture.allocationsFile,
                    "#allocationId|requestId|assetId|allocatedByAdminId|startDateTime|endDateTime|status|createdDateTime|cancellationReason",
                    "AL500|MQ999|HA001|AS001|2026-03-01 09:00:00|2026-03-01 10:00:00|SCHEDULED|2026-03-01 08:00:00|");
            Reopened reopened = fixture.reopen();

            assertThrows(IllegalStateException.class,
                    () -> reopened.allocationManager.cancelAllocation("AL500", "Some reason"),
                    "cancelling an allocation whose linked request cannot be found must be rejected");
            AssetAllocation stillThere = findById(reopened.allocationManager.getAllocations(), "AL500");
            assertEquals(AssetAllocation.STATUS_SCHEDULED, stillThere.getAllocationStatus(),
                    "no allocation state may change when the linked request is missing");
        }
    }

    private void testCompletingAllocationWithMissingLinkedRequestIsRejected() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.allocationsFile,
                    "#allocationId|requestId|assetId|allocatedByAdminId|startDateTime|endDateTime|status|createdDateTime|cancellationReason",
                    "AL501|MQ999|HA001|AS001|2026-03-01 09:00:00|2026-03-01 10:00:00|SCHEDULED|2026-03-01 08:00:00|");
            Reopened reopened = fixture.reopen();

            assertThrows(IllegalStateException.class,
                    () -> reopened.allocationManager.completeAllocation("AL501"),
                    "completing an allocation whose linked request cannot be found must be rejected");
            AssetAllocation stillThere = findById(reopened.allocationManager.getAllocations(), "AL501");
            assertEquals(AssetAllocation.STATUS_SCHEDULED, stillThere.getAllocationStatus(),
                    "no allocation state may change when the linked request is missing");
        }
    }

    private void testCancellingAllocationWhenRequestIsNotAllocatedIsRejected() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            AssetAllocation allocation = fixture.allocationManager.createAllocation(
                    request.getRequestId(), "HA001", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));
            // Desync the request back to APPROVED without touching the (still SCHEDULED) allocation,
            // to simulate an inconsistent state the prevalidation must catch.
            fixture.clinicalManager.revertRequestToApproved(request.getRequestId());

            assertThrows(IllegalStateException.class,
                    () -> fixture.allocationManager.cancelAllocation(allocation.getAllocationId(), "Some reason"),
                    "cancelling must be rejected when the linked request is not ALLOCATED");
            assertEquals(AssetAllocation.STATUS_SCHEDULED, allocation.getAllocationStatus(),
                    "the allocation must be untouched when the linked request state is inconsistent");
        }
    }

    private void testCompletingAllocationWhenRequestIsNotAllocatedIsRejected() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            AssetAllocation allocation = fixture.allocationManager.createAllocation(
                    request.getRequestId(), "HA001", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));
            fixture.clinicalManager.revertRequestToApproved(request.getRequestId());

            assertThrows(IllegalStateException.class,
                    () -> fixture.allocationManager.completeAllocation(allocation.getAllocationId()),
                    "completing must be rejected when the linked request is not ALLOCATED");
            assertEquals(AssetAllocation.STATUS_SCHEDULED, allocation.getAllocationStatus(),
                    "the allocation must be untouched when the linked request state is inconsistent");
        }
    }

    // ---- tests: admin identity validation ----

    private void testCreateAllocationWithValidAdminSucceeds() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            AssetAllocation allocation = fixture.allocationManager.createAllocation(request.getRequestId(), "HA001",
                    "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));
            assertEquals("AS001", allocation.getAllocatedByAdminId(), "a valid Admin Staff ID must be accepted");
        }
    }

    private void testCreateAllocationRejectsDoctorId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.allocationManager.createAllocation(request.getRequestId(), "HA001", "DR001",
                            dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00")),
                    "a Doctor ID must not be accepted as the allocating admin");
            assertTrue(fixture.allocationManager.getAllocations().isEmpty(),
                    "no allocation may be saved for an invalid allocating user");
        }
    }

    private void testCreateAllocationRejectsPatientId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.allocationManager.createAllocation(request.getRequestId(), "HA001", "PT001",
                            dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00")),
                    "a Patient ID must not be accepted as the allocating admin");
        }
    }

    private void testCreateAllocationRejectsMedicalManagerId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.allocationManager.createAllocation(request.getRequestId(), "HA001", "MM001",
                            dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00")),
                    "a Medical Manager ID must not be accepted as the allocating admin");
        }
    }

    private void testCreateAllocationRejectsUnknownAdminId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.allocationManager.createAllocation(request.getRequestId(), "HA001", "UNKNOWN999",
                            dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00")),
                    "an unknown ID must not be accepted as the allocating admin");
        }
    }

    // ---- tests: AssetAllocation model status validation ----

    private void testModelRejectsUnknownStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> new AssetAllocation("AL1", "MQ1", "HA001", "AS001", dt("2026-03-01 09:00:00"),
                        dt("2026-03-01 10:00:00"), "BOGUS_STATUS", dt("2026-03-01 08:00:00"), null),
                "the model must reject an unknown status");
    }

    private void testModelRejectsCancelledWithoutReason() {
        assertThrows(IllegalArgumentException.class,
                () -> new AssetAllocation("AL1", "MQ1", "HA001", "AS001", dt("2026-03-01 09:00:00"),
                        dt("2026-03-01 10:00:00"), AssetAllocation.STATUS_CANCELLED, dt("2026-03-01 08:00:00"), null),
                "a cancelled allocation must require a non-blank reason");
        assertThrows(IllegalArgumentException.class,
                () -> new AssetAllocation("AL1", "MQ1", "HA001", "AS001", dt("2026-03-01 09:00:00"),
                        dt("2026-03-01 10:00:00"), AssetAllocation.STATUS_CANCELLED, dt("2026-03-01 08:00:00"), "   "),
                "a blank cancellation reason must not satisfy the requirement");
    }

    private void testModelRejectsNonCancelledWithReason() {
        assertThrows(IllegalArgumentException.class,
                () -> new AssetAllocation("AL1", "MQ1", "HA001", "AS001", dt("2026-03-01 09:00:00"),
                        dt("2026-03-01 10:00:00"), AssetAllocation.STATUS_SCHEDULED, dt("2026-03-01 08:00:00"),
                        "Should not be here"),
                "a SCHEDULED allocation must not carry a cancellation reason");
        assertThrows(IllegalArgumentException.class,
                () -> new AssetAllocation("AL1", "MQ1", "HA001", "AS001", dt("2026-03-01 09:00:00"),
                        dt("2026-03-01 10:00:00"), AssetAllocation.STATUS_COMPLETED, dt("2026-03-01 08:00:00"),
                        "Should not be here"),
                "a COMPLETED allocation must not carry a cancellation reason");
    }

    // ---- tests: Date defensive copies ----

    private void testDateFieldsAreDefensivelyCopied() {
        Date start = dt("2026-03-01 09:00:00");
        Date end = dt("2026-03-01 10:00:00");
        Date created = dt("2026-03-01 08:00:00");
        AssetAllocation allocation = new AssetAllocation("AL1", "MQ1", "HA001", "AS001", start, end,
                AssetAllocation.STATUS_SCHEDULED, created, null);

        long originalStart = allocation.getStartDateTime().getTime();
        long originalEnd = allocation.getEndDateTime().getTime();
        long originalCreated = allocation.getCreatedDateTime().getTime();

        start.setTime(0);
        end.setTime(0);
        created.setTime(0);
        assertEquals(originalStart, allocation.getStartDateTime().getTime(),
                "mutating the input start Date after construction must not change the stored value");
        assertEquals(originalEnd, allocation.getEndDateTime().getTime(),
                "mutating the input end Date after construction must not change the stored value");
        assertEquals(originalCreated, allocation.getCreatedDateTime().getTime(),
                "mutating the input created Date after construction must not change the stored value");

        allocation.getStartDateTime().setTime(0);
        allocation.getEndDateTime().setTime(0);
        allocation.getCreatedDateTime().setTime(0);
        assertEquals(originalStart, allocation.getStartDateTime().getTime(),
                "mutating a Date returned by a getter must not change the stored value");
        assertEquals(originalEnd, allocation.getEndDateTime().getTime(),
                "mutating a Date returned by a getter must not change the stored value");
        assertEquals(originalCreated, allocation.getCreatedDateTime().getTime(),
                "mutating a Date returned by a getter must not change the stored value");
    }

    // ---- tests: request status integration ----

    private void testRequestBecomesAllocatedAfterSuccess() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            fixture.allocationManager.createAllocation(request.getRequestId(), "HA001", "AS001",
                    dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));
            assertEquals("ALLOCATED", request.getStatus(), "request becomes ALLOCATED after a successful allocation");
        }
    }

    private void testRequestReturnsToApprovedAfterCancellation() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            AssetAllocation allocation = fixture.allocationManager.createAllocation(
                    request.getRequestId(), "HA001", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));
            fixture.allocationManager.cancelAllocation(allocation.getAllocationId(), "Patient rescheduled");
            assertEquals("APPROVED", request.getStatus(), "request returns to APPROVED after allocation cancellation");
        }
    }

    private void testRequestBecomesCompletedAfterCompletion() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            AssetAllocation allocation = fixture.allocationManager.createAllocation(
                    request.getRequestId(), "HA001", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));
            fixture.allocationManager.completeAllocation(allocation.getAllocationId());
            assertEquals("COMPLETED", request.getStatus(), "request becomes COMPLETED after allocation completion");
        }
    }

    // ---- tests: persistence ----

    private void testPersistenceSurvivesReload() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            AssetAllocation allocation = fixture.allocationManager.createAllocation(
                    request.getRequestId(), "HA001", "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));

            Reopened reopened = fixture.reopen();
            AssetAllocation reloadedAllocation = findById(reopened.allocationManager.getAllocations(),
                    allocation.getAllocationId());
            assertNotNull(reloadedAllocation, "allocation survives reload");
            assertEquals("HA001", reloadedAllocation.getAssetId(), "asset id survives reload");
            assertEquals(AssetAllocation.STATUS_SCHEDULED, reloadedAllocation.getAllocationStatus(),
                    "allocation status survives reload");

            MedicalRequest reloadedRequest = reopened.clinicalManager.findRequestById(request.getRequestId());
            assertNotNull(reloadedRequest, "request survives reload");
            assertEquals("ALLOCATED", reloadedRequest.getStatus(), "request status survives reload consistently");
        }
    }

    private void testMalformedAllocationRowsDoNotCrashLoading() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");

            writeFile(fixture.allocationsFile,
                    "#allocationId|requestId|assetId|allocatedByAdminId|startDateTime|endDateTime|status|createdDateTime|cancellationReason",
                    "TOO_FEW_FIELDS|" + request.getRequestId(),
                    "AL900|" + request.getRequestId() + "|HA001|AS001|not-a-date|2026-03-01 10:00:00|SCHEDULED|2026-03-01 08:00:00|",
                    "AL901|" + request.getRequestId() + "|HA001|AS001|2026-03-01 09:00:00|2026-03-01 08:00:00|SCHEDULED|2026-03-01 08:00:00|",
                    "AL902|" + request.getRequestId() + "|HA001|AS001|2026-03-02 09:00:00|2026-03-02 10:00:00|SCHEDULED|2026-03-02 08:00:00|");

            Reopened reopened = fixture.reopen();
            List<AssetAllocation> allocations = reopened.allocationManager.getAllocations();
            assertEquals(1, allocations.size(), "only the one structurally valid row should load");
            assertNotNull(findById(allocations, "AL902"), "the valid allocation row must load");
            assertNull(findById(allocations, "AL900"), "a row with an unparsable date must be skipped, not crash");
            assertNull(findById(allocations, "AL901"), "a row with end before start must be skipped, not crash");
        }
    }

    private void testLoadSkipsUnknownStatus() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            writeFile(fixture.allocationsFile,
                    "#allocationId|requestId|assetId|allocatedByAdminId|startDateTime|endDateTime|status|createdDateTime|cancellationReason",
                    "AL910|" + request.getRequestId() + "|HA001|AS001|2026-03-01 09:00:00|2026-03-01 10:00:00|BOGUS_STATUS|2026-03-01 08:00:00|");

            Reopened reopened = fixture.reopen();
            assertTrue(reopened.allocationManager.getAllocations().isEmpty(),
                    "a row with an unknown status must be skipped, not crash");
        }
    }

    private void testLoadSkipsCancelledWithoutReason() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            writeFile(fixture.allocationsFile,
                    "#allocationId|requestId|assetId|allocatedByAdminId|startDateTime|endDateTime|status|createdDateTime|cancellationReason",
                    "AL911|" + request.getRequestId() + "|HA001|AS001|2026-03-01 09:00:00|2026-03-01 10:00:00|CANCELLED|2026-03-01 08:00:00|");

            Reopened reopened = fixture.reopen();
            assertTrue(reopened.allocationManager.getAllocations().isEmpty(),
                    "a CANCELLED row without a reason must be skipped, not crash");
        }
    }

    private void testLoadSkipsNonCancelledRecordWithReason() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            writeFile(fixture.allocationsFile,
                    "#allocationId|requestId|assetId|allocatedByAdminId|startDateTime|endDateTime|status|createdDateTime|cancellationReason",
                    "AL912|" + request.getRequestId() + "|HA001|AS001|2026-03-01 09:00:00|2026-03-01 10:00:00|SCHEDULED|2026-03-01 08:00:00|Should not be here");

            Reopened reopened = fixture.reopen();
            assertTrue(reopened.allocationManager.getAllocations().isEmpty(),
                    "a non-cancelled row carrying a cancellation reason must be skipped, not crash");
        }
    }

    private void testLoadSkipsDuplicateAllocationIds() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            writeFile(fixture.allocationsFile,
                    "#allocationId|requestId|assetId|allocatedByAdminId|startDateTime|endDateTime|status|createdDateTime|cancellationReason",
                    "AL913|" + request.getRequestId() + "|HA001|AS001|2026-03-01 09:00:00|2026-03-01 10:00:00|SCHEDULED|2026-03-01 08:00:00|",
                    "AL913|" + request.getRequestId() + "|HA004|AS001|2026-03-02 09:00:00|2026-03-02 10:00:00|SCHEDULED|2026-03-02 08:00:00|");

            Reopened reopened = fixture.reopen();
            List<AssetAllocation> allocations = reopened.allocationManager.getAllocations();
            assertEquals(1, allocations.size(), "a duplicate allocation ID must be skipped, keeping only the first");
            assertEquals("HA001", findById(allocations, "AL913").getAssetId(),
                    "the first occurrence of a duplicate allocation ID must be the one kept");
        }
    }

    private void testAllocationWriteFailureDoesNotLeavePhantomAllocation() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");

            // The allocations file doesn't exist yet (no allocation has been saved) - create it
            // first so setReadOnly() has a real file to lock.
            if (!fixture.allocationsFile.exists()) {
                fixture.allocationsFile.createNewFile();
            }
            boolean readOnlySet = fixture.allocationsFile.setReadOnly();
            assertTrue(readOnlySet, "test setup: could not mark the allocations file read-only");
            try {
                assertThrows(IOException.class,
                        () -> fixture.allocationManager.createAllocation(request.getRequestId(), "HA001", "AS001",
                                dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00")),
                        "createAllocation must propagate a file-write failure");
                assertTrue(fixture.allocationManager.getAllocations().isEmpty(),
                        "a failed allocation write must leave no phantom allocation in memory");
                assertEquals("APPROVED", request.getStatus(),
                        "the request must remain APPROVED since the allocation was never actually saved");
            } finally {
                fixture.allocationsFile.setWritable(true);
            }
        }
    }

    private void testRequestWriteFailureLeavesConsistentState() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");

            boolean readOnlySet = fixture.requestsFile.setReadOnly();
            assertTrue(readOnlySet, "test setup: could not mark the requests file read-only");
            try {
                assertThrows(IOException.class,
                        () -> fixture.allocationManager.createAllocation(request.getRequestId(), "HA001", "AS001",
                                dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00")),
                        "createAllocation must propagate a request-status write failure");
                assertTrue(fixture.allocationManager.getAllocations().isEmpty(),
                        "the allocation must be rolled back so the allocation file and request file stay consistent");
                assertEquals("APPROVED", request.getStatus(), "the request must remain APPROVED after the rollback");
            } finally {
                fixture.requestsFile.setWritable(true);
            }
        }
    }

    // ---- tests: Admin GUI ----

    /**
     * Safe, headless component-level check of the actual Admin panel: constructing JPanel/
     * JButton/JComboBox/JTable/GroupLayout needs no display. Verifies the Allocate Asset action,
     * the full status filter (including the two new ALLOCATED/COMPLETED options), and the
     * allocation-history row content - all without ever opening the modal dialog that normally
     * hosts them (buildAllocationHistoryModel is invoked directly instead).
     */
    private void testAdminGuiExposesAllocationControlsAndStatusFilter() throws Exception {
        try (Fixture fixture = new Fixture()) {
            SessionManager sessionManager = new SessionManager(fixture.userManager);
            sessionManager.login("admin1", "pass1234");

            MedicalRequestManagementPanel panel = new MedicalRequestManagementPanel(
                    fixture.clinicalManager, fixture.userManager, fixture.appointmentManager,
                    fixture.hospitalAssetManager, fixture.rateManager, fixture.insuranceManager,
                    fixture.allocationManager, sessionManager, null, () -> { });

            JButton allocateBtn = (JButton) getPrivateField(panel, "allocateBtn");
            assertNotNull(allocateBtn, "the Admin medical-request screen must expose an Allocate Asset button");
            assertEquals("Allocate Asset", allocateBtn.getText(), "the allocation action must be clearly labeled");
            assertTrue(allocateBtn.isEnabled(), "the Allocate Asset button must be enabled");

            JComboBox<?> statusCmb = (JComboBox<?>) getPrivateField(panel, "statusCmb");
            List<Object> statusOptions = new ArrayList<>();
            for (int i = 0; i < statusCmb.getItemCount(); i++) {
                statusOptions.add(statusCmb.getItemAt(i));
            }
            for (String expected : new String[]{"All", "PENDING", "APPROVED", "REJECTED", "ALLOCATED", "COMPLETED"}) {
                assertTrue(statusOptions.contains(expected), "status filter must include " + expected);
            }

            MedicalRequest request = fixture.createApprovedRequest("LAB", "Full blood count");
            AssetAllocation allocation = fixture.allocationManager.createAllocation(request.getRequestId(), "HA001",
                    "AS001", dt("2026-03-01 09:00:00"), dt("2026-03-01 10:00:00"));

            Method buildHistory = MedicalRequestManagementPanel.class
                    .getDeclaredMethod("buildAllocationHistoryModel", String.class);
            buildHistory.setAccessible(true);
            DefaultTableModel historyModel = (DefaultTableModel) buildHistory.invoke(panel, request.getRequestId());

            List<String> columns = new ArrayList<>();
            for (int i = 0; i < historyModel.getColumnCount(); i++) {
                columns.add(historyModel.getColumnName(i));
            }
            assertTrue(columns.contains("Allocation ID"), "allocation history must show the allocation ID");
            assertTrue(columns.contains("Asset"), "allocation history must show the asset");
            assertTrue(columns.contains("Start"), "allocation history must show the start time");
            assertTrue(columns.contains("End"), "allocation history must show the end time");
            assertTrue(columns.contains("Status"), "allocation history must show the status");
            assertEquals(1, historyModel.getRowCount(), "the created allocation appears in its request's history");
            assertEquals(allocation.getAllocationId(), historyModel.getValueAt(0, columns.indexOf("Allocation ID")),
                    "history row shows the allocation id");
            assertEquals("HA001", historyModel.getValueAt(0, columns.indexOf("Asset")), "history row shows the asset");
            assertEquals(AssetAllocation.STATUS_SCHEDULED, historyModel.getValueAt(0, columns.indexOf("Status")),
                    "history row shows the status");
        }
    }

    /**
     * A directory sitting where asset_allocations.txt is expected must fail construction clearly
     * (IllegalStateException), not silently load as an empty allocation list - the same rule
     * already proven for users.txt and hospital_assets.txt. The supporting managers (clinical,
     * hospital asset, user) all use normal temporary files, so the failure is isolated to the
     * allocation file itself.
     */
    private void testUnreadableAllocationFileIsReportedAsLoadFailure() throws Exception {
        Path tempDir = Files.createTempDirectory("hms-allocation-load-failure-test-");
        try {
            File usersFile = new File(tempDir.toFile(), "users.txt");
            File shiftsFile = new File(tempDir.toFile(), "doctor_shifts.txt");
            File appointmentsFile = new File(tempDir.toFile(), "appointments.txt");
            File typesFile = new File(tempDir.toFile(), "assessment_types.txt");
            File recordsFile = new File(tempDir.toFile(), "medical_records.txt");
            File prescriptionsFile = new File(tempDir.toFile(), "prescriptions.txt");
            File requestsFile = new File(tempDir.toFile(), "medical_requests.txt");
            File resultsFile = new File(tempDir.toFile(), "assessment_results.txt");
            File feedbackFile = new File(tempDir.toFile(), "feedback.txt");
            File assetsFile = new File(tempDir.toFile(), "hospital_assets.txt");

            File directoryInPlaceOfAllocationsFile = new File(tempDir.toFile(), "asset_allocations.txt");
            boolean created = directoryInPlaceOfAllocationsFile.mkdirs();
            assertTrue(created, "test setup: could not create the directory standing in for the allocation file");

            UserManager userManager = new UserManager(usersFile);
            DoctorShiftManager shiftManager = new DoctorShiftManager(userManager, shiftsFile);
            AppointmentManager appointmentManager = new AppointmentManager(userManager, shiftManager, appointmentsFile);
            AssessmentTypeManager typeManager = new AssessmentTypeManager(userManager, typesFile);
            ClinicalManager clinicalManager = new ClinicalManager(userManager, appointmentManager, typeManager,
                    recordsFile, prescriptionsFile, requestsFile, resultsFile, feedbackFile);
            HospitalAssetManager hospitalAssetManager = new HospitalAssetManager(userManager, assetsFile);

            boolean constructionFailed = false;
            String failureMessage = null;
            try {
                new AssetAllocationManager(clinicalManager, hospitalAssetManager, userManager,
                        directoryInPlaceOfAllocationsFile);
            } catch (IllegalStateException ex) {
                constructionFailed = true;
                failureMessage = ex.getMessage();
            }
            assertTrue(constructionFailed,
                    "an allocation file that is actually a directory must be reported as a load failure, not an empty allocation list");
            assertNotNull(failureMessage, "the load-failure exception must carry a message");
            assertTrue(failureMessage.contains(directoryInPlaceOfAllocationsFile.getPath()),
                    "the load-failure message must identify the allocation file path: " + failureMessage);
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private static Object getPrivateField(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }
}
