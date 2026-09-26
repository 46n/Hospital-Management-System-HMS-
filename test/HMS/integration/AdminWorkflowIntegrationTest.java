package hms.integration;

import hms.manager.AppointmentManager;
import hms.manager.AssessmentTypeManager;
import hms.manager.AssetAllocationManager;
import hms.manager.ClinicalManager;
import hms.manager.ConsultationRateManager;
import hms.manager.DoctorShiftManager;
import hms.manager.HospitalAssetManager;
import hms.manager.InsuranceNetworkManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.AdminStaff;
import hms.model.Appointment;
import hms.model.AssetAllocation;
import hms.model.ConsultationRate;
import hms.model.Doctor;
import hms.model.HospitalAsset;
import hms.model.InsuranceNetwork;
import hms.model.MedicalManager;
import hms.model.MedicalRecord;
import hms.model.MedicalRequest;
import hms.model.Patient;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Step 11: a realistic end-to-end Admin workflow, proving the features completed in Steps 1-10
 * work together as one application, not just in isolation. Same hand-rolled runner style as the
 * other Step test files, since the project has no JUnit on its classpath. Every test builds its
 * managers in the same dependency order Main.java uses, works only against its own temporary
 * directory (never the real data/ directory), and deletes that directory afterward.
 *
 * Run with (from the project root, after compiling src to build/classes):
 *   javac -d build/test-classes -cp build/classes test/HMS/integration/AdminWorkflowIntegrationTest.java
 *   java -cp build/classes;build/test-classes hms.integration.AdminWorkflowIntegrationTest
 */
public class AdminWorkflowIntegrationTest {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static {
        DATE_FORMAT.setLenient(false);
        DATETIME_FORMAT.setLenient(false);
    }

    private int passed = 0;
    private int failed = 0;

    public static void main(String[] args) throws Exception {
        AdminWorkflowIntegrationTest suite = new AdminWorkflowIntegrationTest();

        // ---- the complete successful scenario ----
        suite.run("testCompleteAdminWorkflowEndToEnd", suite::testCompleteAdminWorkflowEndToEnd);

        // ---- representative rejection checks ----
        suite.run("testInactiveDoctorCannotReceiveNewAssignment", suite::testInactiveDoctorCannotReceiveNewAssignment);
        suite.run("testInactiveManagerCannotReceiveNewAssignment", suite::testInactiveManagerCannotReceiveNewAssignment);
        suite.run("testIncompatibleAssetCannotBeAllocated", suite::testIncompatibleAssetCannotBeAllocated);
        suite.run("testOverlappingAllocationOnSameAssetIsRejected", suite::testOverlappingAllocationOnSameAssetIsRejected);
        suite.run("testPipeInAssetNameIsRejectedWithoutCorruptingFile",
                suite::testPipeInAssetNameIsRejectedWithoutCorruptingFile);
        suite.run("testAssetWithAllocationHistoryCannotBeDeleted", suite::testAssetWithAllocationHistoryCannotBeDeleted);

        // ---- read-only compatibility check against the real data files ----
        suite.run("testRealDataFilesLoadThroughTemporaryCopiesWithoutError",
                suite::testRealDataFilesLoadThroughTemporaryCopiesWithoutError);

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

    // ---- the full set of managers for one scenario, wired in Main.java's dependency order ----

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

        UserManager userManager;
        DoctorShiftManager shiftManager;
        AppointmentManager appointmentManager;
        AssessmentTypeManager typeManager;
        ClinicalManager clinicalManager;
        HospitalAssetManager assetManager;
        ConsultationRateManager rateManager;
        InsuranceNetworkManager insuranceManager;
        AssetAllocationManager allocationManager;
        SessionManager sessionManager;

        Environment() throws IOException {
            tempDir = Files.createTempDirectory("hms-e2e-test-");
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
            buildManagers();
        }

        /** Same dependency order Main.java uses: each later manager resolves IDs through the earlier ones. */
        private void buildManagers() {
            userManager = new UserManager(usersFile);
            shiftManager = new DoctorShiftManager(userManager, shiftsFile);
            appointmentManager = new AppointmentManager(userManager, shiftManager, appointmentsFile);
            typeManager = new AssessmentTypeManager(userManager, typesFile);
            clinicalManager = new ClinicalManager(userManager, appointmentManager, typeManager,
                    recordsFile, prescriptionsFile, requestsFile, resultsFile, feedbackFile);
            assetManager = new HospitalAssetManager(userManager, assetsFile);
            rateManager = new ConsultationRateManager(userManager, ratesFile);
            insuranceManager = new InsuranceNetworkManager(userManager, insuranceFile);
            allocationManager = new AssetAllocationManager(clinicalManager, assetManager, userManager, allocationsFile);
            sessionManager = new SessionManager(userManager);
        }

        /** Rebuilds every manager from the same temporary files, discarding all in-memory state. */
        void reloadEverythingFromDisk() {
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

    /** Reads a temporary data file's lines as plain text, for before/after content comparisons. */
    private static List<String> readFileLines(File file) throws IOException {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * Reads a temporary data file's real lines (skipping the header comment) and confirms every
     * one has exactly the expected number of pipe-separated fields for that file's format - a
     * direct check that no row was left malformed or half-written.
     */
    private static void assertEveryLineHasFieldCount(File file, int expectedFieldCount, String label) throws IOException {
        if (!file.exists()) {
            return;
        }
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        for (String line : lines) {
            if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                continue;
            }
            String[] fields = line.split("\\|", -1);
            assertEquals(expectedFieldCount, fields.length,
                    "every " + label + " line must have exactly " + expectedFieldCount + " pipe-separated fields");
        }
    }

    // =====================================================================================
    // A-E: the complete successful Admin scenario
    // =====================================================================================

    private void testCompleteAdminWorkflowEndToEnd() throws Exception {
        try (Environment env = new Environment()) {

            // ---- Stage A: register the four roles and verify their generated identity ----
            AdminStaff admin = (AdminStaff) env.userManager.registerUser(
                    new AdminStaff(null, "admin1", "pass1234", "0111111111", "admin1@test.com", "ADMIN_STAFF", null));
            MedicalManager manager = (MedicalManager) env.userManager.registerUser(
                    new MedicalManager(null, "manager1", "pass1234", "0122222222", "manager1@test.com",
                            "MEDICAL_MANAGER", null));
            Doctor doctor = (Doctor) env.userManager.registerUser(
                    new Doctor(null, "doctor1", "pass1234", "0133333333", "doctor1@test.com", "DOCTOR", null,
                            "Cardiology"));
            Patient patient = (Patient) env.userManager.registerUser(
                    new Patient(null, "patient1", "pass1234", "0144444444", "patient1@test.com", "PATIENT", null,
                            date("1990-01-01")));

            assertEquals("ADMIN_STAFF", admin.getUserType(), "the admin's role must be ADMIN_STAFF");
            assertEquals("MEDICAL_MANAGER", manager.getUserType(), "the manager's role must be MEDICAL_MANAGER");
            assertEquals("DOCTOR", doctor.getUserType(), "the doctor's role must be DOCTOR");
            assertEquals("PATIENT", patient.getUserType(), "the patient's role must be PATIENT");
            assertEquals(admin.getUserId(), admin.getStaffId(), "the admin's staff ID must match its generated user ID");
            assertEquals(manager.getUserId(), manager.getManagerId(), "the manager's manager ID must match its generated user ID");
            assertEquals(doctor.getUserId(), doctor.getDoctorId(), "the doctor's doctor ID must match its generated user ID");
            assertEquals(patient.getUserId(), patient.getPatientId(), "the patient's patient ID must match its generated user ID");
            assertTrue(admin.isActive() && manager.isActive() && doctor.isActive() && patient.isActive(),
                    "every newly registered user must start active");

            // An Admin-managed update must persist on the live object.
            env.userManager.updateManagedUser(doctor.getUserId(), "doctor1", "0199999999", "doctor1-updated@test.com",
                    "Cardiology", null);
            assertEquals("0199999999", doctor.getPhone(), "the Admin-managed phone update must take effect");
            assertEquals("doctor1-updated@test.com", doctor.getEmail(), "the Admin-managed email update must take effect");

            // Assign the doctor to the manager, and confirm the relationship from both sides.
            env.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());
            assertEquals(manager.getManagerId(), doctor.getShiftManager().getManagerId(),
                    "the doctor must point at the assigned manager");
            assertTrue(manager.getManagedDoctors().contains(doctor), "the manager must list the assigned doctor");

            // Reload users.txt through a brand new UserManager and confirm the assignment survived.
            UserManager reloadedUsersOnly = new UserManager(env.usersFile);
            Doctor reloadedDoctorOnly = reloadedUsersOnly.findDoctorById(doctor.getDoctorId());
            MedicalManager reloadedManagerOnly = reloadedUsersOnly.findMedicalManagerById(manager.getManagerId());
            assertNotNull(reloadedDoctorOnly.getShiftManager(), "the reloaded doctor must still have a manager");
            assertEquals(reloadedManagerOnly.getManagerId(), reloadedDoctorOnly.getShiftManager().getManagerId(),
                    "the reloaded doctor's manager must match the reloaded manager");
            assertTrue(reloadedManagerOnly.getManagedDoctors().contains(reloadedDoctorOnly),
                    "the reloaded manager must still list the reloaded doctor");

            // ---- Stage B: create one hospital asset of every type ----
            HospitalAsset consultationRoom = env.assetManager.createAsset(
                    "Consult Room 1", "CONSULTATION_ROOM", "Ground Floor", true, admin.getStaffId());
            HospitalAsset clinic = env.assetManager.createAsset(
                    "Dermatology Clinic", "CLINIC", "Block B", true, admin.getStaffId());
            HospitalAsset ward = env.assetManager.createAsset("Ward 3", "WARD", "Third Floor", true, admin.getStaffId());
            HospitalAsset lab = env.assetManager.createAsset("Lab Room 1", "LAB", "Ground Floor", true, admin.getStaffId());
            HospitalAsset xrayRoom = env.assetManager.createAsset(
                    "X-Ray Room 1", "XRAY_ROOM", "First Floor", true, admin.getStaffId());
            HospitalAsset imagingRoom = env.assetManager.createAsset(
                    "Imaging Room 1", "IMAGING_ROOM", "First Floor", true, admin.getStaffId());

            assertEquals("CONSULTATION_ROOM", consultationRoom.getAssetType(), "the consultation room's type must be canonical");
            assertEquals("CLINIC", clinic.getAssetType(), "the clinic's type must be canonical");
            assertEquals("WARD", ward.getAssetType(), "the ward's type must be canonical");
            assertEquals("LAB", lab.getAssetType(), "the lab's type must be canonical");
            assertEquals("XRAY_ROOM", xrayRoom.getAssetType(), "the X-ray room's type must be canonical");
            assertEquals("IMAGING_ROOM", imagingRoom.getAssetType(), "the imaging room's type must be canonical");
            assertTrue(lab.isAvailability(), "a newly created asset must start operationally available");
            assertEquals(admin.getStaffId(), lab.getManagedBy().getStaffId(), "the lab must reference the creating Admin");

            HospitalAssetManager reloadedAssetsOnly = new HospitalAssetManager(reloadedUsersOnly, env.assetsFile);
            assertEquals(6, reloadedAssetsOnly.getAssets().size(), "all six created assets must reload");
            HospitalAsset reloadedLabOnly = reloadedAssetsOnly.findById(lab.getAssetId());
            assertEquals("LAB", reloadedLabOnly.getAssetType(), "the reloaded lab's type must survive reload");
            assertEquals(admin.getStaffId(), reloadedLabOnly.getManagedBy().getStaffId(),
                    "the reloaded lab's managing Admin must survive reload");

            // ---- Stage C: create a consultation rate and confirm it becomes the current rate ----
            ConsultationRate rate = env.rateManager.createRate("General Checkup", 50.0, date("2020-01-01"), admin.getStaffId());
            ConsultationRate currentRate = env.rateManager.getCurrentActiveRate();
            assertNotNull(currentRate, "a current active rate must be selected");
            assertEquals(rate.getRateId(), currentRate.getRateId(), "the only active, past-dated rate must be the current rate");

            ConsultationRateManager reloadedRatesOnly = new ConsultationRateManager(reloadedUsersOnly, env.ratesFile);
            ConsultationRate reloadedRateOnly = reloadedRatesOnly.findById(rate.getRateId());
            assertEquals("General Checkup", reloadedRateOnly.getConsultationType(), "the reloaded rate's type must survive reload");
            assertEquals(50.0, reloadedRateOnly.getBaseAmount(), "the reloaded rate's amount must survive reload");
            assertEquals(admin.getStaffId(), reloadedRateOnly.getConfiguredBy().getStaffId(),
                    "the reloaded rate's configuring Admin must survive reload");

            // ---- Stage D: create an accepted insurance network ----
            InsuranceNetwork network = env.insuranceManager.createNetwork(
                    "HealthCo", "Gold Plan", 80.0, admin.getStaffId());
            assertEquals(80.0, network.getCoveragePercentage(), "the network's coverage percentage must be stored");
            assertTrue(network.isActive(), "a newly created network must start active");

            InsuranceNetworkManager reloadedInsuranceOnly = new InsuranceNetworkManager(reloadedUsersOnly, env.insuranceFile);
            InsuranceNetwork reloadedNetworkOnly = reloadedInsuranceOnly.findById(network.getNetworkId());
            assertEquals("HealthCo", reloadedNetworkOnly.getProviderName(), "the reloaded network's provider must survive reload");
            assertEquals(80.0, reloadedNetworkOnly.getCoveragePercentage(), "the reloaded network's coverage must survive reload");
            assertTrue(reloadedNetworkOnly.isActive(), "the reloaded network's active status must survive reload");
            assertEquals(admin.getStaffId(), reloadedNetworkOnly.getManagedBy().getStaffId(),
                    "the reloaded network's managing Admin must survive reload");

            // ---- Stage E: medical request and asset allocation workflow ----
            // A doctor shift and a booked appointment are the existing-design prerequisites for a
            // medical record, which is itself the prerequisite for a lab/imaging request.
            Date shiftDate = date("2027-06-01");
            LocalTime shiftStart = LocalTime.parse("09:00");
            LocalTime shiftEnd = LocalTime.parse("17:00");
            env.shiftManager.createShift(manager.getManagerId(), doctor.getDoctorId(), shiftDate, shiftStart, shiftEnd);
            Appointment appointment = env.appointmentManager.book(patient.getPatientId(), doctor.getDoctorId(), shiftDate, shiftStart);
            assertEquals("CONFIRMED", appointment.getStatus(), "a newly booked appointment must start CONFIRMED");

            MedicalRecord record = env.clinicalManager.getOrOpenRecord(appointment.getAppointmentId(), doctor.getDoctorId());

            // -- LAB request: PENDING -> APPROVED -> ALLOCATED -> COMPLETED --
            MedicalRequest labRequest = env.clinicalManager.addRequest(record.getRecordId(), "LAB", "Routine blood work");
            assertEquals("PENDING", labRequest.getStatus(), "a newly created request must start PENDING");

            env.clinicalManager.approveRequest(labRequest.getRequestId(), admin.getStaffId());
            assertEquals("APPROVED", labRequest.getStatus(), "an approved request must become APPROVED");

            AssetAllocation labAllocation = env.allocationManager.createAllocation(labRequest.getRequestId(),
                    lab.getAssetId(), admin.getStaffId(), dateTime("2027-06-01 10:00:00"), dateTime("2027-06-01 11:00:00"));
            assertEquals("ALLOCATED", labRequest.getStatus(), "allocating an asset must move the request to ALLOCATED");
            assertEquals(AssetAllocation.STATUS_SCHEDULED, labAllocation.getAllocationStatus(),
                    "a new allocation must start SCHEDULED");

            env.allocationManager.completeAllocation(labAllocation.getAllocationId());
            assertEquals(AssetAllocation.STATUS_COMPLETED, labAllocation.getAllocationStatus(),
                    "completing an allocation must mark it COMPLETED");
            assertEquals("COMPLETED", labRequest.getStatus(), "completing the allocation must mark the request COMPLETED");

            // -- X_RAY request on the same record: the same workflow, a different compatible asset --
            MedicalRequest xrayRequest = env.clinicalManager.addRequest(record.getRecordId(), "X_RAY", "Chest X-ray");
            env.clinicalManager.approveRequest(xrayRequest.getRequestId(), admin.getStaffId());
            AssetAllocation xrayAllocation = env.allocationManager.createAllocation(xrayRequest.getRequestId(),
                    xrayRoom.getAssetId(), admin.getStaffId(), dateTime("2027-06-01 11:30:00"), dateTime("2027-06-01 12:00:00"));
            assertEquals("ALLOCATED", xrayRequest.getStatus(), "allocating the X-ray asset must move that request to ALLOCATED");
            env.allocationManager.completeAllocation(xrayAllocation.getAllocationId());
            assertEquals("COMPLETED", xrayRequest.getStatus(), "completing the X-ray allocation must mark that request COMPLETED");

            // ---- Stage G: reload every manager from the same temporary files and re-verify everything ----
            env.reloadEverythingFromDisk();

            Doctor reloadedDoctor = env.userManager.findDoctorById(doctor.getDoctorId());
            MedicalManager reloadedManager = env.userManager.findMedicalManagerById(manager.getManagerId());
            assertEquals(reloadedManager.getManagerId(), reloadedDoctor.getShiftManager().getManagerId(),
                    "Doctor -> Medical Manager must still be linked after a full reload");

            MedicalRequest reloadedLabRequest = env.clinicalManager.findRequestById(labRequest.getRequestId());
            assertEquals("COMPLETED", reloadedLabRequest.getStatus(), "the reloaded LAB request must still be COMPLETED");
            assertEquals(doctor.getDoctorId(), reloadedLabRequest.getDoctor().getDoctorId(),
                    "medical request -> Doctor must still be linked after a full reload");
            assertEquals(patient.getPatientId(), reloadedLabRequest.getMedicalRecord().getPatient().getPatientId(),
                    "medical request -> Patient (via its record) must still be linked after a full reload");

            List<AssetAllocation> reloadedLabAllocations = env.allocationManager.getAllocationsForRequest(labRequest.getRequestId());
            assertEquals(1, reloadedLabAllocations.size(), "exactly one allocation must reload for the LAB request");
            AssetAllocation reloadedLabAllocation = reloadedLabAllocations.get(0);
            assertEquals(AssetAllocation.STATUS_COMPLETED, reloadedLabAllocation.getAllocationStatus(),
                    "the reloaded LAB allocation must still be COMPLETED");
            assertEquals(lab.getAssetId(), reloadedLabAllocation.getAssetId(),
                    "asset allocation -> asset must still be linked after a full reload");
            assertNotNull(env.assetManager.findById(reloadedLabAllocation.getAssetId()),
                    "the allocated asset itself must still exist after a full reload");

            List<AssetAllocation> reloadedXrayAllocations = env.allocationManager.getAllocationsForRequest(xrayRequest.getRequestId());
            assertEquals(1, reloadedXrayAllocations.size(), "exactly one allocation must reload for the X_RAY request");
            assertEquals("COMPLETED", env.clinicalManager.findRequestById(xrayRequest.getRequestId()).getStatus(),
                    "the reloaded X_RAY request must still be COMPLETED");

            ConsultationRate reloadedRate = env.rateManager.findById(rate.getRateId());
            assertEquals(admin.getStaffId(), reloadedRate.getConfiguredBy().getStaffId(),
                    "rate -> managing Admin must still be linked after a full reload");

            InsuranceNetwork reloadedNetwork = env.insuranceManager.findById(network.getNetworkId());
            assertEquals(admin.getStaffId(), reloadedNetwork.getManagedBy().getStaffId(),
                    "insurance network -> managing Admin must still be linked after a full reload");

            // No malformed or duplicate records: exactly the records this scenario created.
            assertEquals(4, env.userManager.getUsers().size(), "exactly the four registered users must remain after reload");
            assertEquals(6, env.assetManager.getAssets().size(), "exactly the six created assets must remain after reload");
            assertEquals(1, env.rateManager.getRates().size(), "exactly the one created rate must remain after reload");
            assertEquals(1, env.insuranceManager.getNetworks().size(), "exactly the one created network must remain after reload");
            assertEquals(2, env.clinicalManager.getAllRequests().size(), "exactly the two created requests must remain after reload");
            assertEquals(2, env.allocationManager.getAllocations().size(), "exactly the two created allocations must remain after reload");

            // Every temporary data file must still have the exact field count its format requires.
            assertEveryLineHasFieldCount(env.usersFile, 10, "users.txt");
            assertEveryLineHasFieldCount(env.appointmentsFile, 6, "appointments.txt");
            assertEveryLineHasFieldCount(env.assetsFile, 6, "hospital_assets.txt");
            assertEveryLineHasFieldCount(env.ratesFile, 6, "consultation_rates.txt");
            assertEveryLineHasFieldCount(env.insuranceFile, 6, "insurance_networks.txt");
            assertEveryLineHasFieldCount(env.requestsFile, 10, "medical_requests.txt");
            assertEveryLineHasFieldCount(env.allocationsFile, 9, "asset_allocations.txt");
        }
    }

    // =====================================================================================
    // F: representative rejection checks - each verifies state and file are unchanged after
    // =====================================================================================

    private void testInactiveDoctorCannotReceiveNewAssignment() throws Exception {
        try (Environment env = new Environment()) {
            AdminStaff admin = registerAdmin(env);
            MedicalManager manager = registerManager(env);
            Doctor doctor = registerDoctor(env);
            env.userManager.deactivateUser(doctor.getUserId());

            List<String> usersBefore = readFileLines(env.usersFile);

            boolean rejected = false;
            try {
                env.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());
            } catch (IllegalArgumentException ex) {
                rejected = true;
            }
            assertTrue(rejected, "an inactive Doctor must not receive a new assignment");
            assertNull(doctor.getShiftManager(), "the rejected assignment must leave the doctor unassigned");
            assertTrue(!manager.getManagedDoctors().contains(doctor), "the rejected assignment must not list the doctor under the manager");

            List<String> usersAfter = readFileLines(env.usersFile);
            assertEquals(usersBefore, usersAfter, "users.txt must be byte-for-byte unchanged after the rejected assignment");

            UserManager reloaded = new UserManager(env.usersFile);
            assertNull(reloaded.findDoctorById(doctor.getDoctorId()).getShiftManager(),
                    "the persisted file must not show the rejected assignment either");
        }
    }

    private void testInactiveManagerCannotReceiveNewAssignment() throws Exception {
        try (Environment env = new Environment()) {
            MedicalManager manager = registerManager(env);
            Doctor doctor = registerDoctor(env);
            env.userManager.deactivateUser(manager.getUserId());

            List<String> usersBefore = readFileLines(env.usersFile);

            boolean rejected = false;
            try {
                env.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());
            } catch (IllegalArgumentException ex) {
                rejected = true;
            }
            assertTrue(rejected, "an inactive Medical Manager must not receive a new assignment");
            assertNull(doctor.getShiftManager(), "the rejected assignment must leave the doctor unassigned");

            List<String> usersAfter = readFileLines(env.usersFile);
            assertEquals(usersBefore, usersAfter, "users.txt must be byte-for-byte unchanged after the rejected assignment");

            UserManager reloaded = new UserManager(env.usersFile);
            assertNull(reloaded.findDoctorById(doctor.getDoctorId()).getShiftManager(),
                    "the reloaded doctor must still show no assignment after the rejected attempt");
        }
    }

    private void testIncompatibleAssetCannotBeAllocated() throws Exception {
        try (Environment env = new Environment()) {
            AdminStaff admin = registerAdmin(env);
            MedicalManager manager = registerManager(env);
            Doctor doctor = registerDoctor(env);
            Patient patient = registerPatient(env);
            env.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());
            HospitalAsset xrayRoom = env.assetManager.createAsset("X-Ray Room 1", "XRAY_ROOM", "First Floor", true,
                    admin.getStaffId());

            Date shiftDate = date("2027-06-01");
            LocalTime shiftStart = LocalTime.parse("09:00");
            env.shiftManager.createShift(manager.getManagerId(), doctor.getDoctorId(), shiftDate, shiftStart, LocalTime.parse("17:00"));
            Appointment appointment = env.appointmentManager.book(patient.getPatientId(), doctor.getDoctorId(), shiftDate, shiftStart);
            MedicalRecord record = env.clinicalManager.getOrOpenRecord(appointment.getAppointmentId(), doctor.getDoctorId());
            MedicalRequest labRequest = env.clinicalManager.addRequest(record.getRecordId(), "LAB", "Routine blood work");
            env.clinicalManager.approveRequest(labRequest.getRequestId(), admin.getStaffId());

            List<String> requestsBefore = readFileLines(env.requestsFile);
            List<String> allocationsBefore = readFileLines(env.allocationsFile);

            boolean rejected = false;
            try {
                env.allocationManager.createAllocation(labRequest.getRequestId(), xrayRoom.getAssetId(),
                        admin.getStaffId(), dateTime("2027-06-01 10:00:00"), dateTime("2027-06-01 11:00:00"));
            } catch (IllegalArgumentException ex) {
                rejected = true;
            }
            assertTrue(rejected, "a LAB request must not accept an XRAY_ROOM asset");
            assertEquals("APPROVED", labRequest.getStatus(), "the request must remain APPROVED after the rejected allocation");
            assertTrue(env.allocationManager.getAllocationsForRequest(labRequest.getRequestId()).isEmpty(),
                    "no allocation must have been created for the rejected attempt");

            List<String> requestsAfter = readFileLines(env.requestsFile);
            List<String> allocationsAfter = readFileLines(env.allocationsFile);
            assertEquals(requestsBefore, requestsAfter, "medical_requests.txt must be byte-for-byte unchanged after the rejected allocation");
            assertEquals(allocationsBefore, allocationsAfter, "asset_allocations.txt must be byte-for-byte unchanged after the rejected allocation");

            env.reloadEverythingFromDisk();
            MedicalRequest reloadedRequest = env.clinicalManager.findRequestById(labRequest.getRequestId());
            assertEquals("APPROVED", reloadedRequest.getStatus(), "the reloaded request must still be APPROVED");
            assertTrue(env.allocationManager.getAllocationsForRequest(labRequest.getRequestId()).isEmpty(),
                    "the reloaded request must still have no allocation");
        }
    }

    private void testOverlappingAllocationOnSameAssetIsRejected() throws Exception {
        try (Environment env = new Environment()) {
            AdminStaff admin = registerAdmin(env);
            MedicalManager manager = registerManager(env);
            Doctor doctor = registerDoctor(env);
            Patient patient = registerPatient(env);
            env.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());
            HospitalAsset lab = env.assetManager.createAsset("Lab Room 1", "LAB", "Ground Floor", true, admin.getStaffId());

            Date shiftDate = date("2027-06-01");
            env.shiftManager.createShift(manager.getManagerId(), doctor.getDoctorId(), shiftDate,
                    LocalTime.parse("09:00"), LocalTime.parse("17:00"));
            Appointment appointment = env.appointmentManager.book(patient.getPatientId(), doctor.getDoctorId(), shiftDate,
                    LocalTime.parse("09:00"));
            MedicalRecord record = env.clinicalManager.getOrOpenRecord(appointment.getAppointmentId(), doctor.getDoctorId());

            MedicalRequest firstRequest = env.clinicalManager.addRequest(record.getRecordId(), "LAB", "First test");
            env.clinicalManager.approveRequest(firstRequest.getRequestId(), admin.getStaffId());
            env.allocationManager.createAllocation(firstRequest.getRequestId(), lab.getAssetId(), admin.getStaffId(),
                    dateTime("2027-06-01 10:00:00"), dateTime("2027-06-01 11:00:00"));

            MedicalRequest secondRequest = env.clinicalManager.addRequest(record.getRecordId(), "LAB", "Second test");
            env.clinicalManager.approveRequest(secondRequest.getRequestId(), admin.getStaffId());

            List<String> requestsBefore = readFileLines(env.requestsFile);
            List<String> allocationsBefore = readFileLines(env.allocationsFile);

            boolean rejected = false;
            try {
                env.allocationManager.createAllocation(secondRequest.getRequestId(), lab.getAssetId(),
                        admin.getStaffId(), dateTime("2027-06-01 10:30:00"), dateTime("2027-06-01 11:30:00"));
            } catch (IllegalStateException ex) {
                rejected = true;
            }
            assertTrue(rejected, "an overlapping time range on the same asset must be rejected");
            assertEquals("APPROVED", secondRequest.getStatus(), "the second request must remain APPROVED after the rejected overlap");
            assertEquals(1, env.allocationManager.getAllocationsForAsset(lab.getAssetId()).size(),
                    "only the first, non-overlapping allocation must exist for the asset");

            List<String> requestsAfter = readFileLines(env.requestsFile);
            List<String> allocationsAfter = readFileLines(env.allocationsFile);
            assertEquals(requestsBefore, requestsAfter, "medical_requests.txt must be byte-for-byte unchanged after the rejected overlap");
            assertEquals(allocationsBefore, allocationsAfter, "asset_allocations.txt must be byte-for-byte unchanged after the rejected overlap");

            env.reloadEverythingFromDisk();
            assertEquals(1, env.allocationManager.getAllocationsForAsset(lab.getAssetId()).size(),
                    "after reload, only the original allocation must exist for the asset");
            MedicalRequest reloadedSecondRequest = env.clinicalManager.findRequestById(secondRequest.getRequestId());
            assertEquals("APPROVED", reloadedSecondRequest.getStatus(), "after reload, the second request must still be APPROVED");
        }
    }

    private void testPipeInAssetNameIsRejectedWithoutCorruptingFile() throws Exception {
        try (Environment env = new Environment()) {
            AdminStaff admin = registerAdmin(env);
            env.assetManager.createAsset("Lab Room 1", "LAB", "Ground Floor", true, admin.getStaffId());
            List<String> beforeContent = readFileLines(env.assetsFile);

            boolean pipeRejected = false;
            try {
                env.assetManager.createAsset("Lab|Room 2", "LAB", "First Floor", true, admin.getStaffId());
            } catch (IllegalArgumentException ex) {
                pipeRejected = true;
            }
            assertTrue(pipeRejected, "an asset name containing '|' must be rejected");

            boolean newlineRejected = false;
            try {
                env.assetManager.createAsset("Lab\nRoom 3", "LAB", "First Floor", true, admin.getStaffId());
            } catch (IllegalArgumentException ex) {
                newlineRejected = true;
            }
            assertTrue(newlineRejected, "an asset name containing a newline must be rejected");

            assertEquals(1, env.assetManager.getAssets().size(), "only the one valid asset must exist after the rejected attempts");
            List<String> afterContent = readFileLines(env.assetsFile);
            assertEquals(beforeContent, afterContent, "the asset file must be byte-for-byte unchanged after rejected writes");
        }
    }

    private void testAssetWithAllocationHistoryCannotBeDeleted() throws Exception {
        try (Environment env = new Environment()) {
            AdminStaff admin = registerAdmin(env);
            MedicalManager manager = registerManager(env);
            Doctor doctor = registerDoctor(env);
            Patient patient = registerPatient(env);
            env.userManager.assignDoctorToManager(doctor.getDoctorId(), manager.getManagerId());
            HospitalAsset lab = env.assetManager.createAsset("Lab Room 1", "LAB", "Ground Floor", true, admin.getStaffId());

            Date shiftDate = date("2027-06-01");
            env.shiftManager.createShift(manager.getManagerId(), doctor.getDoctorId(), shiftDate,
                    LocalTime.parse("09:00"), LocalTime.parse("17:00"));
            Appointment appointment = env.appointmentManager.book(patient.getPatientId(), doctor.getDoctorId(), shiftDate,
                    LocalTime.parse("09:00"));
            MedicalRecord record = env.clinicalManager.getOrOpenRecord(appointment.getAppointmentId(), doctor.getDoctorId());
            MedicalRequest request = env.clinicalManager.addRequest(record.getRecordId(), "LAB", "Routine blood work");
            env.clinicalManager.approveRequest(request.getRequestId(), admin.getStaffId());
            env.allocationManager.createAllocation(request.getRequestId(), lab.getAssetId(), admin.getStaffId(),
                    dateTime("2027-06-01 10:00:00"), dateTime("2027-06-01 11:00:00"));

            List<String> assetsBefore = readFileLines(env.assetsFile);

            // This is the same two-call sequence HospitalAssetManagementPanel uses before deleting:
            // ask AssetAllocationManager whether the asset is safe to delete, then only delete if so.
            boolean rejected = false;
            try {
                env.allocationManager.validateAssetCanBeDeleted(lab.getAssetId());
            } catch (IllegalStateException ex) {
                rejected = true;
            }
            assertTrue(rejected, "an asset with allocation history must not be reported as safe to delete");
            assertNotNull(env.assetManager.findById(lab.getAssetId()),
                    "the asset must still exist, since nothing ever called deleteAsset() after the rejected validation");

            List<String> assetsAfter = readFileLines(env.assetsFile);
            assertEquals(assetsBefore, assetsAfter, "hospital_assets.txt must be byte-for-byte unchanged after the rejected safety validation");
        }
    }

    // ---- small shared helpers for the rejection tests (kept out of the main scenario for clarity) ----

    private AdminStaff registerAdmin(Environment env) {
        return (AdminStaff) env.userManager.registerUser(
                new AdminStaff(null, "admin1", "pass1234", "0111111111", "admin1@test.com", "ADMIN_STAFF", null));
    }

    private MedicalManager registerManager(Environment env) {
        return (MedicalManager) env.userManager.registerUser(
                new MedicalManager(null, "manager1", "pass1234", "0122222222", "manager1@test.com",
                        "MEDICAL_MANAGER", null));
    }

    private Doctor registerDoctor(Environment env) {
        return (Doctor) env.userManager.registerUser(
                new Doctor(null, "doctor1", "pass1234", "0133333333", "doctor1@test.com", "DOCTOR", null, "Cardiology"));
    }

    private Patient registerPatient(Environment env) {
        return (Patient) env.userManager.registerUser(
                new Patient(null, "patient1", "pass1234", "0144444444", "patient1@test.com", "PATIENT", null,
                        date("1990-01-01")));
    }

    // =====================================================================================
    // Read-only compatibility check against the real data files
    // =====================================================================================

    /**
     * The real data/ directory must never be written to by a test, and this project's managers
     * cannot be proven side-effect-free just by reading their source - so instead of pointing a
     * manager straight at the real files, this copies them into a fresh temporary directory first
     * and loads the copies. The check is still meaningful: the copies have byte-for-byte the same
     * content as the real files, so successfully loading them proves the real files remain
     * compatible with every validation rule Steps 1-9 added, without any risk to the originals.
     */
    private void testRealDataFilesLoadThroughTemporaryCopiesWithoutError() throws Exception {
        Path realDataDir = new File("data").toPath();
        if (!Files.exists(realDataDir)) {
            return;
        }
        Path tempDir = Files.createTempDirectory("hms-realdata-copy-test-");
        try {
            File usersFile = copyIfPresent(realDataDir, tempDir, "users.txt");
            File assetsFile = copyIfPresent(realDataDir, tempDir, "hospital_assets.txt");
            File ratesFile = copyIfPresent(realDataDir, tempDir, "consultation_rates.txt");
            File insuranceFile = copyIfPresent(realDataDir, tempDir, "insurance_networks.txt");

            // The compatibility check is simply that each manager's constructor - which parses the
            // file and throws IllegalStateException on a load failure (since Step 8) - completes
            // without throwing. There is nothing else to assert here: the real data's actual record
            // counts and contents legitimately change over time, so no fixed expected count can be
            // written without either duplicating production parsing logic or being tautological.
            UserManager userManager = new UserManager(usersFile);
            new HospitalAssetManager(userManager, assetsFile);
            new ConsultationRateManager(userManager, ratesFile);
            new InsuranceNetworkManager(userManager, insuranceFile);
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private static File copyIfPresent(Path realDataDir, Path tempDir, String fileName) throws IOException {
        File source = new File(realDataDir.toFile(), fileName);
        File destination = new File(tempDir.toFile(), fileName);
        if (source.exists()) {
            Files.copy(source.toPath(), destination.toPath());
        }
        return destination;
    }
}
