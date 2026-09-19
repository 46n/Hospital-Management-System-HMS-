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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Admin screen for doctor management: view, search, assign/reassign a
 * Medical Manager, and update specialization. Appointment booking, doctor
 * schedules, and patient features are handled elsewhere - this panel only
 * prepares the doctor information those workflows will later depend on.
 * All rules (existence checks, role checks, consistent manager linkage)
 * live in UserManager; this panel only collects input and displays results.
 *
 * Visual structure is managed by the NetBeans GUI Builder (see
 * DoctorManagementPanel.form). Only initComponents() is generated/guarded;
 * the table model, search, dialogs, and navigation stay hand-written.
 */
public class DoctorManagementPanel extends javax.swing.JPanel {

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
        initComponents();
        customizeComponents();
        refreshTable(getAllDoctors());
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        searchPanel = new javax.swing.JPanel();
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        tableWrapperPanel = new javax.swing.JPanel();
        doctorScrollPane = new javax.swing.JScrollPane();
        doctorTable = new javax.swing.JTable();
        buttonPanel = new javax.swing.JPanel();
        viewButton = new javax.swing.JButton();
        assignManagerButton = new javax.swing.JButton();
        unassignManagerButton = new javax.swing.JButton();
        updateSpecialtyButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        topPanel.setLayout(new java.awt.BorderLayout());

        titleLabel.setText("Doctor Management");
        topPanel.add(titleLabel, java.awt.BorderLayout.WEST);

        searchLabel.setText("Search (username or specialty):");
        searchPanel.add(searchLabel);

        searchField.setColumns(16);
        searchPanel.add(searchField);

        searchButton.setText("Search");
        searchPanel.add(searchButton);

        clearButton.setText("Clear");
        searchPanel.add(clearButton);

        topPanel.add(searchPanel, java.awt.BorderLayout.EAST);

        add(topPanel, java.awt.BorderLayout.NORTH);

        tableWrapperPanel.setLayout(new java.awt.BorderLayout());

        doctorTable.setRowHeight(24);
        doctorScrollPane.setViewportView(doctorTable);
        tableWrapperPanel.add(doctorScrollPane, java.awt.BorderLayout.CENTER);

        add(tableWrapperPanel, java.awt.BorderLayout.CENTER);

        buttonPanel.setLayout(new java.awt.GridLayout(1, 5, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));

        viewButton.setText("View Details");
        buttonPanel.add(viewButton);

        assignManagerButton.setText("Assign / Reassign Manager");
        buttonPanel.add(assignManagerButton);

        unassignManagerButton.setText("Unassign Manager");
        buttonPanel.add(unassignManagerButton);

        updateSpecialtyButton.setText("Update Specialization");
        buttonPanel.add(updateSpecialtyButton);

        backButton.setText("Back to Dashboard");
        buttonPanel.add(backButton);

        add(buttonPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    /** Applies HMS theming, builds the table model, and wires listeners. */
    private void customizeComponents() {
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        topPanel.setBackground(Theme.BACKGROUND_COLOR);
        UIStyles.applyHeadingStyle(titleLabel);

        searchPanel.setBackground(Theme.BACKGROUND_COLOR);
        UIStyles.applyPrimaryButtonStyle(searchButton);
        UIStyles.applyPrimaryButtonStyle(clearButton);
        searchButton.addActionListener(e -> applySearch());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            refreshTable(getAllDoctors());
        });

        tableWrapperPanel.setBackground(Theme.BACKGROUND_COLOR);
        tableWrapperPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(
                Theme.SPACING_MEDIUM, 0, Theme.SPACING_MEDIUM, 0));

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        doctorTable.setModel(tableModel);
        doctorTable.setFont(Theme.DEFAULT_FONT);
        doctorTable.getTableHeader().setFont(Theme.DEFAULT_FONT);

        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);
        for (javax.swing.JButton button : new javax.swing.JButton[]{viewButton, assignManagerButton,
                unassignManagerButton, updateSpecialtyButton, backButton}) {
            UIStyles.applyPrimaryButtonStyle(button);
        }

        viewButton.addActionListener(e -> viewSelectedDoctor());
        assignManagerButton.addActionListener(e -> openAssignManagerDialog());
        unassignManagerButton.addActionListener(e -> unassignSelectedManager());
        updateSpecialtyButton.addActionListener(e -> openUpdateSpecialtyDialog());
        backButton.addActionListener(e -> mainFrame.showPanel(
                new AdminDashboardPanel(sessionManager, userManager, appointmentManager, assetManager,
                        rateManager, insuranceManager, mainFrame, onLogout)));
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel topPanel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JTextField searchField;
    private javax.swing.JButton searchButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JPanel tableWrapperPanel;
    private javax.swing.JScrollPane doctorScrollPane;
    private javax.swing.JTable doctorTable;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton viewButton;
    private javax.swing.JButton assignManagerButton;
    private javax.swing.JButton unassignManagerButton;
    private javax.swing.JButton updateSpecialtyButton;
    private javax.swing.JButton backButton;
    // End of variables declaration//GEN-END:variables
}
