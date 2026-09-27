package hms.integration;

import hms.manager.AppointmentManager;
import hms.manager.AssessmentTypeManager;
import hms.manager.AssetAllocationManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalDepartmentManager;
import hms.manager.ClinicalManager;
import hms.manager.ConsultationRateManager;
import hms.manager.DoctorShiftManager;
import hms.manager.HospitalAssetManager;
import hms.manager.InsuranceNetworkManager;
import hms.manager.ReportManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;

import hms.model.AdminStaff;
import hms.model.Appointment;
import hms.model.AssessmentResult;
import hms.model.AssessmentType;
import hms.model.AssetAllocation;
import hms.model.ConsultationRate;
import hms.model.Doctor;
import hms.model.Feedback;
import hms.model.HospitalAsset;
import hms.model.InsuranceNetwork;
import hms.model.MedicalManager;
import hms.model.MedicalRecord;
import hms.model.MedicalRequest;
import hms.model.Patient;
import hms.model.Payment;
import hms.model.Prescription;

import hms.ui.admin.AppointmentManagementPanel;
import hms.ui.admin.DoctorManagementPanel;
import hms.ui.admin.HospitalAssetManagementPanel;
import hms.ui.admin.MedicalRequestManagementPanel;
import hms.ui.admin.UserManagementPanel;
import hms.ui.common.EditProfileNew;
import hms.ui.common.MainFrame;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Phase 2: proves the merged Admin, Medical Manager, Doctor and Patient implementations all
 * operate on one shared object/file state - an action started in one role is visible, correctly,
 * from every other role's managers and panels, and every relationship survives a full reload from
 * the same temporary text files.
 *
 * Same hand-rolled runner style as the rest of this project's tests (no JUnit on the classpath):
 * each testXxx() method is discovered by name, run in isolation, and reported as PASS/FAIL. Every
 * scenario works against its own Files.createTempDirectory(...) directory and deletes it
 * afterward - the real data/ directory is only ever copied from, never written to.
 *
 * Run with (from the project root, after compiling src to build/classes):
 *   javac -d build/test-classes -cp build/classes test/HMS/integration/CrossRoleIntegrationTest.java
 *   java -cp build/classes;build/test-classes hms.integration.CrossRoleIntegrationTest
 */
public class CrossRoleIntegrationTest {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static {
        DATE_FORMAT.setLenient(false);
        DATETIME_FORMAT.setLenient(false);
    }

    private int passed = 0;
    private int failed = 0;

    public static void main(String[] args) throws Exception {
        CrossRoleIntegrationTest suite = new CrossRoleIntegrationTest();

        // ---- Scenario 1: Admin -> Medical Manager -> Doctor -> Patient ----
        suite.run("testAdminAssignsDoctorThenPatientBooksAppointmentVisibleToAllRoles",
                suite::testAdminAssignsDoctorThenPatientBooksAppointmentVisibleToAllRoles);

        // ---- Scenario 2: Doctor request -> Admin processing ----
        suite.run("testDoctorLabRequestThroughAdminApprovalAllocationAndCompletion",
                suite::testDoctorLabRequestThroughAdminApprovalAllocationAndCompletion);
        suite.run("testDoctorXRayRequestThroughAdminApprovalAllocationAndCompletion",
                suite::testDoctorXRayRequestThroughAdminApprovalAllocationAndCompletion);

        // ---- Scenario 3: Doctor clinical data -> Patient view ----
        suite.run("testDoctorClinicalDataVisibleToPatientAfterFinalization",
                suite::testDoctorClinicalDataVisibleToPatientAfterFinalization);

        // ---- Scenario 4: Admin configuration used by other roles ----
        suite.run("testAdminConsultationRateConsumedByBillingOutsideAdminPanel",
                suite::testAdminConsultationRateConsumedByBillingOutsideAdminPanel);
        suite.run("testInsuranceCoverageNotYetConsumedByBillingDocumentedLimitation",
                suite::testInsuranceCoverageNotYetConsumedByBillingDocumentedLimitation);

        // ---- Scenario 5: shared EditProfileNew across all roles ----
        suite.run("testAllFourRoleEntryPointsOpenSharedEditProfileNew",
                suite::testAllFourRoleEntryPointsOpenSharedEditProfileNew);
        suite.run("testUpdateOwnProfileAcrossTwoRolesPersistsAfterReload",
                suite::testUpdateOwnProfileAcrossTwoRolesPersistsAfterReload);
        suite.run("testRejectedPasswordChangeLeavesDoctorProfileUnchanged",
                suite::testRejectedPasswordChangeLeavesDoctorProfileUnchanged);

        // ---- Scenario 6: Admin forms reflect other-role actions ----
        suite.run("testAdminPanelsReflectOtherRoleActions", suite::testAdminPanelsReflectOtherRoleActions);

        // ---- Scenario 7: merged main demonstration data ----
        suite.run("testMergedDemonstrationDataLoadsCompletelyThroughManagerGraph",
                suite::testMergedDemonstrationDataLoadsCompletelyThroughManagerGraph);
        suite.run("testMergedDemonstrationDataSupportsSafeMutationsAndReload",
                suite::testMergedDemonstrationDataSupportsSafeMutationsAndReload);
        suite.run("testSyntheticDuplicateAssessmentTypeIdIsSkippedNotCrashed",
                suite::testSyntheticDuplicateAssessmentTypeIdIsSkippedNotCrashed);

        // ---- File-safety checks ----
        suite.run("testRejectedBookingLeavesAppointmentFileUnchanged",
                suite::testRejectedBookingLeavesAppointmentFileUnchanged);
        suite.run("testRejectedAllocationLeavesRequestAndAllocationFilesUnchanged",
                suite::testRejectedAllocationLeavesRequestAndAllocationFilesUnchanged);

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

    // ---- small date/time helpers ----

    private static Date date(String value) {
        try {
            return DATE_FORMAT.parse(value);
        } catch (ParseException e) {
            throw new IllegalStateException("Bad fixture date: " + value, e);
        }
    }

    private static Date dateTime(String value) {
        try {
            return DATETIME_FORMAT.parse(value);
        } catch (ParseException e) {
            throw new IllegalStateException("Bad fixture date/time: " + value, e);
        }
    }

    // ---- the full manager graph, wired in Main.java's dependency order ----

    private static final class Environment implements AutoCloseable {
        final Path tempDir;
        final File usersFile;
        final File shiftsFile;
        final File appointmentsFile;
        final File typesFile;
        final File recordsFile;
        final File prescriptionsFile;
        final File requestsFile;
        final File resultsFile;
        final File feedbackFile;
        final File assetsFile;
        final File allocationsFile;
        final File ratesFile;
        final File insuranceFile;
        final File departmentsFile;
        final File paymentsFile;

        UserManager userManager;
        DoctorShiftManager shiftManager;
        AppointmentManager appointmentManager;
        AssessmentTypeManager typeManager;
        ClinicalManager clinicalManager;
        BillingManager billingManager;
        ReportManager reportManager;
        HospitalAssetManager assetManager;
        ConsultationRateManager rateManager;
        InsuranceNetworkManager insuranceManager;
        ClinicalDepartmentManager departmentManager;
        AssetAllocationManager allocationManager;
        SessionManager sessionManager;

        Environment() throws IOException {
            tempDir = Files.createTempDirectory("hms-crossrole-test-");
            usersFile = new File(tempDir.toFile(), "users.txt");
            shiftsFile = new File(tempDir.toFile(), "doctor_shifts.txt");
            appointmentsFile = new File(tempDir.toFile(), "appointments.txt");
            typesFile = new File(tempDir.toFile(), "assessment_types.txt");
            recordsFile = new File(tempDir.toFile(), "medical_records.txt");
            prescriptionsFile = new File(tempDir.toFile(), "prescriptions.txt");
            requestsFile = new File(tempDir.toFile(), "medical_requests.txt");
            resultsFile = new File(tempDir.toFile(), "assessment_results.txt");
            feedbackFile = new File(tempDir.toFile(), "feedback.txt");
            assetsFile = new File(tempDir.toFile(), "hospital_assets.txt");
            allocationsFile = new File(tempDir.toFile(), "asset_allocations.txt");
            ratesFile = new File(tempDir.toFile(), "consultation_rates.txt");
            insuranceFile = new File(tempDir.toFile(), "insurance_networks.txt");
            departmentsFile = new File(tempDir.toFile(), "clinical_departments.txt");
            paymentsFile = new File(tempDir.toFile(), "payments.txt");
            buildManagers();
        }

        /** Same dependency order Main.java uses. */
        private void buildManagers() {
            userManager = new UserManager(usersFile);
            shiftManager = new DoctorShiftManager(userManager, shiftsFile);
            appointmentManager = new AppointmentManager(userManager, shiftManager, appointmentsFile);
            typeManager = new AssessmentTypeManager(userManager, typesFile);
            clinicalManager = new ClinicalManager(userManager, appointmentManager, typeManager,
                    recordsFile, prescriptionsFile, requestsFile, resultsFile, feedbackFile);
            billingManager = new BillingManager(appointmentManager, paymentsFile);
            reportManager = new ReportManager();
            assetManager = new HospitalAssetManager(userManager, assetsFile);
            rateManager = new ConsultationRateManager(userManager, ratesFile);
            clinicalManager.configureBilling(billingManager, rateManager);
            insuranceManager = new InsuranceNetworkManager(userManager, insuranceFile);
            departmentManager = new ClinicalDepartmentManager(userManager, departmentsFile);
            allocationManager = new AssetAllocationManager(clinicalManager, assetManager, userManager, allocationsFile);
            sessionManager = new SessionManager(userManager);
        }

        /** Rebuilds every manager from the same temporary files, discarding all in-memory state. */
        void reloadEverything() {
            buildManagers();
        }

        @Override
        public void close() throws IOException {
            deleteRecursively(tempDir);
        }
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

    private static List<String> readFileLines(File file) throws IOException {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
    }

    // ---- registration helpers ----

    private static AdminStaff registerAdmin(Environment env, String username) {
        return (AdminStaff) env.userManager.registerUser(
                new AdminStaff(null, username, "pass1234", "0111111111", username + "@test.com", "ADMIN_STAFF", null));
    }

    private static MedicalManager registerManager(Environment env, String username) {
        return (MedicalManager) env.userManager.registerUser(
                new MedicalManager(null, username, "pass1234", "0122222222", username + "@test.com",
                        "MEDICAL_MANAGER", null));
    }

    private static Doctor registerDoctor(Environment env, String username, String specialty) {
        return (Doctor) env.userManager.registerUser(
                new Doctor(null, username, "pass1234", "0133333333", username + "@test.com", "DOCTOR", null, specialty));
    }

    private static Patient registerPatient(Environment env, String username) {
        return (Patient) env.userManager.registerUser(
                new Patient(null, username, "pass1234", "0144444444", username + "@test.com", "PATIENT", null,
                        date("1990-01-01")));
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

    private static Object getPrivateField(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    /** A MainFrame that never shows a real window - it just remembers the last panel it was asked to display. */
    private static final class RecordingMainFrame extends MainFrame {
        private JPanel lastPanel;

        @Override
        public void showPanel(JPanel panel) {
            lastPanel = panel;
        }
    }

    // =====================================================================================
    // Scenario 1: Admin -> Medical Manager -> Doctor -> Patient
    // =====================================================================================

    private void testAdminAssignsDoctorThenPatientBooksAppointmentVisibleToAllRoles() throws Exception {
        try (Environment env = new Environment()) {
            AdminStaff admin = registerAdmin(env, "admin1");
            MedicalManager manager = registerManager(env, "manager1");
            Doctor doctor = registerDoctor(env, "doctor1", "Cardiology");
            Patient patient = registerPatient(env, "patient1");

            // Step 1-2: Admin assigns the Doctor to the Medical Manager; confirm both sides.
            env.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());
            assertEquals(manager.getManagerId(), doctor.getShiftManager().getManagerId(),
                    "the doctor must point at the manager Admin assigned");
            assertTrue(manager.getManagedDoctors().contains(doctor), "the manager must list the assigned doctor");

            // Step 3: Medical Manager creates a valid Doctor shift.
            Date shiftDate = date("2027-06-01");
            env.shiftManager.createShift(manager.getManagerId(), doctor.getDoctorId(), shiftDate,
                    LocalTime.parse("09:00"), LocalTime.parse("17:00"));

            // Step 4: Patient obtains an available consultation slot.
            List<LocalTime> availableSlots = env.appointmentManager.getAvailableSlots(doctor.getDoctorId(), shiftDate);
            assertTrue(!availableSlots.isEmpty(), "at least one slot must be available within the new shift");
            LocalTime chosenSlot = availableSlots.get(0);

            // Step 5: Patient books an appointment.
            Appointment appointment = env.appointmentManager.book(
                    patient.getPatientId(), doctor.getDoctorId(), shiftDate, chosenSlot);
            assertEquals("CONFIRMED", appointment.getStatus(), "a newly booked appointment must start CONFIRMED");

            // Step 6: confirm every view agrees.
            List<Appointment> patientView = env.appointmentManager.listForPatient(patient.getPatientId());
            assertEquals(1, patientView.size(), "the patient must see exactly the one appointment");
            assertEquals(appointment.getAppointmentId(), patientView.get(0).getAppointmentId(), "patient view ID must agree");

            List<Appointment> doctorView = env.appointmentManager.listForDoctor(doctor.getDoctorId());
            assertEquals(1, doctorView.size(), "the doctor must see exactly the one appointment");
            assertEquals(appointment.getAppointmentId(), doctorView.get(0).getAppointmentId(), "doctor view ID must agree");
            assertEquals("CONFIRMED", doctorView.get(0).getStatus(), "doctor view status must agree");

            RecordingMainFrame mainFrame = new RecordingMainFrame();
            AppointmentManagementPanel adminPanel = new AppointmentManagementPanel(env.appointmentManager,
                    env.userManager, env.clinicalManager, env.assetManager, env.rateManager, env.insuranceManager,
                    env.allocationManager, env.sessionManager, mainFrame, () -> { });
            JTable adminTable = (JTable) getPrivateField(adminPanel, "appointmentTable");
            boolean foundInAdminTable = false;
            for (int row = 0; row < adminTable.getModel().getRowCount(); row++) {
                if (appointment.getAppointmentId().equals(adminTable.getModel().getValueAt(row, 0))) {
                    foundInAdminTable = true;
                }
            }
            assertTrue(foundInAdminTable, "Admin's AppointmentManagementPanel table must display the Patient-booked appointment");

            // Step 7-8: reload UserManager, DoctorShiftManager and AppointmentManager, confirm links survive.
            UserManager reloadedUsers = new UserManager(env.usersFile);
            DoctorShiftManager reloadedShifts = new DoctorShiftManager(reloadedUsers, env.shiftsFile);
            AppointmentManager reloadedAppointments = new AppointmentManager(reloadedUsers, reloadedShifts, env.appointmentsFile);

            Doctor reloadedDoctor = reloadedUsers.findDoctorById(doctor.getDoctorId());
            assertNotNull(reloadedDoctor.getShiftManager(), "the reloaded doctor must still have an assigned manager");
            assertEquals(manager.getManagerId(), reloadedDoctor.getShiftManager().getManagerId(),
                    "the reloaded doctor-manager assignment must match");
            assertEquals(1, reloadedShifts.getShiftsForDoctor(doctor.getDoctorId()).size(),
                    "the reloaded shift must still exist for the doctor");
            List<Appointment> reloadedAppointmentsForPatient = reloadedAppointments.listForPatient(patient.getPatientId());
            assertEquals(1, reloadedAppointmentsForPatient.size(), "the reloaded appointment must still exist for the patient");
            assertEquals("CONFIRMED", reloadedAppointmentsForPatient.get(0).getStatus(),
                    "the reloaded appointment status must be unchanged");
        }
    }

    // =====================================================================================
    // Scenario 2: Doctor request -> Admin processing
    // =====================================================================================

    /** Shared setup for both Scenario 2 tests: one assigned Doctor with a booked appointment. */
    private static final class RequestScenarioFixture {
        final Environment env;
        final AdminStaff admin;
        final Doctor doctor;
        final Patient patient;
        final Appointment appointment;

        RequestScenarioFixture() throws Exception {
            env = new Environment();
            admin = registerAdmin(env, "admin1");
            MedicalManager manager = registerManager(env, "manager1");
            doctor = registerDoctor(env, "doctor1", "Radiology");
            patient = registerPatient(env, "patient1");
            env.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());
            Date shiftDate = date("2027-06-01");
            env.shiftManager.createShift(manager.getManagerId(), doctor.getDoctorId(), shiftDate,
                    LocalTime.parse("09:00"), LocalTime.parse("17:00"));
            appointment = env.appointmentManager.book(patient.getPatientId(), doctor.getDoctorId(), shiftDate,
                    LocalTime.parse("09:00"));
        }

        void close() throws IOException {
            env.close();
        }
    }

    private void testDoctorLabRequestThroughAdminApprovalAllocationAndCompletion() throws Exception {
        RequestScenarioFixture fixture = new RequestScenarioFixture();
        try {
            Environment env = fixture.env;

            // Step 1-3: Doctor opens the record, records vitals/notes, creates a LAB request with details.
            MedicalRecord record = env.clinicalManager.getOrOpenRecord(
                    fixture.appointment.getAppointmentId(), fixture.doctor.getDoctorId());
            env.clinicalManager.updateVitalsAndNotes(record.getRecordId(), "BP 120/80", "Routine check", "No complaints");
            MedicalRequest labRequest = env.clinicalManager.addRequest(record.getRecordId(), "LAB", "Full blood count");
            assertEquals("PENDING", labRequest.getStatus(), "a newly created request must start PENDING");

            // Step 4: RequestLabImagingNew must be able to display id/type/details/status.
            assertRequestVisibleOnDoctorScreen(env, fixture.doctor, labRequest, "Full blood count", "PENDING");

            // Step 5-6: Admin's MedicalRequestManagementPanel must immediately show the request.
            assertRequestVisibleOnAdminScreen(env, labRequest, "Full blood count");

            // Step 7: Admin approves.
            env.clinicalManager.approveRequest(labRequest.getRequestId(), fixture.admin.getStaffId());
            assertEquals("APPROVED", labRequest.getStatus(), "an approved request must become APPROVED");

            // Step 8-9: Admin creates a compatible LAB asset and allocates it for a valid, non-overlapping period.
            HospitalAsset labAsset = env.assetManager.createAsset("Lab Room 1", "LAB", "Ground Floor", true,
                    fixture.admin.getStaffId());
            AssetAllocation allocation = env.allocationManager.createAllocation(labRequest.getRequestId(),
                    labAsset.getAssetId(), fixture.admin.getStaffId(),
                    dateTime("2027-06-01 10:00:00"), dateTime("2027-06-01 11:00:00"));

            // Step 10: confirm ALLOCATED, correct references, and the Doctor-side table reflects the new status.
            assertEquals("ALLOCATED", labRequest.getStatus(), "allocating an asset must move the request to ALLOCATED");
            assertEquals(labRequest.getRequestId(), allocation.getMedicalRequestId(), "allocation must reference the correct request");
            assertEquals(labAsset.getAssetId(), allocation.getAssetId(), "allocation must reference the correct asset");
            assertRequestVisibleOnDoctorScreen(env, fixture.doctor, labRequest, "Full blood count", "ALLOCATED");

            // Step 11-12: Admin completes the allocation; both become COMPLETED.
            env.allocationManager.completeAllocation(allocation.getAllocationId());
            assertEquals(AssetAllocation.STATUS_COMPLETED, allocation.getAllocationStatus(), "allocation must become COMPLETED");
            assertEquals("COMPLETED", labRequest.getStatus(), "request must become COMPLETED");

            // Step 13-14: reload every relevant manager and confirm persistence.
            env.reloadEverything();
            MedicalRequest reloadedRequest = env.clinicalManager.findRequestById(labRequest.getRequestId());
            assertEquals("COMPLETED", reloadedRequest.getStatus(), "the reloaded request must still be COMPLETED");
            List<AssetAllocation> reloadedAllocations = env.allocationManager.getAllocationsForRequest(labRequest.getRequestId());
            assertEquals(1, reloadedAllocations.size(), "exactly one allocation must reload for the request");
            assertEquals(AssetAllocation.STATUS_COMPLETED, reloadedAllocations.get(0).getAllocationStatus(),
                    "the reloaded allocation must still be COMPLETED");
        } finally {
            fixture.close();
        }
    }

    /** A compact X_RAY pass through the same lifecycle - not a duplicate of the full Admin unit-test depth. */
    private void testDoctorXRayRequestThroughAdminApprovalAllocationAndCompletion() throws Exception {
        RequestScenarioFixture fixture = new RequestScenarioFixture();
        try {
            Environment env = fixture.env;
            MedicalRecord record = env.clinicalManager.getOrOpenRecord(
                    fixture.appointment.getAppointmentId(), fixture.doctor.getDoctorId());
            env.clinicalManager.updateVitalsAndNotes(record.getRecordId(), "BP 118/76", "Follow-up", "Stable");
            MedicalRequest xrayRequest = env.clinicalManager.addRequest(record.getRecordId(), "X_RAY", "Chest X-ray");

            env.clinicalManager.approveRequest(xrayRequest.getRequestId(), fixture.admin.getStaffId());
            HospitalAsset xrayAsset = env.assetManager.createAsset("X-Ray Room 1", "XRAY_ROOM", "First Floor", true,
                    fixture.admin.getStaffId());
            AssetAllocation allocation = env.allocationManager.createAllocation(xrayRequest.getRequestId(),
                    xrayAsset.getAssetId(), fixture.admin.getStaffId(),
                    dateTime("2027-06-01 10:00:00"), dateTime("2027-06-01 10:30:00"));
            assertEquals("ALLOCATED", xrayRequest.getStatus(), "the X_RAY request must become ALLOCATED");

            env.allocationManager.completeAllocation(allocation.getAllocationId());
            assertEquals("COMPLETED", xrayRequest.getStatus(), "the X_RAY request must become COMPLETED");

            env.reloadEverything();
            assertEquals("COMPLETED", env.clinicalManager.findRequestById(xrayRequest.getRequestId()).getStatus(),
                    "the reloaded X_RAY request must still be COMPLETED");
        } finally {
            fixture.close();
        }
    }

    /**
     * Builds the same reflective view of RequestLabImagingNew's table the existing
     * ClinicalManagerMedicalRequestTest uses, and confirms the given request's row shows the
     * expected details/status. Constructed via reflection: RequestLabImagingNew lives in the
     * (pre-existing, oddly-cased) "HMS.ui.doctor" package, which a case-insensitive filesystem
     * collapses into the same directory as "hms.ui.doctor" - javac's cross-compilation-unit
     * classpath lookup rejects that case mismatch even though a single combined compile does not.
     */
    private static void assertRequestVisibleOnDoctorScreen(Environment env, Doctor doctor, MedicalRequest request,
            String expectedDetails, String expectedStatus) throws Exception {
        // RequestLabImagingNew reads the active Doctor from the session, so the Doctor must be
        // logged in before the panel is constructed. None of this test's later steps depend on
        // session state, so the login is simply left in place rather than restored.
        if (env.sessionManager.getCurrentUser() == null) {
            env.sessionManager.login(doctor.getUsername(), "pass1234");
        }
        Class<?> panelClass = Class.forName("HMS.ui.doctor.RequestLabImagingNew");
        Constructor<?> constructor = panelClass.getConstructor(ClinicalManager.class, AppointmentManager.class,
                AssessmentTypeManager.class, UserManager.class, SessionManager.class, MainFrame.class, Runnable.class);
        Object panel = constructor.newInstance(env.clinicalManager, env.appointmentManager, env.typeManager,
                env.userManager, env.sessionManager, null, (Runnable) () -> { });

        JTable table = (JTable) getPrivateField(panel, "tblRequests");
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        boolean foundRow = false;
        for (int row = 0; row < model.getRowCount(); row++) {
            if (request.getRequestId().equals(model.getValueAt(row, 0))) {
                assertEquals(request.getRequestType(), model.getValueAt(row, 1), "Doctor screen must show the request type");
                assertEquals(expectedDetails, model.getValueAt(row, 2), "Doctor screen must show the request details");
                assertEquals(expectedStatus, model.getValueAt(row, 4), "Doctor screen must show the current status");
                foundRow = true;
            }
        }
        assertTrue(foundRow, "the request must appear on the active Doctor's own request screen");
    }

    private static void assertRequestVisibleOnAdminScreen(Environment env, MedicalRequest request, String expectedDetails)
            throws Exception {
        RecordingMainFrame mainFrame = new RecordingMainFrame();
        MedicalRequestManagementPanel adminPanel = new MedicalRequestManagementPanel(env.clinicalManager,
                env.userManager, env.appointmentManager, env.assetManager, env.rateManager, env.insuranceManager,
                env.allocationManager, env.sessionManager, mainFrame, () -> { });
        JTable table = (JTable) getPrivateField(adminPanel, "requestsTbl");
        boolean foundRow = false;
        for (int row = 0; row < table.getModel().getRowCount(); row++) {
            if (request.getRequestId().equals(table.getModel().getValueAt(row, 0))) {
                assertEquals(expectedDetails, table.getModel().getValueAt(row, 4), "Admin screen must show the same details");
                foundRow = true;
            }
        }
        assertTrue(foundRow, "the Doctor-created request must immediately appear on Admin's Medical Request screen");
    }

    // =====================================================================================
    // Scenario 3: Doctor clinical data -> Patient view
    // =====================================================================================

    private void testDoctorClinicalDataVisibleToPatientAfterFinalization() throws Exception {
        RequestScenarioFixture fixture = new RequestScenarioFixture();
        try {
            Environment env = fixture.env;

            // A current consultation rate is a standing operational precondition in production
            // (Main.java always wires ClinicalManager to BillingManager/ConsultationRateManager),
            // not something specific to this scenario - finalizeRecord() always bills, so one must
            // exist before any record can be finalized, exactly as it must in real use.
            env.rateManager.createRate("General Checkup", 50.0, date("2020-01-01"), fixture.admin.getStaffId());

            // Step 1: Medical Manager creates an assessment/check-up type.
            AssessmentType type = env.typeManager.createType("Blood Pressure Check", "VITALS", "DESCRIPTIVE", 15.0,
                    env.userManager.findDoctorById(fixture.doctor.getDoctorId()).getShiftManager().getManagerId());

            MedicalRecord record = env.clinicalManager.getOrOpenRecord(
                    fixture.appointment.getAppointmentId(), fixture.doctor.getDoctorId());

            // Step 2: Doctor records an assessment result.
            AssessmentResult result = env.clinicalManager.addAssessmentResult(
                    record.getRecordId(), type.getAssessmentTypeId(), "120/80", "NORMAL");

            // Step 3: Doctor issues a prescription.
            Prescription prescription = env.clinicalManager.addPrescription(
                    record.getRecordId(), "Paracetamol 500mg", "1 tablet daily", "Take after food");

            // Step 4: finalize the record (requires non-blank consultation notes, already set in the fixture's
            // earlier updateVitalsAndNotes call inside the RequestScenarioFixture would not have run here, so
            // set them now before finalizing).
            env.clinicalManager.updateVitalsAndNotes(record.getRecordId(), "BP 120/80", "Routine check", "Patient stable");
            env.clinicalManager.finalizeRecord(record.getRecordId());
            assertEquals("COMPLETED", env.appointmentManager.findById(fixture.appointment.getAppointmentId()).getStatus(),
                    "finalizing the record must mark the appointment COMPLETED");

            // Step 5: the Patient's production APIs must be able to reach all of this.
            List<MedicalRecord> patientHistory = env.clinicalManager.getPatientHistory(fixture.patient.getPatientId());
            assertEquals(1, patientHistory.size(), "the patient's history must include the completed record");
            MedicalRecord historyRecord = patientHistory.get(0);
            assertTrue(historyRecord.getAssessmentResults().contains(result), "the history record must include the assessment result");
            assertTrue(historyRecord.getPrescriptions().contains(prescription), "the history record must include the prescription");

            // Step 6: Patient submits feedback, now that the appointment is COMPLETED.
            Feedback feedback = env.clinicalManager.submitFeedback(
                    fixture.patient.getPatientId(), fixture.appointment.getAppointmentId(), 5, "Great consultation");

            // Step 7-8: reload and confirm everything the patient can see remains available.
            env.reloadEverything();
            List<MedicalRecord> reloadedHistory = env.clinicalManager.getPatientHistory(fixture.patient.getPatientId());
            assertEquals(1, reloadedHistory.size(), "the reloaded patient history must still include the record");
            MedicalRecord reloadedRecord = reloadedHistory.get(0);
            assertEquals(1, reloadedRecord.getAssessmentResults().size(), "the reloaded record must still have its assessment result");
            assertEquals(1, reloadedRecord.getPrescriptions().size(), "the reloaded record must still have its prescription");
            List<Feedback> reloadedFeedback = env.clinicalManager.getDoctorFeedback(fixture.doctor.getDoctorId());
            boolean foundFeedback = false;
            for (Feedback entry : reloadedFeedback) {
                if (entry.getFeedbackId().equals(feedback.getFeedbackId())) {
                    foundFeedback = true;
                }
            }
            assertTrue(foundFeedback, "the reloaded feedback must still be linked to the doctor");
        } finally {
            fixture.close();
        }
    }

    // =====================================================================================
    // Scenario 4: Admin configuration used by other roles
    // =====================================================================================

    private void testAdminConsultationRateConsumedByBillingOutsideAdminPanel() throws Exception {
        RequestScenarioFixture fixture = new RequestScenarioFixture();
        try {
            Environment env = fixture.env;

            // Step 1: Admin creates a current consultation rate.
            ConsultationRate rate = env.rateManager.createRate("General Checkup", 55.0, date("2020-01-01"),
                    fixture.admin.getStaffId());
            assertEquals(rate.getRateId(), env.rateManager.getCurrentActiveRate().getRateId(),
                    "the only active, past-dated rate must be the current rate");

            // Step 3-4: finalizing a record (a Doctor/production workflow) must consume that rate through
            // billing, without the Admin panel itself ever being touched.
            MedicalRecord record = env.clinicalManager.getOrOpenRecord(
                    fixture.appointment.getAppointmentId(), fixture.doctor.getDoctorId());
            env.clinicalManager.updateVitalsAndNotes(record.getRecordId(), "BP 118/76", "Routine", "All well");
            env.clinicalManager.finalizeRecord(record.getRecordId());

            List<Payment> patientBills = env.billingManager.getPatientBills(fixture.patient.getPatientId());
            assertEquals(1, patientBills.size(), "finalizing the record must create exactly one bill for the patient");
            assertEquals(55.0, patientBills.get(0).getAmount(), "the bill amount must equal the current consultation rate");

            // Step 6: reload and verify the rate and the billing record both persist.
            env.reloadEverything();
            assertEquals(55.0, env.rateManager.findById(rate.getRateId()).getBaseAmount(), "the reloaded rate amount must persist");
            List<Payment> reloadedBills = env.billingManager.getPatientBills(fixture.patient.getPatientId());
            assertEquals(1, reloadedBills.size(), "the reloaded billing record must still exist");
            assertEquals(55.0, reloadedBills.get(0).getAmount(), "the reloaded bill amount must be unchanged");
        } finally {
            fixture.close();
        }
    }

    /**
     * Documents a genuine, verified integration limitation rather than inventing new billing
     * design: InsuranceNetworkManager's coverage percentage is stored and reloadable, but
     * BillingManager has no reference to InsuranceNetworkManager at all and never reduces a bill
     * by a patient's coverage percentage. This test proves that behavior directly - a bill created
     * through the same finalizeRecord() workflow charges the FULL base rate even with an active,
     * high-coverage insurance network on file - so this is not accidentally left untested, and any
     * future implementer knows exactly what does not happen yet.
     */
    private void testInsuranceCoverageNotYetConsumedByBillingDocumentedLimitation() throws Exception {
        RequestScenarioFixture fixture = new RequestScenarioFixture();
        try {
            Environment env = fixture.env;
            env.rateManager.createRate("General Checkup", 60.0, date("2020-01-01"), fixture.admin.getStaffId());
            InsuranceNetwork network = env.insuranceManager.createNetwork(
                    "HealthCo", "Gold Plan", 90.0, fixture.admin.getStaffId());
            assertTrue(network.isActive(), "the new insurance network must start active");

            MedicalRecord record = env.clinicalManager.getOrOpenRecord(
                    fixture.appointment.getAppointmentId(), fixture.doctor.getDoctorId());
            env.clinicalManager.updateVitalsAndNotes(record.getRecordId(), "BP 118/76", "Routine", "All well");
            env.clinicalManager.finalizeRecord(record.getRecordId());

            List<Payment> bills = env.billingManager.getPatientBills(fixture.patient.getPatientId());
            assertEquals(1, bills.size(), "exactly one bill must be created");
            assertEquals(60.0, bills.get(0).getAmount(),
                    "documented limitation: the bill charges the full base rate - insurance coverage is not yet applied");
        } finally {
            fixture.close();
        }
    }

    // =====================================================================================
    // Scenario 5: shared EditProfileNew across all roles
    // =====================================================================================

    private void testAllFourRoleEntryPointsOpenSharedEditProfileNew() throws Exception {
        try (Environment env = new Environment()) {
            AdminStaff admin = registerAdmin(env, "admin1");
            MedicalManager manager = registerManager(env, "manager1");
            Doctor doctor = registerDoctor(env, "doctor1", "Cardiology");
            Patient patient = registerPatient(env, "patient1");

            // Admin.
            env.sessionManager.login("admin1", "pass1234");
            RecordingMainFrame adminFrame = new RecordingMainFrame();
            hms.ui.admin.AdminDashboardNew adminDashboard = new hms.ui.admin.AdminDashboardNew(env.sessionManager,
                    env.userManager, env.appointmentManager, env.clinicalManager, env.assetManager, env.rateManager,
                    env.insuranceManager, env.allocationManager, adminFrame, () -> { });
            ((JButton) getPrivateField(adminDashboard, "btnEditProfile")).doClick();
            assertTrue(adminFrame.lastPanel instanceof EditProfileNew, "AdminDashboardNew's Edit Profile must open EditProfileNew");

            // Doctor.
            env.sessionManager.logout();
            env.sessionManager.login("doctor1", "pass1234");
            RecordingMainFrame doctorFrame = new RecordingMainFrame();
            Class<?> doctorDashboardClass = Class.forName("HMS.ui.doctor.DoctorDashboardNew");
            Constructor<?> doctorCtor = doctorDashboardClass.getConstructor(SessionManager.class, UserManager.class,
                    AppointmentManager.class, ClinicalManager.class, AssessmentTypeManager.class, MainFrame.class,
                    Runnable.class);
            Object doctorDashboard = doctorCtor.newInstance(env.sessionManager, env.userManager, env.appointmentManager,
                    env.clinicalManager, env.typeManager, doctorFrame, (Runnable) () -> { });
            ((JButton) getPrivateField(doctorDashboard, "btnEditMyProfile")).doClick();
            assertTrue(doctorFrame.lastPanel instanceof EditProfileNew, "DoctorDashboardNew's Edit Profile must open EditProfileNew");

            // Medical Manager.
            env.sessionManager.logout();
            env.sessionManager.login("manager1", "pass1234");
            RecordingMainFrame managerFrame = new RecordingMainFrame();
            hms.ui.medicalmanager.MedicalManagerDashboardNew managerDashboard =
                    new hms.ui.medicalmanager.MedicalManagerDashboardNew(env.sessionManager, env.userManager,
                            env.appointmentManager, env.billingManager, env.reportManager, env.typeManager,
                            env.shiftManager, env.departmentManager, env.clinicalManager, managerFrame, () -> { });
            ((JButton) getPrivateField(managerDashboard, "btnProfile")).doClick();
            assertTrue(managerFrame.lastPanel instanceof EditProfileNew,
                    "MedicalManagerDashboardNew's Edit Profile must open EditProfileNew");

            // Patient (PatientController.openProfile wraps EditProfileNew in a plain layout panel).
            env.sessionManager.logout();
            env.sessionManager.login("patient1", "pass1234");
            RecordingMainFrame patientFrame = new RecordingMainFrame();
            hms.ui.patient.PatientController patientController = new hms.ui.patient.PatientController(
                    env.sessionManager, env.userManager, env.appointmentManager, env.shiftManager, env.clinicalManager,
                    env.billingManager, patientFrame, () -> { });
            patientController.openProfile();
            assertNotNull(patientFrame.lastPanel, "Patient's Edit Profile action must show a panel");
            boolean foundEditProfile = false;
            for (java.awt.Component component : patientFrame.lastPanel.getComponents()) {
                if (component instanceof EditProfileNew) {
                    foundEditProfile = true;
                }
            }
            assertTrue(foundEditProfile, "PatientController's Edit Profile must open the shared EditProfileNew");
        }
    }

    private void testUpdateOwnProfileAcrossTwoRolesPersistsAfterReload() throws Exception {
        try (Environment env = new Environment()) {
            Doctor doctor = registerDoctor(env, "doctor1", "Cardiology");
            Patient patient = registerPatient(env, "patient1");

            // Doctor: contact-only update.
            env.userManager.updateOwnProfile(doctor.getUserId(), "0199999999", "doctor-updated@test.com", "", "", "");
            assertEquals("0199999999", doctor.getPhone(), "the doctor's phone must be updated");

            // Patient: contact + password update.
            env.userManager.updateOwnProfile(patient.getUserId(), "0188888888", "patient-updated@test.com",
                    "pass1234", "newPatientPass1", "newPatientPass1");
            assertEquals("newPatientPass1", patient.getPassword(), "the patient's password must be updated");

            env.reloadEverything();
            Doctor reloadedDoctor = (Doctor) env.userManager.findById(doctor.getUserId());
            assertEquals("0199999999", reloadedDoctor.getPhone(), "the reloaded doctor's phone must persist");
            assertEquals("doctor-updated@test.com", reloadedDoctor.getEmail(), "the reloaded doctor's email must persist");

            Patient reloadedPatient = (Patient) env.userManager.findById(patient.getUserId());
            assertEquals("0188888888", reloadedPatient.getPhone(), "the reloaded patient's phone must persist");
            assertEquals("newPatientPass1", reloadedPatient.getPassword(), "the reloaded patient's new password must persist");
        }
    }

    private void testRejectedPasswordChangeLeavesDoctorProfileUnchanged() throws Exception {
        try (Environment env = new Environment()) {
            Doctor doctor = registerDoctor(env, "doctor1", "Cardiology");

            boolean rejected = false;
            try {
                env.userManager.updateOwnProfile(doctor.getUserId(), "0199999999", "changed@test.com",
                        "pass1234", "newPass1", "differentPass1");
            } catch (IllegalArgumentException ex) {
                rejected = true;
            }
            assertTrue(rejected, "a mismatched password confirmation must be rejected");
            assertEquals("0133333333", doctor.getPhone(), "phone must be unchanged after the rejected update");
            assertEquals("doctor1@test.com", doctor.getEmail(), "email must be unchanged after the rejected update");
            assertEquals("pass1234", doctor.getPassword(), "password must be unchanged after the rejected update");
        }
    }

    // =====================================================================================
    // Scenario 6: Admin forms reflect other-role actions
    // =====================================================================================

    private void testAdminPanelsReflectOtherRoleActions() throws Exception {
        try (Environment env = new Environment()) {
            AdminStaff admin = registerAdmin(env, "admin1");
            MedicalManager manager = registerManager(env, "manager1");
            Doctor doctor = registerDoctor(env, "doctor1", "Cardiology");
            Patient patient = registerPatient(env, "patient1");

            env.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());
            Date shiftDate = date("2027-06-01");
            env.shiftManager.createShift(manager.getManagerId(), doctor.getDoctorId(), shiftDate,
                    LocalTime.parse("09:00"), LocalTime.parse("17:00"));
            Appointment appointment = env.appointmentManager.book(patient.getPatientId(), doctor.getDoctorId(),
                    shiftDate, LocalTime.parse("09:00"));
            MedicalRecord record = env.clinicalManager.getOrOpenRecord(appointment.getAppointmentId(), doctor.getDoctorId());
            MedicalRequest request = env.clinicalManager.addRequest(record.getRecordId(), "LAB", "Routine blood work");
            env.clinicalManager.approveRequest(request.getRequestId(), admin.getStaffId());
            HospitalAsset asset = env.assetManager.createAsset("Lab Room 1", "LAB", "Ground Floor", true, admin.getStaffId());
            env.allocationManager.createAllocation(request.getRequestId(), asset.getAssetId(), admin.getStaffId(),
                    dateTime("2027-06-01 10:00:00"), dateTime("2027-06-01 11:00:00"));

            RecordingMainFrame mainFrame = new RecordingMainFrame();

            // DoctorManagementPanel shows the Doctor's Medical Manager.
            DoctorManagementPanel doctorPanel = new DoctorManagementPanel(env.userManager, env.appointmentManager,
                    env.clinicalManager, env.assetManager, env.rateManager, env.insuranceManager, env.allocationManager,
                    env.sessionManager, mainFrame, () -> { });
            JTable doctorTable = (JTable) getPrivateField(doctorPanel, "doctorTable");
            assertTableShowsManagerColumn(doctorTable, doctor.getDoctorId(), manager.getUsername());

            // AppointmentManagementPanel shows the Patient-created appointment.
            AppointmentManagementPanel appointmentPanel = new AppointmentManagementPanel(env.appointmentManager,
                    env.userManager, env.clinicalManager, env.assetManager, env.rateManager, env.insuranceManager,
                    env.allocationManager, env.sessionManager, mainFrame, () -> { });
            JTable appointmentTable = (JTable) getPrivateField(appointmentPanel, "appointmentTable");
            assertRowExistsInColumnZero(appointmentTable, appointment.getAppointmentId());

            // MedicalRequestManagementPanel shows the Doctor-created request, details and status.
            MedicalRequestManagementPanel requestPanel = new MedicalRequestManagementPanel(env.clinicalManager,
                    env.userManager, env.appointmentManager, env.assetManager, env.rateManager, env.insuranceManager,
                    env.allocationManager, env.sessionManager, mainFrame, () -> { });
            JTable requestTable = (JTable) getPrivateField(requestPanel, "requestsTbl");
            boolean requestFound = false;
            for (int row = 0; row < requestTable.getModel().getRowCount(); row++) {
                if (request.getRequestId().equals(requestTable.getModel().getValueAt(row, 0))) {
                    assertEquals("Routine blood work", requestTable.getModel().getValueAt(row, 4), "details must match");
                    assertEquals("ALLOCATED", requestTable.getModel().getValueAt(row, 6), "status must match");
                    requestFound = true;
                }
            }
            assertTrue(requestFound, "MedicalRequestManagementPanel must show the Doctor-created request");

            // HospitalAssetManagementPanel shows the allocated asset and its availability.
            HospitalAssetManagementPanel assetPanel = new HospitalAssetManagementPanel(env.assetManager,
                    env.userManager, env.appointmentManager, env.clinicalManager, env.rateManager, env.insuranceManager,
                    env.allocationManager, env.sessionManager, mainFrame, () -> { });
            JTable assetTable = (JTable) getPrivateField(assetPanel, "assetTable");
            assertRowExistsInColumnZero(assetTable, asset.getAssetId());

            // UserManagementPanel shows all four roles and their current active states.
            UserManagementPanel userPanel = new UserManagementPanel(env.userManager, env.appointmentManager,
                    env.clinicalManager, env.assetManager, env.rateManager, env.insuranceManager, env.allocationManager,
                    env.sessionManager, mainFrame, () -> { });
            JTable userTable = (JTable) getPrivateField(userPanel, "userTable");
            assertEquals(4, userTable.getModel().getRowCount(), "UserManagementPanel must list all four registered roles");

            // Tables are non-editable, and the ID comes from column 0 rather than row position.
            assertTrue(!doctorTable.getModel().isCellEditable(0, 0), "doctor table must not be directly editable");
            assertTrue(!appointmentTable.getModel().isCellEditable(0, 0), "appointment table must not be directly editable");
            assertTrue(!requestTable.getModel().isCellEditable(0, 0), "request table must not be directly editable");
            assertTrue(!assetTable.getModel().isCellEditable(0, 0), "asset table must not be directly editable");
            assertTrue(!userTable.getModel().isCellEditable(0, 0), "user table must not be directly editable");
        }
    }

    private static void assertTableShowsManagerColumn(JTable doctorTable, String doctorId, String managerUsername) {
        for (int row = 0; row < doctorTable.getModel().getRowCount(); row++) {
            if (doctorId.equals(doctorTable.getModel().getValueAt(row, 0))) {
                String managerCell = String.valueOf(doctorTable.getModel().getValueAt(row, 3));
                assertTrue(managerCell.contains(managerUsername),
                        "DoctorManagementPanel must show the assigned Medical Manager's username: " + managerCell);
                return;
            }
        }
        throw new AssertionError("doctor row not found by ID in column 0");
    }

    private static void assertRowExistsInColumnZero(JTable table, String expectedId) {
        for (int row = 0; row < table.getModel().getRowCount(); row++) {
            if (expectedId.equals(table.getModel().getValueAt(row, 0))) {
                return;
            }
        }
        throw new AssertionError("expected ID " + expectedId + " not found in column 0 of the table");
    }

    // =====================================================================================
    // Scenario 7: merged main demonstration data
    // =====================================================================================

    private void testMergedDemonstrationDataLoadsCompletelyThroughManagerGraph() throws Exception {
        Path realDataDir = new File("data").toPath();
        if (!Files.exists(realDataDir)) {
            return;
        }
        Path tempDir = Files.createTempDirectory("hms-crossrole-realdata-");
        try {
            copyEntireDataDirectory(realDataDir, tempDir);

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
            File allocationsFile = new File(tempDir.toFile(), "asset_allocations.txt");
            File ratesFile = new File(tempDir.toFile(), "consultation_rates.txt");
            File insuranceFile = new File(tempDir.toFile(), "insurance_networks.txt");
            File departmentsFile = new File(tempDir.toFile(), "clinical_departments.txt");
            File paymentsFile = new File(tempDir.toFile(), "payments.txt");

            PrintStream originalErr = System.err;
            ByteArrayOutputStream capturedErr = new ByteArrayOutputStream();
            UserManager userManager;
            DoctorShiftManager shiftManager;
            AppointmentManager appointmentManager;
            AssessmentTypeManager typeManager;
            ClinicalManager clinicalManager;
            HospitalAssetManager assetManager;
            ConsultationRateManager rateManager;
            InsuranceNetworkManager insuranceManager;
            ClinicalDepartmentManager departmentManager;
            AssetAllocationManager allocationManager;
            BillingManager billingManager;
            try {
                System.setErr(new PrintStream(capturedErr, true, "UTF-8"));
                userManager = new UserManager(usersFile);
                shiftManager = new DoctorShiftManager(userManager, shiftsFile);
                appointmentManager = new AppointmentManager(userManager, shiftManager, appointmentsFile);
                typeManager = new AssessmentTypeManager(userManager, typesFile);
                clinicalManager = new ClinicalManager(userManager, appointmentManager, typeManager,
                        recordsFile, prescriptionsFile, requestsFile, resultsFile, feedbackFile);
                billingManager = new BillingManager(appointmentManager, paymentsFile);
                assetManager = new HospitalAssetManager(userManager, assetsFile);
                rateManager = new ConsultationRateManager(userManager, ratesFile);
                clinicalManager.configureBilling(billingManager, rateManager);
                insuranceManager = new InsuranceNetworkManager(userManager, insuranceFile);
                departmentManager = new ClinicalDepartmentManager(userManager, departmentsFile);
                allocationManager = new AssetAllocationManager(clinicalManager, assetManager, userManager, allocationsFile);
            } finally {
                System.setErr(originalErr);
            }

            // The committed demonstration data has now been corrected (duplicate assessment-type
            // IDs renumbered, CD009's embedded newline fixed) - loading it must produce zero
            // warnings at all. No warning is allowed here any more; any output at all means a
            // valid row is being skipped and must fail this test.
            String[] capturedLines = capturedErr.toString("UTF-8").split("\\r?\\n");
            for (String line : capturedLines) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                throw new AssertionError(
                        "loading the corrected merged demonstration data must produce no warnings at all, but got: " + line);
            }

            // ---- users: all four roles ----
            assertEquals(15, userManager.getUsers().size(), "all fifteen merged demonstration users must load");
            int adminCount = 0;
            int managerCount = 0;
            int doctorCount = 0;
            int patientCount = 0;
            for (hms.model.User user : userManager.getUsers()) {
                if (user instanceof AdminStaff) {
                    adminCount++;
                } else if (user instanceof MedicalManager) {
                    managerCount++;
                } else if (user instanceof Doctor) {
                    doctorCount++;
                } else if (user instanceof Patient) {
                    patientCount++;
                }
            }
            assertEquals(4, adminCount, "all four Admin Staff rows must load");
            assertEquals(2, managerCount, "both Medical Manager rows must load");
            assertEquals(4, doctorCount, "all four Doctor rows must load");
            assertEquals(5, patientCount, "all five Patient rows must load");

            // ---- Doctor -> Medical Manager links ----
            Doctor dr001 = userManager.findDoctorById("DR001");
            assertNotNull(dr001, "DR001 must load");
            assertNotNull(dr001.getShiftManager(), "DR001 must have a resolved manager link");
            assertEquals("MM001", dr001.getShiftManager().getManagerId(), "DR001 must link to MM001");
            Doctor dr002 = userManager.findDoctorById("DR002");
            assertEquals("MM002", dr002.getShiftManager().getManagerId(), "DR002 must link to MM002");

            // ---- hospital assets: all eleven, with the three legacy-alias rows resolved canonically ----
            assertEquals(11, assetManager.getAssets().size(), "all eleven merged hospital assets must load");
            assertEquals("WARD", assetManager.findById("HA004").getAssetType(), "HA004 must resolve to WARD");
            assertEquals("LAB", assetManager.findById("HA005").getAssetType(), "HA005 must resolve to LAB");
            assertEquals("WARD", assetManager.findById("HA008").getAssetType(), "HA008 must resolve to WARD");

            // ---- consultation rates / insurance networks ----
            assertEquals(9, rateManager.getRates().size(), "all nine merged consultation rates must load");
            assertEquals(10, insuranceManager.getNetworks().size(), "all ten merged insurance networks must load");

            // ---- shifts / appointments ----
            assertEquals(18, shiftManager.getShiftsForDoctor("DR001").size()
                    + shiftManager.getShiftsForDoctor("DR002").size()
                    + shiftManager.getShiftsForDoctor("DR003").size()
                    + shiftManager.getShiftsForDoctor("DR004").size(),
                    "all eighteen merged doctor shifts must load");
            assertEquals(14, appointmentManager.getAppointments().size(), "all fourteen merged appointments must load");

            // ---- medical records / prescriptions / assessment results ----
            assertEquals(8, clinicalManager.getRecords().size(), "all eight merged medical records must load");
            int prescriptionCount = 0;
            int resultCount = 0;
            for (MedicalRecord record : clinicalManager.getRecords()) {
                prescriptionCount += record.getPrescriptions().size();
                resultCount += record.getAssessmentResults().size();
            }
            assertEquals(8, prescriptionCount, "all eight merged prescriptions must load");
            assertEquals(12, resultCount, "all twelve merged assessment results must load");

            // ---- assessment types: all fifteen load, with no duplicate ID ----
            assertEquals(15, typeManager.getTypes().size(), "all fifteen corrected merged assessment types must load");
            assertNotNull(typeManager.findById("AT001"), "AT001 (original) must still load");
            assertNotNull(typeManager.findById("AT008"), "AT008 (original) must still load");
            assertNotNull(typeManager.findById("AT009"), "AT009 (Fitness check) must still load");
            assertEquals("Complete Blood Count (CBC)", typeManager.findById("AT010").getName(), "AT010 must be the renumbered CBC type");
            assertEquals("Chest X-Ray Examination", typeManager.findById("AT011").getName(), "AT011 must be the renumbered X-Ray type");
            assertEquals("Electrocardiogram (ECG)", typeManager.findById("AT012").getName(), "AT012 must be the renumbered ECG type");
            assertEquals("Magnetic Resonance Imaging (MRI)", typeManager.findById("AT013").getName(), "AT013 must be the renumbered MRI type");
            assertEquals("Fasting Blood Glucose Test", typeManager.findById("AT014").getName(), "AT014 must be the fasting glucose type");
            assertEquals("Vision Screening", typeManager.findById("AT015").getName(), "AT015 must be the added vision screening type");

            // ---- medical requests, including the legacy 6-field rows ----
            assertEquals(10, clinicalManager.getAllRequests().size(),
                    "all ten merged medical requests (including legacy 6-field rows) must load");

            // ---- feedback / payments ----
            assertEquals(8, clinicalManager.getDoctorFeedback("DR001").size() + clinicalManager.getDoctorFeedback("DR002").size(),
                    "all eight merged feedback entries must load");
            assertEquals(9, billingManager.getPayments().size(), "all nine merged payments must load");

            // ---- clinical departments: all eleven load, including the corrected CD009 ----
            assertEquals(11, departmentManager.getDepartments().size(), "all eleven merged clinical departments must load");
            assertEquals("Fitness Lab", departmentManager.findById("CD009").getDepartmentName(),
                    "CD009 must be the corrected single-line Fitness Lab department");
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private void testMergedDemonstrationDataSupportsSafeMutationsAndReload() throws Exception {
        Path realDataDir = new File("data").toPath();
        if (!Files.exists(realDataDir)) {
            return;
        }
        Path tempDir = Files.createTempDirectory("hms-crossrole-realdata-mutate-");
        try {
            copyEntireDataDirectory(realDataDir, tempDir);

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
            File allocationsFile = new File(tempDir.toFile(), "asset_allocations.txt");
            File ratesFile = new File(tempDir.toFile(), "consultation_rates.txt");
            File insuranceFile = new File(tempDir.toFile(), "insurance_networks.txt");
            File departmentsFile = new File(tempDir.toFile(), "clinical_departments.txt");

            UserManager userManager = new UserManager(usersFile);
            DoctorShiftManager shiftManager = new DoctorShiftManager(userManager, shiftsFile);
            AppointmentManager appointmentManager = new AppointmentManager(userManager, shiftManager, appointmentsFile);
            AssessmentTypeManager typeManager = new AssessmentTypeManager(userManager, typesFile);
            ClinicalManager clinicalManager = new ClinicalManager(userManager, appointmentManager, typeManager,
                    recordsFile, prescriptionsFile, requestsFile, resultsFile, feedbackFile);
            HospitalAssetManager assetManager = new HospitalAssetManager(userManager, assetsFile);
            ConsultationRateManager rateManager = new ConsultationRateManager(userManager, ratesFile);
            InsuranceNetworkManager insuranceManager = new InsuranceNetworkManager(userManager, insuranceFile);
            ClinicalDepartmentManager departmentManager = new ClinicalDepartmentManager(userManager, departmentsFile);
            AssetAllocationManager allocationManager = new AssetAllocationManager(clinicalManager, assetManager,
                    userManager, allocationsFile);

            // Confirm the corrected counts before any mutation.
            assertEquals(15, typeManager.getTypes().size(), "all fifteen assessment types must load before mutation");
            assertEquals(11, departmentManager.getDepartments().size(), "all eleven clinical departments must load before mutation");

            // Safe mutation 1: update an existing profile.
            userManager.updateProfile("PT001", "0199990000", "patient-updated@test.com");

            // Safe mutation 2: create one new appointment (a future date, within a new shift).
            Date newShiftDate = date("2027-06-01");
            shiftManager.createShift("MM001", "DR001", newShiftDate, LocalTime.parse("09:00"), LocalTime.parse("17:00"));
            Appointment newAppointment = appointmentManager.book("PT001", "DR001", newShiftDate, LocalTime.parse("09:00"));

            // Safe mutation 3: create and process one new request.
            MedicalRecord newRecord = clinicalManager.getOrOpenRecord(newAppointment.getAppointmentId(), "DR001");
            MedicalRequest newRequest = clinicalManager.addRequest(newRecord.getRecordId(), "LAB", "New cross-role test request");
            clinicalManager.approveRequest(newRequest.getRequestId(), "AS001");

            // Safe mutation 4: an asset/rate/network change - toggle an existing asset's availability.
            assetManager.setAvailability("HA001", false);

            // Reload the complete graph and verify original records remain, new records persist,
            // legacy rows are preserved, and asset aliases are rewritten canonically only now that a
            // legitimate save has actually happened on hospital_assets.txt.
            UserManager reloadedUsers = new UserManager(usersFile);
            DoctorShiftManager reloadedShifts = new DoctorShiftManager(reloadedUsers, shiftsFile);
            AppointmentManager reloadedAppointments = new AppointmentManager(reloadedUsers, reloadedShifts, appointmentsFile);
            AssessmentTypeManager reloadedTypes = new AssessmentTypeManager(reloadedUsers, typesFile);
            ClinicalManager reloadedClinical = new ClinicalManager(reloadedUsers, reloadedAppointments, reloadedTypes,
                    recordsFile, prescriptionsFile, requestsFile, resultsFile, feedbackFile);
            HospitalAssetManager reloadedAssets = new HospitalAssetManager(reloadedUsers, assetsFile);
            ClinicalDepartmentManager reloadedDepartments = new ClinicalDepartmentManager(reloadedUsers, departmentsFile);

            // Neither assessment types nor clinical departments were touched by any of these
            // mutations - confirm all fifteen types and all eleven departments are still exactly
            // as they were, proving no unrelated row was lost by reloading the rest of the graph.
            assertEquals(15, reloadedTypes.getTypes().size(), "all fifteen assessment types must remain after the mutation");
            assertEquals("Complete Blood Count (CBC)", reloadedTypes.findById("AT010").getName(), "AT010 must remain unchanged");
            assertEquals(11, reloadedDepartments.getDepartments().size(), "all eleven clinical departments must remain after the mutation");
            assertEquals("Fitness Lab", reloadedDepartments.findById("CD009").getDepartmentName(), "CD009 must remain unchanged");

            assertEquals(15, reloadedUsers.getUsers().size(), "the original fifteen users must remain after the mutation");
            assertEquals("0199990000", reloadedUsers.findById("PT001").getPhone(), "the profile update must persist");
            assertEquals(15, reloadedAppointments.getAppointments().size(),
                    "the original fourteen appointments plus the one new appointment must remain");
            assertEquals(11, reloadedClinical.getAllRequests().size(),
                    "the original ten requests plus the one new request must remain, including legacy 6-field rows");
            assertEquals("APPROVED", reloadedClinical.findRequestById(newRequest.getRequestId()).getStatus(),
                    "the new request's processed status must persist");
            assertEquals(11, reloadedAssets.getAssets().size(), "no asset must be lost by the availability toggle");
            assertTrue(!reloadedAssets.findById("HA001").isAvailability(), "the availability toggle must persist");

            // The legacy-aliased assets were not touched by this mutation (only HA001 was saved), so
            // their canonical resolution is still proven purely by the load-time alias mapping, not by
            // a rewrite - confirm they still resolve correctly after this unrelated save.
            assertEquals("WARD", reloadedAssets.findById("HA004").getAssetType(), "HA004 must still resolve to WARD");
            assertEquals("LAB", reloadedAssets.findById("HA005").getAssetType(), "HA005 must still resolve to LAB");

            // Every line in the resaved files must still be valid pipe-delimited structure.
            assertEveryNonCommentLineHasPipes(assetsFile);
            assertEveryNonCommentLineHasPipes(appointmentsFile);
            assertEveryNonCommentLineHasPipes(requestsFile);
        } finally {
            deleteRecursively(tempDir);
        }
    }

    /**
     * The committed demonstration data no longer has any duplicate assessment-type ID (that was
     * corrected in this phase), so AssessmentTypeManager's duplicate-ID protection is still real
     * production behavior that must remain tested - just against a synthetic temporary file
     * instead of relying on committed data staying malformed. Proves a duplicate ID is skipped and
     * logged, not silently accepted and not a crash, and that the first occurrence wins.
     */
    private void testSyntheticDuplicateAssessmentTypeIdIsSkippedNotCrashed() throws Exception {
        Path tempDir = Files.createTempDirectory("hms-crossrole-duplicate-assessment-type-");
        try {
            File usersFile = new File(tempDir.toFile(), "users.txt");
            File typesFile = new File(tempDir.toFile(), "assessment_types.txt");

            List<String> userLines = new ArrayList<>();
            userLines.add("#userId|role|username|password|phone|email|extra1|extra2|active|doctorManagerId");
            userLines.add("MM001|MEDICAL_MANAGER|manager1|pass1234|0122222222|manager1@test.com|MM001||true|");
            Files.write(usersFile.toPath(), userLines, StandardCharsets.UTF_8);

            List<String> typeLines = new ArrayList<>();
            typeLines.add("#assessmentTypeId|name|category|gradingRule|baseCharge|active|createdByManagerId");
            typeLines.add("AT001|First Definition|VITALS|DESCRIPTIVE|15.0|true|MM001");
            typeLines.add("AT001|Second Definition Reusing The Same ID|LAB|NUMERIC_RANGE|45.0|true|MM001");
            typeLines.add("AT002|Unrelated Valid Type|VITALS|DESCRIPTIVE|10.0|true|MM001");
            Files.write(typesFile.toPath(), typeLines, StandardCharsets.UTF_8);

            UserManager userManager = new UserManager(usersFile);

            PrintStream originalErr = System.err;
            ByteArrayOutputStream capturedErr = new ByteArrayOutputStream();
            AssessmentTypeManager typeManager;
            try {
                System.setErr(new PrintStream(capturedErr, true, "UTF-8"));
                typeManager = new AssessmentTypeManager(userManager, typesFile);
            } finally {
                System.setErr(originalErr);
            }

            assertEquals(2, typeManager.getTypes().size(),
                    "only the two rows with distinct IDs must load - the duplicate must be skipped, not crash");
            assertEquals("First Definition", typeManager.findById("AT001").getName(),
                    "the first occurrence of a duplicated ID must be the one that survives");
            assertNotNull(typeManager.findById("AT002"), "the unrelated valid row must still load");

            String warnings = capturedErr.toString("UTF-8");
            assertTrue(warnings.contains("Skipping duplicate assessment type ID:"),
                    "a duplicate assessment type ID must be logged as skipped: " + warnings);
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private static void copyEntireDataDirectory(Path sourceDir, Path targetDir) throws IOException {
        File[] files = sourceDir.toFile().listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile()) {
                Files.copy(file.toPath(), targetDir.resolve(file.getName()));
            }
        }
    }

    private static void assertEveryNonCommentLineHasPipes(File file) throws IOException {
        List<String> lines = readFileLines(file);
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            assertTrue(trimmed.contains("|"), "every data row must still be pipe-delimited: " + line);
        }
    }

    // =====================================================================================
    // File-safety checks
    // =====================================================================================

    private void testRejectedBookingLeavesAppointmentFileUnchanged() throws Exception {
        try (Environment env = new Environment()) {
            MedicalManager manager = registerManager(env, "manager1");
            Doctor doctor = registerDoctor(env, "doctor1", "Cardiology");
            Patient patient = registerPatient(env, "patient1");
            env.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());
            Date shiftDate = date("2027-06-01");
            env.shiftManager.createShift(manager.getManagerId(), doctor.getDoctorId(), shiftDate,
                    LocalTime.parse("09:00"), LocalTime.parse("17:00"));

            List<String> beforeContent = readFileLines(env.appointmentsFile);

            // Outside the shift window - must be rejected.
            boolean rejected = false;
            try {
                env.appointmentManager.book(patient.getPatientId(), doctor.getDoctorId(), shiftDate, LocalTime.parse("20:00"));
            } catch (IllegalArgumentException ex) {
                rejected = true;
            }
            assertTrue(rejected, "booking outside the doctor's shift must be rejected");

            List<String> afterContent = readFileLines(env.appointmentsFile);
            assertEquals(beforeContent, afterContent, "appointments.txt must be byte-for-byte unchanged after the rejected booking");
            assertEquals(0, env.appointmentManager.getAppointments().size(), "no appointment must exist after the rejected booking");
        }
    }

    private void testRejectedAllocationLeavesRequestAndAllocationFilesUnchanged() throws Exception {
        RequestScenarioFixture fixture = new RequestScenarioFixture();
        try {
            Environment env = fixture.env;
            MedicalRecord record = env.clinicalManager.getOrOpenRecord(
                    fixture.appointment.getAppointmentId(), fixture.doctor.getDoctorId());
            MedicalRequest request = env.clinicalManager.addRequest(record.getRecordId(), "LAB", "Test request");
            env.clinicalManager.approveRequest(request.getRequestId(), fixture.admin.getStaffId());
            HospitalAsset xrayAsset = env.assetManager.createAsset("X-Ray Room 1", "XRAY_ROOM", "First Floor", true,
                    fixture.admin.getStaffId());

            List<String> requestsBefore = readFileLines(env.requestsFile);
            List<String> allocationsBefore = readFileLines(env.allocationsFile);

            boolean rejected = false;
            try {
                env.allocationManager.createAllocation(request.getRequestId(), xrayAsset.getAssetId(),
                        fixture.admin.getStaffId(), dateTime("2027-06-01 10:00:00"), dateTime("2027-06-01 11:00:00"));
            } catch (IllegalArgumentException ex) {
                rejected = true;
            }
            assertTrue(rejected, "a LAB request must not accept an XRAY_ROOM asset");

            List<String> requestsAfter = readFileLines(env.requestsFile);
            List<String> allocationsAfter = readFileLines(env.allocationsFile);
            assertEquals(requestsBefore, requestsAfter, "medical_requests.txt must be unchanged after the rejected allocation");
            assertEquals(allocationsBefore, allocationsAfter, "asset_allocations.txt must be unchanged after the rejected allocation");
            assertEquals("APPROVED", request.getStatus(), "the request must remain APPROVED after the rejected allocation");
        } finally {
            fixture.close();
        }
    }
}
