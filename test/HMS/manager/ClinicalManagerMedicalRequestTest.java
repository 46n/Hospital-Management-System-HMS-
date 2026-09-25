package hms.manager;

import hms.data.TextFileUtil;
import hms.model.MedicalRecord;
import hms.model.MedicalRequest;
import hms.ui.common.MainFrame;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * Standalone, dependency-free lifecycle tests for the Admin medical-request
 * workflow (ClinicalManager.addRequest/getAllRequests/approveRequest/
 * rejectRequest, plus TextFileUtil.writeLinesChecked). The project has no
 * JUnit on its classpath (a plain-Java, no-framework coursework project), so
 * this is a small hand-rolled runner instead: each testXxx() method is
 * discovered by name, run in isolation, and reported as PASS/FAIL.
 *
 * Every test works against its own temporary directory (never the real
 * data/ directory) and deletes it afterward.
 *
 * Run with (from the project root, after compiling src to build/classes):
 *   javac -d build/test-classes -cp build/classes test/HMS/manager/ClinicalManagerMedicalRequestTest.java
 *   java -cp build/classes;build/test-classes hms.manager.ClinicalManagerMedicalRequestTest
 */
public class ClinicalManagerMedicalRequestTest {

    private static final SimpleDateFormat PROCESSED_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int passed = 0;
    private int failed = 0;

    public static void main(String[] args) throws Exception {
        ClinicalManagerMedicalRequestTest suite = new ClinicalManagerMedicalRequestTest();
        suite.run("testLoadingLegacySixFieldRecord", suite::testLoadingLegacySixFieldRecord);
        suite.run("testMalformedRecordsAreSkippedNotCrashed", suite::testMalformedRecordsAreSkippedNotCrashed);
        suite.run("testImpossibleRequestDateIsRejectedAsMalformed", suite::testImpossibleRequestDateIsRejectedAsMalformed);
        suite.run("testCreatingRequestStartsPending", suite::testCreatingRequestStartsPending);
        suite.run("testCreatingLabRequest", suite::testCreatingLabRequest);
        suite.run("testCreatingXRayRequest", suite::testCreatingXRayRequest);
        suite.run("testCreatingSpecializedImagingRequest", suite::testCreatingSpecializedImagingRequest);
        suite.run("testApprovePendingRequest", suite::testApprovePendingRequest);
        suite.run("testRejectPendingRequestWithReason", suite::testRejectPendingRequestWithReason);
        suite.run("testRejectWithoutReasonIsRejectedByValidation", suite::testRejectWithoutReasonIsRejectedByValidation);
        suite.run("testApprovedRequestCannotBeProcessedAgain", suite::testApprovedRequestCannotBeProcessedAgain);
        suite.run("testRejectedRequestCannotBeApproved", suite::testRejectedRequestCannotBeApproved);
        suite.run("testDetailsSurvivePersistenceAndReloadWhilePending",
                suite::testDetailsSurvivePersistenceAndReloadWhilePending);
        suite.run("testPersistenceSurvivesReload", suite::testPersistenceSurvivesReload);
        suite.run("testRejectionSurvivesPersistenceAndReload", suite::testRejectionSurvivesPersistenceAndReload);
        suite.run("testAddRequestPropagatesWriteFailureAndLeavesNoPhantom",
                suite::testAddRequestPropagatesWriteFailureAndLeavesNoPhantom);
        suite.run("testWriteLinesCheckedPropagatesIOException", suite::testWriteLinesCheckedPropagatesIOException);
        suite.run("testTableRowFormattingIsSafeAndCorrect", suite::testTableRowFormattingIsSafeAndCorrect);
        suite.run("testActiveDoctorScreenTableIncludesDetailsAndStatus",
                suite::testActiveDoctorScreenTableIncludesDetailsAndStatus);

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

    /** One doctor, one patient, one appointment - enough for ClinicalManager.openRecord(). */
    private static final class Fixture implements AutoCloseable {
        final Path tempDir;
        final UserManager userManager;
        final AppointmentManager appointmentManager;
        final ClinicalManager clinicalManager;
        final File requestsFile;
        final File recordsFile;

        Fixture() throws IOException {
            tempDir = Files.createTempDirectory("hms-request-test-");
            File usersFile = new File(tempDir.toFile(), "users.txt");
            File shiftsFile = new File(tempDir.toFile(), "doctor_shifts.txt");
            File appointmentsFile = new File(tempDir.toFile(), "appointments.txt");
            File typesFile = new File(tempDir.toFile(), "assessment_types.txt");
            recordsFile = new File(tempDir.toFile(), "medical_records.txt");
            File prescriptionsFile = new File(tempDir.toFile(), "prescriptions.txt");
            requestsFile = new File(tempDir.toFile(), "medical_requests.txt");
            File resultsFile = new File(tempDir.toFile(), "assessment_results.txt");
            File feedbackFile = new File(tempDir.toFile(), "feedback.txt");

            writeFile(usersFile,
                    "#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId",
                    "DR001|DOCTOR|doctor1|pass1234|0111111111|doctor1@test.com|DR001|Cardiology|true|",
                    "PT001|PATIENT|patient1|pass1234|0122222222|patient1@test.com|PT001|1990-01-01|true|");
            writeFile(appointmentsFile,
                    "#appointmentId|patientId|doctorId|appointmentDate|appointmentTime|status",
                    "AP001|PT001|DR001|2026-01-10|09:00|SCHEDULED");

            userManager = new UserManager(usersFile);
            DoctorShiftManager shiftManager = new DoctorShiftManager(userManager, shiftsFile);
            appointmentManager = new AppointmentManager(userManager, shiftManager, appointmentsFile);
            AssessmentTypeManager typeManager = new AssessmentTypeManager(userManager, typesFile);
            clinicalManager = new ClinicalManager(userManager, appointmentManager, typeManager,
                    recordsFile, prescriptionsFile, requestsFile, resultsFile, feedbackFile);
        }

        /** Reopens a fresh ClinicalManager against the same on-disk files, to test reload. */
        ClinicalManager reopenClinicalManager() throws IOException {
            File usersFile = new File(tempDir.toFile(), "users.txt");
            File shiftsFile = new File(tempDir.toFile(), "doctor_shifts.txt");
            File appointmentsFile = new File(tempDir.toFile(), "appointments.txt");
            File typesFile = new File(tempDir.toFile(), "assessment_types.txt");
            File prescriptionsFile = new File(tempDir.toFile(), "prescriptions.txt");
            File resultsFile = new File(tempDir.toFile(), "assessment_results.txt");
            File feedbackFile = new File(tempDir.toFile(), "feedback.txt");
            UserManager freshUsers = new UserManager(usersFile);
            DoctorShiftManager freshShifts = new DoctorShiftManager(freshUsers, shiftsFile);
            AppointmentManager freshAppointments = new AppointmentManager(freshUsers, freshShifts, appointmentsFile);
            AssessmentTypeManager freshTypes = new AssessmentTypeManager(freshUsers, typesFile);
            return new ClinicalManager(freshUsers, freshAppointments, freshTypes,
                    recordsFile, prescriptionsFile, requestsFile, resultsFile, feedbackFile);
        }

        MedicalRequest createPendingRequest(String type, String details) throws IOException {
            MedicalRecord record = clinicalManager.getOrOpenRecord("AP001", "DR001");
            return clinicalManager.addRequest(record.getRecordId(), type, details);
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

    // ---- tests ----

    private void testLoadingLegacySixFieldRecord() throws Exception {
        try (Fixture fixture = new Fixture()) {
            // A record and a legacy 6-field request row (no details/rejectionReason/
            // processedBy/processedDateTime columns), written directly - as if saved
            // by the pre-Admin-feature version of the file format.
            writeFile(fixture.recordsFile,
                    "#recordId|appointmentId|patientId|doctorId|vitalSigns|consultationNotes|diagnosis|recordDate",
                    "MR100|AP001|PT001|DR001|BP 120/80|Routine check|Healthy|2026-01-10");
            writeFile(fixture.requestsFile,
                    "#requestId|recordId|doctorId|requestType|requestDate|status",
                    "MQ100|MR100|DR001|IMAGING|2026-01-11|PENDING");

            ClinicalManager reloaded = fixture.reopenClinicalManager();
            List<MedicalRequest> all = reloaded.getAllRequests();
            assertEquals(1, all.size(), "legacy request should load");
            MedicalRequest request = all.get(0);
            assertEquals("IMAGING", request.getRequestType(), "legacy request type");
            assertEquals("PENDING", request.getStatus(), "legacy request status");
            assertNull(request.getProcessedBy(), "legacy request has no processedBy");
            assertNull(request.getProcessedDate(), "legacy request has no processedDate");
        }
    }

    private void testMalformedRecordsAreSkippedNotCrashed() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.recordsFile,
                    "#recordId|appointmentId|patientId|doctorId|vitalSigns|consultationNotes|diagnosis|recordDate",
                    "MR100|AP001|PT001|DR001|BP 120/80|Routine check|Healthy|2026-01-10");
            writeFile(fixture.requestsFile,
                    "#requestId|recordId|doctorId|requestType|requestDate|status|details|rejectionReason|processedBy|processedDateTime",
                    "TOO_FEW_FIELDS|MR100",
                    "MQ200|UNKNOWN_RECORD|DR001|LAB|2026-01-11|PENDING",
                    "MQ201|MR100|UNKNOWN_DOCTOR|LAB|2026-01-11|PENDING",
                    "MQ202|MR100|DR001|LAB|not-a-date|PENDING",
                    "MQ203|MR100|DR001|LAB|2026-01-11|APPROVED|Chest scan||AS001|garbage-timestamp",
                    "MQ204|MR100|DR001|IMAGING|2026-01-12|PENDING||||");

            ClinicalManager reloaded = fixture.reopenClinicalManager();
            List<MedicalRequest> all = reloaded.getAllRequests();
            assertEquals(2, all.size(), "only the two structurally valid rows should load");

            MedicalRequest approved = findById(all, "MQ203");
            assertNotNull(approved, "MQ203 (valid core fields, bad processed timestamp) should still load");
            assertEquals("APPROVED", approved.getStatus(), "MQ203 status");
            assertEquals("AS001", approved.getProcessedBy(), "MQ203 processedBy still recorded");
            assertNull(approved.getProcessedDate(), "MQ203 processedDate left null since it was unparsable");

            MedicalRequest pending = findById(all, "MQ204");
            assertNotNull(pending, "MQ204 (valid, blank trailing fields) should load");
            assertEquals("PENDING", pending.getStatus(), "MQ204 status");
        }
    }

    private void testImpossibleRequestDateIsRejectedAsMalformed() throws Exception {
        try (Fixture fixture = new Fixture()) {
            writeFile(fixture.recordsFile,
                    "#recordId|appointmentId|patientId|doctorId|vitalSigns|consultationNotes|diagnosis|recordDate",
                    "MR100|AP001|PT001|DR001|BP 120/80|Routine check|Healthy|2026-01-10");
            writeFile(fixture.requestsFile,
                    "#requestId|recordId|doctorId|requestType|requestDate|status",
                    "MQ300|MR100|DR001|LAB|2026-02-30|PENDING",
                    "MQ301|MR100|DR001|LAB|2026-02-28|PENDING");

            ClinicalManager reloaded = fixture.reopenClinicalManager();
            List<MedicalRequest> all = reloaded.getAllRequests();
            assertNotNull(findById(all, "MQ301"), "a genuinely valid date (2026-02-28) must still load");
            assertNull(findById(all, "MQ300"),
                    "an impossible calendar date (2026-02-30) must be rejected, not silently rolled over");
            assertEquals(1, all.size(), "only the request with a valid date should load");
        }
    }

    private void testCreatingRequestStartsPending() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createPendingRequest("LAB", "Full blood count");
            assertEquals("PENDING", request.getStatus(), "a newly submitted request must start PENDING");
            assertEquals("Full blood count", request.getDetails(), "details are preserved");
            assertTrue(fixture.clinicalManager.getAllRequests().contains(request), "request is visible to Admin");
        }
    }

    private void testCreatingLabRequest() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createPendingRequest("LAB", "Full blood count");
            assertEquals("LAB", request.getRequestType(), "LAB category stored as-is");
        }
    }

    private void testCreatingXRayRequest() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createPendingRequest("X_RAY", "Right wrist, two views");
            assertEquals("X_RAY", request.getRequestType(), "X_RAY category stored as-is");
        }
    }

    private void testCreatingSpecializedImagingRequest() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createPendingRequest("SPECIALIZED_IMAGING", "MRI, lumbar spine");
            assertEquals("SPECIALIZED_IMAGING", request.getRequestType(), "SPECIALIZED_IMAGING category stored as-is");
        }
    }

    private void testApprovePendingRequest() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createPendingRequest("LAB", "Full blood count");
            fixture.clinicalManager.approveRequest(request.getRequestId(), "AS001");
            assertEquals("APPROVED", request.getStatus(), "approved status");
            assertEquals("AS001", request.getProcessedBy(), "processing admin recorded");
            assertNotNull(request.getProcessedDate(), "processed date/time recorded");
            assertNull(request.getRejectionReason(), "approved request has no rejection reason");
        }
    }

    private void testRejectPendingRequestWithReason() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createPendingRequest("IMAGING", "Chest X-ray");
            fixture.clinicalManager.rejectRequest(request.getRequestId(), "AS001", "Not clinically justified");
            assertEquals("REJECTED", request.getStatus(), "rejected status");
            assertEquals("Not clinically justified", request.getRejectionReason(), "rejection reason recorded");
            assertEquals("AS001", request.getProcessedBy(), "processing admin recorded");
            assertNotNull(request.getProcessedDate(), "processed date/time recorded");
        }
    }

    private void testRejectWithoutReasonIsRejectedByValidation() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createPendingRequest("LAB", "Full blood count");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.clinicalManager.rejectRequest(request.getRequestId(), "AS001", ""),
                    "rejecting with a blank reason must be rejected");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.clinicalManager.rejectRequest(request.getRequestId(), "AS001", "   "),
                    "rejecting with a whitespace-only reason must be rejected");
            assertThrows(IllegalArgumentException.class,
                    () -> fixture.clinicalManager.rejectRequest(request.getRequestId(), "AS001", null),
                    "rejecting with a null reason must be rejected");
            assertEquals("PENDING", request.getStatus(), "request is untouched after a failed rejection attempt");
        }
    }

    private void testApprovedRequestCannotBeProcessedAgain() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createPendingRequest("LAB", "Full blood count");
            fixture.clinicalManager.approveRequest(request.getRequestId(), "AS001");
            assertThrows(IllegalStateException.class,
                    () -> fixture.clinicalManager.approveRequest(request.getRequestId(), "AS001"),
                    "an already-approved request must not be approved again (repeated click / no PENDING->PENDING)");
            assertThrows(IllegalStateException.class,
                    () -> fixture.clinicalManager.rejectRequest(request.getRequestId(), "AS001", "changed my mind"),
                    "an already-approved request must not be rejected");
        }
    }

    private void testRejectedRequestCannotBeApproved() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createPendingRequest("LAB", "Full blood count");
            fixture.clinicalManager.rejectRequest(request.getRequestId(), "AS001", "Duplicate request");
            assertThrows(IllegalStateException.class,
                    () -> fixture.clinicalManager.approveRequest(request.getRequestId(), "AS001"),
                    "an already-rejected request must not be approved");
        }
    }

    private void testPersistenceSurvivesReload() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createPendingRequest("IMAGING", "MRI of left knee");
            fixture.clinicalManager.approveRequest(request.getRequestId(), "AS001");
            String expectedProcessed = PROCESSED_FORMAT.format(request.getProcessedDate());

            ClinicalManager reloaded = fixture.reopenClinicalManager();
            MedicalRequest reloadedRequest = findById(reloaded.getAllRequests(), request.getRequestId());
            assertNotNull(reloadedRequest, "approved request survives reload");
            assertEquals("APPROVED", reloadedRequest.getStatus(), "status survives reload");
            assertEquals("AS001", reloadedRequest.getProcessedBy(), "processedBy survives reload");
            assertEquals("MRI of left knee", reloadedRequest.getDetails(), "details survive reload");
            assertNotNull(reloadedRequest.getProcessedDate(), "processedDate survives reload");
            assertEquals(expectedProcessed, PROCESSED_FORMAT.format(reloadedRequest.getProcessedDate()),
                    "processedDate round-trips to the same second");
        }
    }

    private void testDetailsSurvivePersistenceAndReloadWhilePending() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createPendingRequest("X_RAY", "Left ankle, weight-bearing view");

            ClinicalManager reloaded = fixture.reopenClinicalManager();
            MedicalRequest reloadedRequest = findById(reloaded.getAllRequests(), request.getRequestId());
            assertNotNull(reloadedRequest, "pending request survives reload");
            assertEquals("Left ankle, weight-bearing view", reloadedRequest.getDetails(),
                    "details survive reload while still pending");
            assertEquals("PENDING", reloadedRequest.getStatus(), "status is unaffected by reload");
        }
    }

    private void testRejectionSurvivesPersistenceAndReload() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createPendingRequest("LAB", "Lipid panel");
            fixture.clinicalManager.rejectRequest(request.getRequestId(), "AS001", "Duplicate of an existing order");

            ClinicalManager reloaded = fixture.reopenClinicalManager();
            MedicalRequest reloadedRequest = findById(reloaded.getAllRequests(), request.getRequestId());
            assertNotNull(reloadedRequest, "rejected request survives reload");
            assertEquals("REJECTED", reloadedRequest.getStatus(), "status survives reload");
            assertEquals("Duplicate of an existing order", reloadedRequest.getRejectionReason(),
                    "rejection reason survives reload");
            assertEquals("AS001", reloadedRequest.getProcessedBy(), "processedBy survives reload");
            assertNotNull(reloadedRequest.getProcessedDate(), "processedDate survives reload");
        }
    }

    private void testAddRequestPropagatesWriteFailureAndLeavesNoPhantom() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest existing = fixture.createPendingRequest("LAB", "Existing request");
            MedicalRecord record = fixture.clinicalManager.getOrOpenRecord("AP001", "DR001");
            assertEquals(1, record.getMedicalRequests().size(), "one request exists before the failure");

            boolean readOnlySet = fixture.requestsFile.setReadOnly();
            assertTrue(readOnlySet, "test setup: could not mark the requests file read-only");
            try {
                assertThrows(IOException.class,
                        () -> fixture.clinicalManager.addRequest(record.getRecordId(), "X_RAY", "Should not be saved"),
                        "addRequest must propagate a file-write failure to the caller");

                assertEquals(1, record.getMedicalRequests().size(),
                        "a failed addRequest must leave no phantom request on the in-memory record");
                List<MedicalRequest> all = fixture.clinicalManager.getAllRequests();
                assertEquals(1, all.size(), "Admin's view must not include the failed request either");
                assertEquals(existing.getRequestId(), all.get(0).getRequestId(),
                        "the pre-existing request is unchanged after the failed write");
                assertEquals("PENDING", all.get(0).getStatus(), "pre-existing request status is unchanged");
            } finally {
                fixture.requestsFile.setWritable(true);
            }
        }
    }

    private void testWriteLinesCheckedPropagatesIOException() throws Exception {
        Path tempDir = Files.createTempDirectory("hms-writecheck-test-");
        try {
            // A regular file used as a "directory" makes any write beneath it fail -
            // a portable way to force an IOException without touching permissions.
            File blocker = new File(tempDir.toFile(), "blocker");
            Files.write(blocker.toPath(), List.of("not a directory"), StandardCharsets.UTF_8);
            File unwritableTarget = new File(blocker, "medical_requests.txt");

            assertThrows(IOException.class,
                    () -> TextFileUtil.writeLinesChecked(unwritableTarget, "#header", List.of("a|b|c")),
                    "writeLinesChecked must propagate a write failure instead of hiding it");

            // The plain (legacy) writeLines() intentionally swallows the same failure -
            // confirming the two behave differently is the point of this feature.
            TextFileUtil.writeLines(unwritableTarget, "#header", List.of("a|b|c"));
            assertTrue(!unwritableTarget.exists(), "writeLines() did not actually save, and did not throw either");
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private void testTableRowFormattingIsSafeAndCorrect() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createPendingRequest("LAB", "Full blood count");

            // A safe, headless Swing component-level check: build the same DefaultTableModel
            // the Admin panel builds, from the same row-formatting helper, and verify it
            // without ever showing a window.
            DefaultTableModel model = new DefaultTableModel(
                    hms.ui.common.MedicalRequestTableUtil.COLUMN_NAMES, 0);
            model.addRow(hms.ui.common.MedicalRequestTableUtil.toRow(request));

            assertEquals(7, model.getColumnCount(), "Admin table has all required columns");
            assertEquals(request.getRequestId(), model.getValueAt(0, 0), "request id column");
            assertEquals("patient1", model.getValueAt(0, 1), "patient column");
            assertEquals("doctor1", model.getValueAt(0, 2), "doctor column");
            assertEquals("LAB", model.getValueAt(0, 3), "type column");
            assertEquals("Full blood count", model.getValueAt(0, 4), "details column");
            assertEquals("PENDING", model.getValueAt(0, 6), "status column");
        }
    }

    /**
     * Component-level check of the actual active Doctor screen (Main -&gt; DoctorDashboardNew
     * -&gt; RequestLabImagingNew), not just the shared row-formatting helper. Runs headless -
     * building JPanel/JComboBox/JTable/GroupLayout needs no display, and mainFrame is passed
     * as null since only the (unclicked) back button would ever use it.
     *
     * Constructed via reflection: RequestLabImagingNew lives in the (pre-existing, oddly-cased)
     * "HMS.ui.doctor" package, which a case-insensitive filesystem collapses into the same
     * directory as "hms.ui.doctor" - javac's cross-compilation-unit classpath lookup rejects
     * that case mismatch even though a single combined compile does not. Reflection resolves
     * the class by its exact runtime name instead of a source-level import, sidestepping that
     * without touching (or renaming) the pre-existing package declaration.
     */
    private void testActiveDoctorScreenTableIncludesDetailsAndStatus() throws Exception {
        try (Fixture fixture = new Fixture()) {
            MedicalRequest request = fixture.createPendingRequest("X_RAY", "Chest, PA and lateral");

            SessionManager sessionManager = new SessionManager(fixture.userManager);
            sessionManager.login("doctor1", "pass1234");
            AssessmentTypeManager typeManager = new AssessmentTypeManager(fixture.userManager,
                    new File(fixture.tempDir.toFile(), "assessment_types.txt"));

            Class<?> panelClass = Class.forName("HMS.ui.doctor.RequestLabImagingNew");
            Constructor<?> constructor = panelClass.getConstructor(ClinicalManager.class, AppointmentManager.class,
                    AssessmentTypeManager.class, UserManager.class, SessionManager.class, MainFrame.class,
                    Runnable.class);
            Object panel = constructor.newInstance(fixture.clinicalManager, fixture.appointmentManager, typeManager,
                    fixture.userManager, sessionManager, null, (Runnable) () -> { });

            JTable table = (JTable) getPrivateField(panel, "tblRequests");
            TableModel model = table.getModel();

            List<String> columnNames = new ArrayList<>();
            for (int i = 0; i < model.getColumnCount(); i++) {
                columnNames.add(model.getColumnName(i));
            }
            assertTrue(columnNames.contains("Details"), "active Doctor request table must show Details");
            assertTrue(columnNames.contains("Status"), "active Doctor request table must show Status");

            boolean foundRow = false;
            for (int row = 0; row < model.getRowCount(); row++) {
                if (request.getRequestId().equals(model.getValueAt(row, 0))) {
                    assertEquals(request.getDetails(), model.getValueAt(row, columnNames.indexOf("Details")),
                            "submitted details appear in the Doctor's own table");
                    assertEquals("PENDING", model.getValueAt(row, columnNames.indexOf("Status")),
                            "current status appears in the Doctor's own table");
                    foundRow = true;
                }
            }
            assertTrue(foundRow, "the newly submitted request must be present in the active Doctor screen's table");
        }
    }

    private static Object getPrivateField(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static MedicalRequest findById(List<MedicalRequest> requests, String requestId) {
        for (MedicalRequest request : requests) {
            if (request.getRequestId().equals(requestId)) {
                return request;
            }
        }
        return null;
    }
}
