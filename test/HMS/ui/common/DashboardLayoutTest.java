package hms.ui.common;

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
import hms.model.Doctor;
import hms.model.MedicalManager;
import hms.model.Patient;
import hms.ui.admin.AdminDashboardNew;
import hms.ui.medicalmanager.MedicalManagerDashboardNew;
import hms.ui.patient.PatientDashboardNew;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Headless layout regression test for the four role dashboards and the
 * shared Edit Profile screen. Confirms the dashboard-layout fix: at the
 * actual size MainFrame's CardLayout gives a dashboard panel, every action
 * control stays within bounds, the action area is reasonably centered
 * rather than pinned to the upper-left or spread across the window, every
 * primary action button on a dashboard shares one common size, Logout sits
 * close beneath the last action row instead of at the window's bottom edge,
 * no two controls overlap, and every button still has its navigation
 * listener attached. Also confirms the Edit Profile screen's content is
 * centered and its panels share one consistent white background.
 *
 * Same hand-rolled runner style as the rest of this project's tests (no
 * JUnit on the classpath, no modal dialogs triggered - only setSize()/
 * doLayout() are called, never doClick()).
 *
 * Run with (from the project root, after compiling src to build/classes):
 *   javac -d build/test-classes -cp build/classes test/HMS/ui/common/DashboardLayoutTest.java
 *   java -cp build/classes;build/test-classes hms.ui.common.DashboardLayoutTest
 */
public class DashboardLayoutTest {

    /** The actual usable area a dashboard panel receives inside MainFrame's CardLayout content pane. */
    private static final int CONTENT_WIDTH = Theme.WINDOW_WIDTH - (2 * Theme.SPACING_LARGE);
    private static final int CONTENT_HEIGHT = Theme.WINDOW_HEIGHT - (2 * Theme.SPACING_LARGE);

    /** How far below the last action row's bottom edge Logout may sit and still count as "close". */
    private static final int MAX_GAP_BEFORE_LOGOUT = 60;

    /** How far the action block's center may drift from the container's true center and still count as "centered". */
    private static final int CENTERING_TOLERANCE = 80;

    private static final Color WHITE = new Color(255, 255, 255);

    private int passed = 0;
    private int failed = 0;

    public static void main(String[] args) throws Exception {
        DashboardLayoutTest suite = new DashboardLayoutTest();

        suite.runAdminDashboardTest();
        suite.runDoctorDashboardTest();
        suite.runMedicalManagerDashboardTest();
        suite.runPatientDashboardTest();
        suite.runEditProfileNewTest();

        System.out.println();
        System.out.println(suite.passed + " passed, " + suite.failed + " failed");
        if (suite.failed > 0) {
            System.exit(1);
        }
    }

    private void runAdminDashboardTest() {
        String name = "testAdminDashboardLayoutIsSaneAtMainFrameContentSize";
        try {
            testAdminDashboardLayoutIsSaneAtMainFrameContentSize();
            System.out.println("PASS  " + name);
            passed++;
        } catch (Throwable t) {
            System.out.println("FAIL  " + name + " - " + t.getMessage());
            failed++;
        }
    }

    private void runDoctorDashboardTest() {
        String name = "testDoctorDashboardLayoutIsSaneAtMainFrameContentSize";
        try {
            testDoctorDashboardLayoutIsSaneAtMainFrameContentSize();
            System.out.println("PASS  " + name);
            passed++;
        } catch (Throwable t) {
            System.out.println("FAIL  " + name + " - " + t.getMessage());
            failed++;
        }
    }

    private void runMedicalManagerDashboardTest() {
        String name = "testMedicalManagerDashboardLayoutIsSaneAtMainFrameContentSize";
        try {
            testMedicalManagerDashboardLayoutIsSaneAtMainFrameContentSize();
            System.out.println("PASS  " + name);
            passed++;
        } catch (Throwable t) {
            System.out.println("FAIL  " + name + " - " + t.getMessage());
            failed++;
        }
    }

    private void runPatientDashboardTest() {
        String name = "testPatientDashboardStaysCompactAndCenteredWithinItsWrapper";
        try {
            testPatientDashboardStaysCompactAndCenteredWithinItsWrapper();
            System.out.println("PASS  " + name);
            passed++;
        } catch (Throwable t) {
            System.out.println("FAIL  " + name + " - " + t.getMessage());
            failed++;
        }
    }

    private void runEditProfileNewTest() {
        String name = "testEditProfileNewContentIsCenteredAndBackgroundsAreWhite";
        try {
            testEditProfileNewContentIsCenteredAndBackgroundsAreWhite();
            System.out.println("PASS  " + name);
            passed++;
        } catch (Throwable t) {
            System.out.println("FAIL  " + name + " - " + t.getMessage());
            failed++;
        }
    }

    // ---- a small manager graph, wired in Main.java's dependency order ----

    private static final class Environment implements AutoCloseable {
        final Path tempDir;
        final UserManager userManager;
        final DoctorShiftManager shiftManager;
        final AppointmentManager appointmentManager;
        final AssessmentTypeManager typeManager;
        final ClinicalManager clinicalManager;
        final BillingManager billingManager;
        final ReportManager reportManager;
        final HospitalAssetManager assetManager;
        final ConsultationRateManager rateManager;
        final InsuranceNetworkManager insuranceManager;
        final ClinicalDepartmentManager departmentManager;
        final AssetAllocationManager allocationManager;
        final SessionManager sessionManager;

        Environment() throws IOException {
            tempDir = Files.createTempDirectory("hms-dashboard-layout-test-");
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

        @Override
        public void close() throws IOException {
            deleteRecursively(tempDir);
        }
    }

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

    private static Object getPrivateField(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    // =====================================================================================
    // Admin
    // =====================================================================================

    private void testAdminDashboardLayoutIsSaneAtMainFrameContentSize() throws Exception {
        try (Environment env = new Environment()) {
            AdminStaff admin = (AdminStaff) env.userManager.registerUser(
                    new AdminStaff(null, "admin1", "pass1234", "0111111111", "admin1@test.com", "ADMIN_STAFF", null));
            env.sessionManager.login("admin1", "pass1234");

            Runnable onLogout = new Runnable() {
                @Override
                public void run() {
                }
            };

            AdminDashboardNew dashboard = new AdminDashboardNew(env.sessionManager, env.userManager,
                    env.appointmentManager, env.clinicalManager, env.assetManager, env.rateManager,
                    env.insuranceManager, env.allocationManager, null, onLogout);

            JButton[] actionButtons = {
                (JButton) getPrivateField(dashboard, "btnEditProfile"),
                (JButton) getPrivateField(dashboard, "btnManageUsers"),
                (JButton) getPrivateField(dashboard, "btnManageDoctors"),
                (JButton) getPrivateField(dashboard, "btnViewAppointments"),
                (JButton) getPrivateField(dashboard, "btnMedicalRequests"),
                (JButton) getPrivateField(dashboard, "btnManageAssets"),
                (JButton) getPrivateField(dashboard, "btnManageRates"),
                (JButton) getPrivateField(dashboard, "btnManageInsurance")
            };
            JButton logout = (JButton) getPrivateField(dashboard, "btnLogout");
            JLabel welcome = (JLabel) getPrivateField(dashboard, "lblWelcome");

            assertDashboardLayoutIsSane(dashboard, CONTENT_WIDTH, CONTENT_HEIGHT, welcome, actionButtons, logout, "Admin");
        }
    }

    // =====================================================================================
    // Doctor
    // =====================================================================================

    private void testDoctorDashboardLayoutIsSaneAtMainFrameContentSize() throws Exception {
        try (Environment env = new Environment()) {
            Doctor doctor = (Doctor) env.userManager.registerUser(
                    new Doctor(null, "doctor1", "pass1234", "0133333333", "doctor1@test.com", "DOCTOR", null, "Cardiology"));
            env.sessionManager.login("doctor1", "pass1234");

            // RequestLabImagingNew's own package "HMS.ui.doctor" collides, on a case-insensitive
            // filesystem, with the directory javac uses for "hms.ui.doctor" - DoctorDashboardNew
            // lives in that same pre-existing, oddly-cased package, so it is constructed via
            // reflection here for the same reason the project's other tests already do.
            Class<?> dashboardClass = Class.forName("HMS.ui.doctor.DoctorDashboardNew");
            Constructor<?> constructor = dashboardClass.getConstructor(SessionManager.class, UserManager.class,
                    AppointmentManager.class, ClinicalManager.class, AssessmentTypeManager.class, MainFrame.class,
                    Runnable.class);

            Runnable onLogout = new Runnable() {
                @Override
                public void run() {
                }
            };

            Object dashboard = constructor.newInstance(env.sessionManager, env.userManager, env.appointmentManager,
                    env.clinicalManager, env.typeManager, null, onLogout);

            JButton[] actionButtons = {
                (JButton) getPrivateField(dashboard, "btnEditMyProfile"),
                (JButton) getPrivateField(dashboard, "btnViewMyAppointments"),
                (JButton) getPrivateField(dashboard, "btnLogVitalsConsultationNotes"),
                (JButton) getPrivateField(dashboard, "btnRecordAssessmentResult"),
                (JButton) getPrivateField(dashboard, "btnRequestLabImaging"),
                (JButton) getPrivateField(dashboard, "btnIssuePrescriptions")
            };
            JButton logout = (JButton) getPrivateField(dashboard, "btnLogOut");
            JLabel welcome = (JLabel) getPrivateField(dashboard, "jLabel1");

            assertDashboardLayoutIsSane((JPanel) dashboard, CONTENT_WIDTH, CONTENT_HEIGHT, welcome, actionButtons,
                    logout, "Doctor");
        }
    }

    // =====================================================================================
    // Medical Manager
    // =====================================================================================

    private void testMedicalManagerDashboardLayoutIsSaneAtMainFrameContentSize() throws Exception {
        try (Environment env = new Environment()) {
            MedicalManager manager = (MedicalManager) env.userManager.registerUser(
                    new MedicalManager(null, "manager1", "pass1234", "0122222222", "manager1@test.com",
                            "MEDICAL_MANAGER", null));
            env.sessionManager.login("manager1", "pass1234");

            Runnable onLogout = new Runnable() {
                @Override
                public void run() {
                }
            };

            MedicalManagerDashboardNew dashboard = new MedicalManagerDashboardNew(env.sessionManager, env.userManager,
                    env.appointmentManager, env.billingManager, env.reportManager, env.typeManager, env.shiftManager,
                    env.departmentManager, env.clinicalManager, null, onLogout);

            JButton[] actionButtons = {
                (JButton) getPrivateField(dashboard, "btnDepartments"),
                (JButton) getPrivateField(dashboard, "btnAssessmentTypes"),
                (JButton) getPrivateField(dashboard, "btnDoctorShifts"),
                (JButton) getPrivateField(dashboard, "btnReports1"),
                (JButton) getPrivateField(dashboard, "btnProfile"),
                (JButton) getPrivateField(dashboard, "btnWorkloadAlerts")
            };
            JButton logout = (JButton) getPrivateField(dashboard, "btnLogout");
            JLabel title = (JLabel) getPrivateField(dashboard, "lblPageTitle");

            assertDashboardLayoutIsSane(dashboard, CONTENT_WIDTH, CONTENT_HEIGHT, title, actionButtons, logout,
                    "Medical Manager");
        }
    }

    // =====================================================================================
    // Patient - already centered via PatientController's GridBagLayout wrapper; this proves
    // that wrapper still centers the dashboard correctly at the real MainFrame content size,
    // and that the dashboard's own internal button layout stays sane at its own preferred size
    // (the only size it is ever actually given, since the wrapper never stretches it).
    // =====================================================================================

    private void testPatientDashboardStaysCompactAndCenteredWithinItsWrapper() throws Exception {
        try (Environment env = new Environment()) {
            Patient patient = (Patient) env.userManager.registerUser(
                    new Patient(null, "patient1", "pass1234", "0144444444", "patient1@test.com", "PATIENT", null,
                            hms.util.ValidationUtil.parseStrictDate("1990-01-01")));
            env.sessionManager.login("patient1", "pass1234");

            Runnable onLogout = new Runnable() {
                @Override
                public void run() {
                }
            };

            PatientDashboardNew dashboard = new PatientDashboardNew(env.sessionManager, env.userManager,
                    env.appointmentManager, env.shiftManager, env.clinicalManager, env.billingManager, null,
                    onLogout);

            // The same centering wrapper PatientController.showPatientPanel() and Main.java both use.
            JPanel wrapper = new JPanel(new GridBagLayout());
            wrapper.add(dashboard);
            wrapper.setSize(CONTENT_WIDTH, CONTENT_HEIGHT);
            wrapper.doLayout();
            // wrapper.doLayout() only positions its direct child (the dashboard itself) - the
            // dashboard's own GroupLayout still needs its own doLayout() call to position the
            // buttons inside it, exactly as Swing would do once this ever became visible.
            dashboard.doLayout();

            Rectangle dashboardBounds = dashboard.getBounds();
            assertTrue(dashboardBounds.width > 0 && dashboardBounds.height > 0,
                    "the Patient dashboard must still be given a real size inside its wrapper");
            assertTrue(dashboardBounds.width <= dashboard.getPreferredSize().width + 5,
                    "the wrapper must not stretch the Patient dashboard wider than its own preferred size");

            int dashboardCenterX = dashboardBounds.x + (dashboardBounds.width / 2);
            int dashboardCenterY = dashboardBounds.y + (dashboardBounds.height / 2);
            assertTrue(Math.abs(dashboardCenterX - (CONTENT_WIDTH / 2)) <= CENTERING_TOLERANCE,
                    "the Patient dashboard must be horizontally centered within its wrapper");
            assertTrue(Math.abs(dashboardCenterY - (CONTENT_HEIGHT / 2)) <= CENTERING_TOLERANCE,
                    "the Patient dashboard must be vertically centered within its wrapper");

            // The dashboard's own internal button layout, at the size it is actually given.
            JButton[] actionButtons = {
                (JButton) getPrivateField(dashboard, "btnProfile"),
                (JButton) getPrivateField(dashboard, "btnAppointments"),
                (JButton) getPrivateField(dashboard, "btnMedicalHistory"),
                (JButton) getPrivateField(dashboard, "btnPrescriptionsBills"),
                (JButton) getPrivateField(dashboard, "btnFeedback")
            };
            JButton logout = (JButton) getPrivateField(dashboard, "btnLogout");

            assertNoOverlaps(actionButtons, logout, "Patient");
            assertWithinContainerBounds(dashboard, actionButtons, logout, "Patient");
            assertNavigationListenersConnected(actionButtons, logout, "Patient");
        }
    }

    // =====================================================================================
    // Edit Profile - the profile screen every role navigates to. Confirms its title+form
    // content block is centered within the real MainFrame content size, and that the outer
    // panel and the inner form panel share one consistent white background (no gray layers).
    // =====================================================================================

    private void testEditProfileNewContentIsCenteredAndBackgroundsAreWhite() throws Exception {
        try (Environment env = new Environment()) {
            AdminStaff admin = (AdminStaff) env.userManager.registerUser(
                    new AdminStaff(null, "admin2", "pass1234", "0155555555", "admin2@test.com", "ADMIN_STAFF", null));
            env.sessionManager.login("admin2", "pass1234");

            Runnable onBack = new Runnable() {
                @Override
                public void run() {
                }
            };

            EditProfileNew panel = new EditProfileNew(env.sessionManager, env.userManager, onBack);
            panel.setSize(CONTENT_WIDTH, CONTENT_HEIGHT);
            panel.doLayout();

            JLabel title = (JLabel) getPrivateField(panel, "lblTitle");
            JPanel form = (JPanel) getPrivateField(panel, "lblCurrentPassword");

            assertTrue(panel.getBackground().equals(WHITE),
                    "EditProfileNew's own background must be white");
            assertTrue(form.getBackground().equals(WHITE),
                    "EditProfileNew's form content panel must share the same white background");

            Rectangle titleBounds = title.getBounds();
            Rectangle formBounds = form.getBounds();

            assertTrue(new Rectangle(0, 0, CONTENT_WIDTH, CONTENT_HEIGHT).contains(titleBounds),
                    "the title must stay within the container's bounds");
            assertTrue(new Rectangle(0, 0, CONTENT_WIDTH, CONTENT_HEIGHT).contains(formBounds),
                    "the form content panel must stay within the container's bounds");
            assertTrue(!titleBounds.intersects(formBounds),
                    "the title must not overlap the form content panel");
            assertTrue(titleBounds.y < formBounds.y,
                    "the title must sit immediately above the form content");

            int left = Math.min(titleBounds.x, formBounds.x);
            int right = Math.max(titleBounds.x + titleBounds.width, formBounds.x + formBounds.width);
            int top = titleBounds.y;
            int bottom = formBounds.y + formBounds.height;
            int contentCenterX = (left + right) / 2;
            int contentCenterY = (top + bottom) / 2;

            assertTrue(Math.abs(contentCenterX - (CONTENT_WIDTH / 2)) <= CENTERING_TOLERANCE,
                    "the profile content must be horizontally centered (center was " + contentCenterX
                            + ", container width " + CONTENT_WIDTH + ")");
            assertTrue(Math.abs(contentCenterY - (CONTENT_HEIGHT / 2)) <= CENTERING_TOLERANCE,
                    "the profile content must be vertically centered (center was " + contentCenterY
                            + ", container height " + CONTENT_HEIGHT + ")");
        }
    }

    // =====================================================================================
    // Shared layout-sanity checks
    // =====================================================================================

    private static void assertDashboardLayoutIsSane(JPanel dashboard, int width, int height, JLabel welcomeLabel,
            JButton[] actionButtons, JButton logoutButton, String roleLabel) {
        dashboard.setSize(width, height);
        dashboard.doLayout();

        assertWithinContainerBounds(dashboard, actionButtons, logoutButton, roleLabel);
        assertNoOverlaps(actionButtons, logoutButton, roleLabel);
        assertNavigationListenersConnected(actionButtons, logoutButton, roleLabel);
        assertAllButtonsShareDimensions(actionButtons, roleLabel);

        // Welcome heading must sit above the action buttons, not below or overlapping them.
        int lowestButtonTop = Integer.MAX_VALUE;
        for (JButton button : actionButtons) {
            lowestButtonTop = Math.min(lowestButtonTop, button.getY());
        }
        assertTrue(welcomeLabel.getY() < lowestButtonTop,
                roleLabel + ": the welcome heading must be positioned above the action buttons");

        // Logout must sit directly below the last (lowest) action row, not pushed to the window bottom.
        int lastRowBottom = 0;
        for (JButton button : actionButtons) {
            lastRowBottom = Math.max(lastRowBottom, button.getY() + button.getHeight());
        }
        int gapBeforeLogout = logoutButton.getY() - lastRowBottom;
        assertTrue(gapBeforeLogout >= 0 && gapBeforeLogout <= MAX_GAP_BEFORE_LOGOUT,
                roleLabel + ": Logout must sit close beneath the last action row, not at the window bottom"
                        + " (gap was " + gapBeforeLogout + "px, container height " + height + "px,"
                        + " Logout y=" + logoutButton.getY() + ")");

        // The action area (buttons + Logout, excluding the heading) must be reasonably centered,
        // not pinned to the upper-left corner and not spread across the full window.
        int left = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int top = Integer.MAX_VALUE;
        int bottom = Integer.MIN_VALUE;
        for (JButton button : actionButtons) {
            left = Math.min(left, button.getX());
            right = Math.max(right, button.getX() + button.getWidth());
            top = Math.min(top, button.getY());
            bottom = Math.max(bottom, button.getY() + button.getHeight());
        }
        left = Math.min(left, logoutButton.getX());
        right = Math.max(right, logoutButton.getX() + logoutButton.getWidth());
        top = Math.min(top, logoutButton.getY());
        bottom = Math.max(bottom, logoutButton.getY() + logoutButton.getHeight());

        int actionAreaCenterX = (left + right) / 2;
        int actionAreaCenterY = (top + bottom) / 2;
        assertTrue(Math.abs(actionAreaCenterX - (width / 2)) <= CENTERING_TOLERANCE,
                roleLabel + ": the action area must be horizontally centered (center was " + actionAreaCenterX
                        + ", container width " + width + ")");
        assertTrue(Math.abs(actionAreaCenterY - (height / 2)) <= CENTERING_TOLERANCE,
                roleLabel + ": the action area must be vertically centered (center was " + actionAreaCenterY
                        + ", container height " + height + ")");
        assertTrue((right - left) < width - (2 * CENTERING_TOLERANCE),
                roleLabel + ": the action area must form a compact block, not spread across the full window width");
    }

    /** Every primary action button on a dashboard must share one common width and height. */
    private static void assertAllButtonsShareDimensions(JButton[] actionButtons, String roleLabel) {
        int width = actionButtons[0].getWidth();
        int height = actionButtons[0].getHeight();
        for (JButton button : actionButtons) {
            assertTrue(button.getWidth() == width,
                    roleLabel + ": " + button.getText() + " must share the same width as the other action buttons"
                            + " (was " + button.getWidth() + "px, expected " + width + "px)");
            assertTrue(button.getHeight() == height,
                    roleLabel + ": " + button.getText() + " must share the same height as the other action buttons"
                            + " (was " + button.getHeight() + "px, expected " + height + "px)");
        }
    }

    private static void assertWithinContainerBounds(JPanel container, JButton[] actionButtons, JButton logoutButton,
            String roleLabel) {
        Rectangle containerBounds = new Rectangle(0, 0, container.getWidth(), container.getHeight());
        for (JButton button : actionButtons) {
            assertTrue(containerBounds.contains(button.getBounds()),
                    roleLabel + ": " + button.getText() + " must stay within the container's bounds ("
                            + button.getBounds() + " not inside " + containerBounds + ")");
        }
        assertTrue(containerBounds.contains(logoutButton.getBounds()),
                roleLabel + ": " + logoutButton.getText() + " must stay within the container's bounds ("
                        + logoutButton.getBounds() + " not inside " + containerBounds + ")");
    }

    private static void assertNoOverlaps(JButton[] actionButtons, JButton logoutButton, String roleLabel) {
        for (int i = 0; i < actionButtons.length; i++) {
            for (int j = i + 1; j < actionButtons.length; j++) {
                Rectangle a = actionButtons[i].getBounds();
                Rectangle b = actionButtons[j].getBounds();
                assertTrue(!a.intersects(b),
                        roleLabel + ": " + actionButtons[i].getText() + " must not overlap "
                                + actionButtons[j].getText() + " (" + a + " vs " + b + ")");
            }
            assertTrue(!actionButtons[i].getBounds().intersects(logoutButton.getBounds()),
                    roleLabel + ": " + actionButtons[i].getText() + " must not overlap " + logoutButton.getText());
        }
    }

    private static void assertNavigationListenersConnected(JButton[] actionButtons, JButton logoutButton,
            String roleLabel) {
        for (JButton button : actionButtons) {
            assertTrue(button.getActionListeners().length > 0,
                    roleLabel + ": " + button.getText() + " must still have its navigation listener attached");
        }
        assertTrue(logoutButton.getActionListeners().length > 0,
                roleLabel + ": " + logoutButton.getText() + " must still have its navigation listener attached");
    }

}
