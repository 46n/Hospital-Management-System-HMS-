package hms.ui.admin;

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
import hms.ui.common.EditProfileNew;
import hms.ui.common.MainFrame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 * Standalone, dependency-free tests for the Admin dashboard's navigation wiring, now that Admin
 * has migrated onto AdminDashboardNew - a genuine NetBeans GUI Builder JPanel (the same pattern
 * as DoctorDashboardNew/MedicalManagerDashboardNew), replacing the older
 * AdminDashboardPanel/AdminNavigationPanel pair. Every dashboard button opens the intended screen
 * with the shared manager instances, every management screen's Back button returns to a fresh
 * AdminDashboardNew using the same session and managers, tables stay non-editable, and a
 * persistence failure on the shared Edit Profile screen is shown as an error instead of crashing.
 * Same hand-rolled runner style as the other Step test files, since the project has no JUnit on
 * its classpath.
 *
 * This file lives in package hms.ui.admin (matching the production classes it tests). Unlike the
 * retired AdminNavigationPanel, AdminDashboardNew's buttons are private fields on the dashboard
 * itself (the same shape as DoctorDashboardNew/MedicalManagerDashboardNew), so this test reaches
 * them the same way this project's other dashboard tests already do: reflection on the private
 * field, not a public getter.
 *
 * Run with (from the project root, after compiling src to build/classes):
 *   javac -d build/test-classes -cp build/classes test/HMS/ui/admin/AdminNavigationTest.java
 *   java -cp build/classes;build/test-classes hms.ui.admin.AdminNavigationTest
 */
public class AdminNavigationTest {

    private int passed = 0;
    private int failed = 0;

    public static void main(String[] args) throws Exception {
        AdminNavigationTest suite = new AdminNavigationTest();

        // ---- dashboard navigation ----
        suite.run("testAllEightFeatureButtonsAndLogoutExistAndEnabled",
                suite::testAllEightFeatureButtonsAndLogoutExistAndEnabled);
        suite.run("testEditProfileButtonOpensSharedEditProfileNew", suite::testEditProfileButtonOpensSharedEditProfileNew);
        suite.run("testManageUsersButtonOpensUserManagementPanel", suite::testManageUsersButtonOpensUserManagementPanel);
        suite.run("testManageDoctorsButtonOpensDoctorManagementPanel", suite::testManageDoctorsButtonOpensDoctorManagementPanel);
        suite.run("testViewAppointmentsButtonOpensAppointmentManagementPanel",
                suite::testViewAppointmentsButtonOpensAppointmentManagementPanel);
        suite.run("testManageRequestsButtonOpensMedicalRequestManagementPanel",
                suite::testManageRequestsButtonOpensMedicalRequestManagementPanel);
        suite.run("testManageAssetsButtonOpensHospitalAssetManagementPanel",
                suite::testManageAssetsButtonOpensHospitalAssetManagementPanel);
        suite.run("testManageRatesButtonOpensConsultationRateManagementPanel",
                suite::testManageRatesButtonOpensConsultationRateManagementPanel);
        suite.run("testManageInsuranceButtonOpensInsuranceNetworkManagementPanel",
                suite::testManageInsuranceButtonOpensInsuranceNetworkManagementPanel);

        // ---- shared manager wiring ----
        suite.run("testManageUsersButtonPassesSharedManagerInstances",
                suite::testManageUsersButtonPassesSharedManagerInstances);
        suite.run("testManageAssetsButtonPassesSharedManagerInstances",
                suite::testManageAssetsButtonPassesSharedManagerInstances);

        // ---- back navigation ----
        suite.run("testUserManagementBackButtonReturnsToDashboard", suite::testUserManagementBackButtonReturnsToDashboard);
        suite.run("testHospitalAssetBackButtonReturnsToDashboard", suite::testHospitalAssetBackButtonReturnsToDashboard);
        suite.run("testConsultationRateBackButtonReturnsToDashboard", suite::testConsultationRateBackButtonReturnsToDashboard);
        suite.run("testInsuranceNetworkBackButtonReturnsToDashboard", suite::testInsuranceNetworkBackButtonReturnsToDashboard);
        suite.run("testDoctorManagementBackButtonReturnsToDashboard", suite::testDoctorManagementBackButtonReturnsToDashboard);
        suite.run("testAppointmentManagementBackButtonReturnsToDashboard",
                suite::testAppointmentManagementBackButtonReturnsToDashboard);
        suite.run("testMedicalRequestManagementBackButtonReturnsToDashboard",
                suite::testMedicalRequestManagementBackButtonReturnsToDashboard);
        suite.run("testEditProfileBackButtonReturnsToSameDashboardInstance",
                suite::testEditProfileBackButtonReturnsToSameDashboardInstance);
        suite.run("testBackNavigationPreservesLoggedInSession", suite::testBackNavigationPreservesLoggedInSession);

        // ---- logout ----
        suite.run("testLogoutButtonLogsOutAndRunsSuppliedCallback", suite::testLogoutButtonLogsOutAndRunsSuppliedCallback);

        // ---- table and state behavior ----
        suite.run("testAllAdminTablesAreNonEditable", suite::testAllAdminTablesAreNonEditable);
        suite.run("testToggleAvailabilityRefreshesAssetTableUsingAssetId",
                suite::testToggleAvailabilityRefreshesAssetTableUsingAssetId);
        suite.run("testUpdateOwnProfileFailureThrowsTheExceptionTypeEditProfileNewCatches",
                suite::testUpdateOwnProfileFailureThrowsTheExceptionTypeEditProfileNewCatches);

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
            tempDir = Files.createTempDirectory("hms-adminnav-test-");
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
            sessionManager.login("admin1", "pass1234");
        }

        /** Builds the dashboard exactly the way Main.java does, with every shared manager. */
        AdminDashboardNew newDashboard(MainFrame mainFrame) {
            return newDashboard(mainFrame, () -> { });
        }

        AdminDashboardNew newDashboard(MainFrame mainFrame, Runnable onLogout) {
            return new AdminDashboardNew(sessionManager, userManager, appointmentManager, clinicalManager,
                    hospitalAssetManager, rateManager, insuranceManager, allocationManager, mainFrame, onLogout);
        }

        UserManagementPanel newUserPanel(MainFrame mainFrame) {
            return new UserManagementPanel(userManager, appointmentManager, clinicalManager, hospitalAssetManager,
                    rateManager, insuranceManager, allocationManager, sessionManager, mainFrame, () -> { });
        }

        HospitalAssetManagementPanel newAssetPanel(MainFrame mainFrame) {
            return new HospitalAssetManagementPanel(hospitalAssetManager, userManager, appointmentManager,
                    clinicalManager, rateManager, insuranceManager, allocationManager, sessionManager, mainFrame,
                    () -> { });
        }

        ConsultationRateManagementPanel newRatePanel(MainFrame mainFrame) {
            return new ConsultationRateManagementPanel(rateManager, userManager, appointmentManager, clinicalManager,
                    hospitalAssetManager, insuranceManager, allocationManager, sessionManager, mainFrame, () -> { });
        }

        InsuranceNetworkManagementPanel newInsurancePanel(MainFrame mainFrame) {
            return new InsuranceNetworkManagementPanel(insuranceManager, userManager, appointmentManager,
                    clinicalManager, hospitalAssetManager, rateManager, allocationManager, sessionManager, mainFrame,
                    () -> { });
        }

        DoctorManagementPanel newDoctorPanel(MainFrame mainFrame) {
            return new DoctorManagementPanel(userManager, appointmentManager, clinicalManager, hospitalAssetManager,
                    rateManager, insuranceManager, allocationManager, sessionManager, mainFrame, () -> { });
        }

        AppointmentManagementPanel newAppointmentPanel(MainFrame mainFrame) {
            return new AppointmentManagementPanel(appointmentManager, userManager, clinicalManager,
                    hospitalAssetManager, rateManager, insuranceManager, allocationManager, sessionManager, mainFrame,
                    () -> { });
        }

        MedicalRequestManagementPanel newRequestPanel(MainFrame mainFrame) {
            return new MedicalRequestManagementPanel(clinicalManager, userManager, appointmentManager,
                    hospitalAssetManager, rateManager, insuranceManager, allocationManager, sessionManager, mainFrame,
                    () -> { });
        }

        @Override
        public void close() throws IOException {
            deleteRecursively(tempDir);
        }
    }

    /**
     * A MainFrame that never actually shows a window - it just remembers the last panel it was
     * asked to display, so a test can check which screen a button navigated to without opening
     * any real window or relying on Swing's CardLayout internals.
     */
    private static final class RecordingMainFrame extends MainFrame {
        private JPanel lastPanel;

        @Override
        public void showPanel(JPanel panel) {
            lastPanel = panel;
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

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
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

    private static void assertTableNotEditable(JTable table, String label) {
        assertTrue(!table.getModel().isCellEditable(0, 0), "the " + label + " must not be directly editable");
    }

    // ---- tests: dashboard navigation ----

    private void testAllEightFeatureButtonsAndLogoutExistAndEnabled() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            AdminDashboardNew dashboard = fixture.newDashboard(mainFrame);

            JButton[] buttons = {
                    (JButton) getPrivateField(dashboard, "btnEditProfile"),
                    (JButton) getPrivateField(dashboard, "btnManageUsers"),
                    (JButton) getPrivateField(dashboard, "btnManageDoctors"),
                    (JButton) getPrivateField(dashboard, "btnViewAppointments"),
                    (JButton) getPrivateField(dashboard, "btnMedicalRequests"),
                    (JButton) getPrivateField(dashboard, "btnManageAssets"),
                    (JButton) getPrivateField(dashboard, "btnManageRates"),
                    (JButton) getPrivateField(dashboard, "btnManageInsurance"),
                    (JButton) getPrivateField(dashboard, "btnLogout")
            };
            for (JButton button : buttons) {
                assertNotNull(button, "every one of the eight feature buttons plus Logout must exist");
                assertTrue(button.isEnabled(), "every one of the eight feature buttons plus Logout must be enabled");
            }
        }
    }

    private void testEditProfileButtonOpensSharedEditProfileNew() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            AdminDashboardNew dashboard = fixture.newDashboard(mainFrame);

            ((JButton) getPrivateField(dashboard, "btnEditProfile")).doClick();

            assertTrue(mainFrame.lastPanel instanceof EditProfileNew,
                    "Edit Profile must open the group's shared EditProfileNew screen");
        }
    }

    private void testManageUsersButtonOpensUserManagementPanel() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            AdminDashboardNew dashboard = fixture.newDashboard(mainFrame);

            ((JButton) getPrivateField(dashboard, "btnManageUsers")).doClick();

            assertTrue(mainFrame.lastPanel instanceof UserManagementPanel, "Manage Users must open UserManagementPanel");
        }
    }

    private void testManageDoctorsButtonOpensDoctorManagementPanel() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            AdminDashboardNew dashboard = fixture.newDashboard(mainFrame);

            ((JButton) getPrivateField(dashboard, "btnManageDoctors")).doClick();

            assertTrue(mainFrame.lastPanel instanceof DoctorManagementPanel, "Manage Doctors must open DoctorManagementPanel");
        }
    }

    private void testViewAppointmentsButtonOpensAppointmentManagementPanel() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            AdminDashboardNew dashboard = fixture.newDashboard(mainFrame);

            ((JButton) getPrivateField(dashboard, "btnViewAppointments")).doClick();

            assertTrue(mainFrame.lastPanel instanceof AppointmentManagementPanel,
                    "View Appointments must open AppointmentManagementPanel");
        }
    }

    private void testManageRequestsButtonOpensMedicalRequestManagementPanel() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            AdminDashboardNew dashboard = fixture.newDashboard(mainFrame);

            ((JButton) getPrivateField(dashboard, "btnMedicalRequests")).doClick();

            assertTrue(mainFrame.lastPanel instanceof MedicalRequestManagementPanel,
                    "Medical Requests must open MedicalRequestManagementPanel");
        }
    }

    private void testManageAssetsButtonOpensHospitalAssetManagementPanel() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            AdminDashboardNew dashboard = fixture.newDashboard(mainFrame);

            ((JButton) getPrivateField(dashboard, "btnManageAssets")).doClick();

            assertTrue(mainFrame.lastPanel instanceof HospitalAssetManagementPanel,
                    "Manage Hospital Assets must open HospitalAssetManagementPanel");
        }
    }

    private void testManageRatesButtonOpensConsultationRateManagementPanel() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            AdminDashboardNew dashboard = fixture.newDashboard(mainFrame);

            ((JButton) getPrivateField(dashboard, "btnManageRates")).doClick();

            assertTrue(mainFrame.lastPanel instanceof ConsultationRateManagementPanel,
                    "Manage Consultation Rates must open ConsultationRateManagementPanel");
        }
    }

    private void testManageInsuranceButtonOpensInsuranceNetworkManagementPanel() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            AdminDashboardNew dashboard = fixture.newDashboard(mainFrame);

            ((JButton) getPrivateField(dashboard, "btnManageInsurance")).doClick();

            assertTrue(mainFrame.lastPanel instanceof InsuranceNetworkManagementPanel,
                    "Manage Insurance Networks must open InsuranceNetworkManagementPanel");
        }
    }

    // ---- tests: shared manager wiring ----

    private void testManageUsersButtonPassesSharedManagerInstances() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            AdminDashboardNew dashboard = fixture.newDashboard(mainFrame);

            ((JButton) getPrivateField(dashboard, "btnManageUsers")).doClick();

            Object wiredUserManager = getPrivateField(mainFrame.lastPanel, "userManager");
            assertTrue(wiredUserManager == fixture.userManager,
                    "UserManagementPanel must receive the same shared UserManager, not a new one");
        }
    }

    private void testManageAssetsButtonPassesSharedManagerInstances() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            AdminDashboardNew dashboard = fixture.newDashboard(mainFrame);

            ((JButton) getPrivateField(dashboard, "btnManageAssets")).doClick();

            Object wiredAssetManager = getPrivateField(mainFrame.lastPanel, "assetManager");
            Object wiredAllocationManager = getPrivateField(mainFrame.lastPanel, "allocationManager");
            assertTrue(wiredAssetManager == fixture.hospitalAssetManager,
                    "HospitalAssetManagementPanel must receive the same shared HospitalAssetManager, not a new one");
            assertTrue(wiredAllocationManager == fixture.allocationManager,
                    "HospitalAssetManagementPanel must receive the same shared AssetAllocationManager, not a new one");
        }
    }

    // ---- tests: back navigation ----

    private void testUserManagementBackButtonReturnsToDashboard() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            UserManagementPanel panel = fixture.newUserPanel(mainFrame);

            ((JButton) getPrivateField(panel, "backButton")).doClick();

            assertTrue(mainFrame.lastPanel instanceof AdminDashboardNew, "Back must return to AdminDashboardNew");
        }
    }

    private void testHospitalAssetBackButtonReturnsToDashboard() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            HospitalAssetManagementPanel panel = fixture.newAssetPanel(mainFrame);

            ((JButton) getPrivateField(panel, "backButton")).doClick();

            assertTrue(mainFrame.lastPanel instanceof AdminDashboardNew, "Back must return to AdminDashboardNew");
        }
    }

    private void testConsultationRateBackButtonReturnsToDashboard() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            ConsultationRateManagementPanel panel = fixture.newRatePanel(mainFrame);

            ((JButton) getPrivateField(panel, "backButton")).doClick();

            assertTrue(mainFrame.lastPanel instanceof AdminDashboardNew, "Back must return to AdminDashboardNew");
        }
    }

    private void testInsuranceNetworkBackButtonReturnsToDashboard() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            InsuranceNetworkManagementPanel panel = fixture.newInsurancePanel(mainFrame);

            ((JButton) getPrivateField(panel, "backButton")).doClick();

            assertTrue(mainFrame.lastPanel instanceof AdminDashboardNew, "Back must return to AdminDashboardNew");
        }
    }

    private void testDoctorManagementBackButtonReturnsToDashboard() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            DoctorManagementPanel panel = fixture.newDoctorPanel(mainFrame);

            ((JButton) getPrivateField(panel, "backButton")).doClick();

            assertTrue(mainFrame.lastPanel instanceof AdminDashboardNew, "Back must return to AdminDashboardNew");
        }
    }

    private void testAppointmentManagementBackButtonReturnsToDashboard() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            AppointmentManagementPanel panel = fixture.newAppointmentPanel(mainFrame);

            ((JButton) getPrivateField(panel, "backButton")).doClick();

            assertTrue(mainFrame.lastPanel instanceof AdminDashboardNew, "Back must return to AdminDashboardNew");
        }
    }

    private void testMedicalRequestManagementBackButtonReturnsToDashboard() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            MedicalRequestManagementPanel panel = fixture.newRequestPanel(mainFrame);

            ((JButton) getPrivateField(panel, "backBtn")).doClick();

            assertTrue(mainFrame.lastPanel instanceof AdminDashboardNew, "Back must return to AdminDashboardNew");
        }
    }

    /**
     * The shared EditProfileNew screen is opened with onBack = () -&gt; mainFrame.showPanel(this) -
     * the same idiom every other role dashboard uses - so its Back button must return to the exact
     * dashboard instance it was opened from, not a freshly constructed one.
     */
    private void testEditProfileBackButtonReturnsToSameDashboardInstance() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            AdminDashboardNew dashboard = fixture.newDashboard(mainFrame);

            ((JButton) getPrivateField(dashboard, "btnEditProfile")).doClick();
            EditProfileNew profilePanel = (EditProfileNew) mainFrame.lastPanel;

            ((JButton) getPrivateField(profilePanel, "btnBackDashboard")).doClick();

            assertTrue(mainFrame.lastPanel == dashboard,
                    "Back from the shared Edit Profile screen must return to the same AdminDashboardNew instance");
        }
    }

    private void testBackNavigationPreservesLoggedInSession() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            UserManagementPanel panel = fixture.newUserPanel(mainFrame);

            ((JButton) getPrivateField(panel, "backButton")).doClick();

            AdminDashboardNew dashboard = (AdminDashboardNew) mainFrame.lastPanel;
            Object wiredSessionManager = getPrivateField(dashboard, "sessionManager");
            assertTrue(wiredSessionManager == fixture.sessionManager,
                    "Back navigation must reuse the same SessionManager, not a new one");
            assertTrue(fixture.sessionManager.getCurrentUser().getUserId().equals(fixture.admin.getUserId()),
                    "the logged-in Admin must still be the current user after returning to the dashboard");
        }
    }

    // ---- tests: logout ----

    private void testLogoutButtonLogsOutAndRunsSuppliedCallback() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            boolean[] callbackRan = {false};
            AdminDashboardNew dashboard = fixture.newDashboard(mainFrame, () -> callbackRan[0] = true);

            ((JButton) getPrivateField(dashboard, "btnLogout")).doClick();

            assertTrue(callbackRan[0], "Logout must run the onLogout callback supplied to the dashboard");
            assertTrue(fixture.sessionManager.getCurrentUser() == null,
                    "Logout must clear the session's current user");
        }
    }

    // ---- tests: table and state behavior ----

    private void testAllAdminTablesAreNonEditable() throws Exception {
        try (Fixture fixture = new Fixture()) {
            RecordingMainFrame mainFrame = new RecordingMainFrame();

            assertTableNotEditable((JTable) getPrivateField(fixture.newUserPanel(mainFrame), "userTable"), "user table");
            assertTableNotEditable((JTable) getPrivateField(fixture.newAssetPanel(mainFrame), "assetTable"), "asset table");
            assertTableNotEditable((JTable) getPrivateField(fixture.newRatePanel(mainFrame), "rateTable"), "rate table");
            assertTableNotEditable((JTable) getPrivateField(fixture.newInsurancePanel(mainFrame), "networkTable"),
                    "network table");
            assertTableNotEditable((JTable) getPrivateField(fixture.newDoctorPanel(mainFrame), "doctorTable"), "doctor table");
            assertTableNotEditable((JTable) getPrivateField(fixture.newAppointmentPanel(mainFrame), "appointmentTable"),
                    "appointment table");
            assertTableNotEditable((JTable) getPrivateField(fixture.newRequestPanel(mainFrame), "requestsTbl"),
                    "medical request table");
        }
    }

    /**
     * End-to-end: a real button click flips operational availability and refreshes the table,
     * proving the click used the asset's ID (read from the selected row's ID column) rather than
     * the row position, and that the table reflects the change afterward.
     */
    private void testToggleAvailabilityRefreshesAssetTableUsingAssetId() throws Exception {
        try (Fixture fixture = new Fixture()) {
            fixture.hospitalAssetManager.createAsset("Lab Room 1", "LAB", "Ground Floor", true, fixture.admin.getStaffId());
            RecordingMainFrame mainFrame = new RecordingMainFrame();
            HospitalAssetManagementPanel panel = fixture.newAssetPanel(mainFrame);

            JTable assetTable = (JTable) getPrivateField(panel, "assetTable");
            assetTable.setRowSelectionInterval(0, 0);
            String selectedAssetId = (String) assetTable.getModel().getValueAt(0, 0);

            ((JButton) getPrivateField(panel, "toggleButton")).doClick();

            assertTrue(!fixture.hospitalAssetManager.findById(selectedAssetId).isAvailability(),
                    "clicking Toggle Availability must flip the selected asset's availability");
            assertTrue("No".equals(assetTable.getModel().getValueAt(0, 4)),
                    "the table must refresh to show the new availability after the toggle");
        }
    }

    /**
     * The shared EditProfileNew screen now calls UserManager.updateOwnProfile() - one atomic
     * operation - instead of the old two-step updateProfile()+changePassword() sequence. This
     * proves that same atomic method surfaces a genuine persistence failure as IllegalStateException
     * (not IOException, and not silently swallowed), which is the exact type EditProfileNew's
     * saveProfile() catch clause accepts.
     *
     * This test cannot safely call saveProfile() itself: both its success and failure paths end in
     * JOptionPane.showMessageDialog(), which would open a real, un-automatable modal window on this
     * non-headless machine and hang the test run - exactly what this project's tests must avoid.
     * Holding the users file open (the same technique as the Step 8 persistence tests) forces that
     * failure without touching any real data.
     */
    private void testUpdateOwnProfileFailureThrowsTheExceptionTypeEditProfileNewCatches() throws Exception {
        try (Fixture fixture = new Fixture()) {
            try (FileOutputStream lock = new FileOutputStream(fixture.usersFile, true)) {
                try {
                    fixture.userManager.updateOwnProfile(fixture.admin.getUserId(), "0199999999", "changed@test.com",
                            "", "", "");
                    throw new AssertionError("test setup: the locked write should have failed");
                } catch (IllegalStateException expected) {
                    // This is the exact exception type EditProfileNew.saveProfile()'s
                    // catch clause must accept instead of letting it escape.
                }
            }
        }
    }
}
