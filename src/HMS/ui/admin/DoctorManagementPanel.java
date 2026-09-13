package hms.ui.admin;

import hms.manager.AppointmentManager;
import hms.manager.ConsultationRateManager;
import hms.manager.HospitalAssetManager;
import hms.manager.InsuranceNetworkManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.Doctor;
import hms.model.MedicalManager;
import hms.model.User;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Admin screen for doctor management: view, search, assign/reassign a
 * Medical Manager, and update specialization. Appointment booking, doctor
 * schedules, and patient features are handled elsewhere - this panel only
 * prepares the doctor information those workflows will later depend on.
 * All rules (existence checks, role checks, consistent manager linkage)
 * live in UserManager; this panel only collects input and displays results.
 */
public class DoctorManagementPanel extends JPanel {

    private static final String[] COLUMN_NAMES =
            {"ID", "Username", "Specialty", "Assigned Manager", "Phone", "Email", "Active"};
    private static final String UNASSIGNED_LABEL = "-- Unassigned --";

    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final HospitalAssetManager assetManager;
    private final ConsultationRateManager rateManager;
    private final InsuranceNetworkManager insuranceManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private DefaultTableModel tableModel;
    private JTable doctorTable;
    private JTextField searchField;

    public DoctorManagementPanel(UserManager userManager, AppointmentManager appointmentManager,
            HospitalAssetManager assetManager, ConsultationRateManager rateManager,
            InsuranceNetworkManager insuranceManager, SessionManager sessionManager,
            MainFrame mainFrame, Runnable onLogout) {
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.assetManager = assetManager;
        this.rateManager = rateManager;
        this.insuranceManager = insuranceManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
        buildLayout();
        refreshTable(getAllDoctors());
    }

    private void buildLayout() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Theme.BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Doctor Management");
        UIStyles.applyHeadingStyle(titleLabel);
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(Theme.BACKGROUND_COLOR);
        searchField = new JTextField(16);
        JButton searchButton = new JButton("Search");
        UIStyles.applyPrimaryButtonStyle(searchButton);
        searchButton.addActionListener(e -> applySearch());
        JButton clearButton = new JButton("Clear");
        UIStyles.applyPrimaryButtonStyle(clearButton);
        clearButton.addActionListener(e -> {
            searchField.setText("");
            refreshTable(getAllDoctors());
        });
        searchPanel.add(new JLabel("Search (username or specialty):"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        topPanel.add(searchPanel, BorderLayout.EAST);

        return topPanel;
    }

    private JPanel createTablePanel() {
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        doctorTable = new JTable(tableModel);
        doctorTable.setRowHeight(24);
        doctorTable.setFont(Theme.DEFAULT_FONT);
        doctorTable.getTableHeader().setFont(Theme.DEFAULT_FONT);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BACKGROUND_COLOR);
        wrapper.setBorder(BorderFactory.createEmptyBorder(Theme.SPACING_MEDIUM, 0, Theme.SPACING_MEDIUM, 0));
        wrapper.add(new JScrollPane(doctorTable), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);

        JButton viewButton = new JButton("View Details");
        JButton assignManagerButton = new JButton("Assign / Reassign Manager");
        JButton unassignManagerButton = new JButton("Unassign Manager");
        JButton updateSpecialtyButton = new JButton("Update Specialization");
        JButton backButton = new JButton("Back to Dashboard");

        for (JButton button : new JButton[]{viewButton, assignManagerButton, unassignManagerButton,
                updateSpecialtyButton, backButton}) {
            UIStyles.applyPrimaryButtonStyle(button);
        }

        viewButton.addActionListener(e -> viewSelectedDoctor());
        assignManagerButton.addActionListener(e -> openAssignManagerDialog());
        unassignManagerButton.addActionListener(e -> unassignSelectedManager());
        updateSpecialtyButton.addActionListener(e -> openUpdateSpecialtyDialog());
        backButton.addActionListener(e -> mainFrame.showPanel(
                new AdminDashboardPanel(sessionManager, userManager, appointmentManager, assetManager,
                        rateManager, insuranceManager, mainFrame, onLogout)));

        buttonPanel.add(viewButton);
        buttonPanel.add(assignManagerButton);
        buttonPanel.add(unassignManagerButton);
        buttonPanel.add(updateSpecialtyButton);
        buttonPanel.add(backButton);
        return buttonPanel;
    }

    private List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        for (User user : userManager.getUsers()) {
            if (user instanceof Doctor) {
                doctors.add((Doctor) user);
            }
        }
        return doctors;
    }

    private List<MedicalManager> getAllMedicalManagers() {
        List<MedicalManager> managers = new ArrayList<>();
        for (User user : userManager.getUsers()) {
            if (user instanceof MedicalManager) {
                managers.add((MedicalManager) user);
            }
        }
        return managers;
    }

    private void applySearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            refreshTable(getAllDoctors());
            return;
        }
        List<Doctor> matches = new ArrayList<>();
        for (Doctor doctor : getAllDoctors()) {
            String specialty = doctor.getSpecialty() == null ? "" : doctor.getSpecialty().toLowerCase();
            if (doctor.getUsername().toLowerCase().contains(query) || specialty.contains(query)) {
                matches.add(doctor);
            }
        }
        refreshTable(matches);
    }

    private void refreshTable(List<Doctor> doctors) {
        tableModel.setRowCount(0);
        for (Doctor doctor : doctors) {
            tableModel.addRow(new Object[]{
                    doctor.getDoctorId(),
                    doctor.getUsername(),
                    doctor.getSpecialty(),
                    managerLabel(doctor),
                    doctor.getPhone(),
                    doctor.getEmail(),
                    doctor.isActive() ? "Yes" : "No"
            });
        }
    }

    private String managerLabel(Doctor doctor) {
        MedicalManager manager = doctor.getShiftManager();
        return manager == null ? UNASSIGNED_LABEL : manager.getUsername() + " (" + manager.getManagerId() + ")";
    }

    private String getSelectedDoctorId() {
        int row = doctorTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return (String) tableModel.getValueAt(row, 0);
    }

    private String requireSelection() {
        String doctorId = getSelectedDoctorId();
        if (doctorId == null) {
            showError("Select a doctor first.");
        }
        return doctorId;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void viewSelectedDoctor() {
        String doctorId = requireSelection();
        if (doctorId == null) {
            return;
        }
        Doctor doctor = userManager.findDoctorById(doctorId);
        if (doctor == null) {
            showError("Doctor not found: " + doctorId);
            return;
        }
        String details = "Doctor ID: " + doctor.getDoctorId()
                + "\nUsername: " + doctor.getUsername()
                + "\nSpecialty: " + doctor.getSpecialty()
                + "\nAssigned Manager: " + managerLabel(doctor)
                + "\nPhone: " + doctor.getPhone()
                + "\nEmail: " + doctor.getEmail()
                + "\nActive: " + (doctor.isActive() ? "Yes" : "No");
        JOptionPane.showMessageDialog(this, details, "Doctor Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openAssignManagerDialog() {
        String doctorId = requireSelection();
        if (doctorId == null) {
            return;
        }
        Doctor doctor = userManager.findDoctorById(doctorId);
        List<MedicalManager> managers = getAllMedicalManagers();
        if (managers.isEmpty()) {
            showError("No Medical Managers are registered yet.");
            return;
        }

        String[] options = new String[managers.size() + 1];
        options[0] = UNASSIGNED_LABEL;
        for (int i = 0; i < managers.size(); i++) {
            options[i + 1] = managers.get(i).getUsername() + " (" + managers.get(i).getManagerId() + ")";
        }
        Object selection = JOptionPane.showInputDialog(this,
                "Assign Medical Manager for " + doctor.getUsername() + ":",
                "Assign Medical Manager", JOptionPane.PLAIN_MESSAGE, null, options, managerLabel(doctor));
        if (selection == null) {
            return;
        }
        try {
            if (UNASSIGNED_LABEL.equals(selection)) {
                userManager.assignDoctorToManager(doctorId, null);
            } else {
                int index = Arrays.asList(options).indexOf(selection) - 1;
                userManager.assignDoctorToManager(doctorId, managers.get(index).getManagerId());
            }
            refreshTable(getAllDoctors());
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void openUpdateSpecialtyDialog() {
        String doctorId = requireSelection();
        if (doctorId == null) {
            return;
        }
        Doctor doctor = userManager.findDoctorById(doctorId);
        String[] specialties = UserManager.getValidSpecialties().toArray(new String[0]);
        Object selection = JOptionPane.showInputDialog(this,
                "Specialization for " + doctor.getUsername() + ":",
                "Update Specialization", JOptionPane.PLAIN_MESSAGE, null, specialties, doctor.getSpecialty());
        if (selection == null) {
            return;
        }
        try {
            userManager.updateDoctorSpecialty(doctorId, (String) selection);
            refreshTable(getAllDoctors());
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void unassignSelectedManager() {
        String doctorId = requireSelection();
        if (doctorId == null) {
            return;
        }
        Doctor doctor = userManager.findDoctorById(doctorId);
        if (doctor.getShiftManager() == null) {
            showError("This doctor has no assigned manager.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Unassign " + doctor.getUsername() + " from " + doctor.getShiftManager().getUsername() + "?",
                "Confirm Unassign", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            userManager.assignDoctorToManager(doctorId, null);
            refreshTable(getAllDoctors());
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }
}
