package hms.ui.admin;

import hms.manager.AppointmentManager;
import hms.manager.AssetAllocationManager;
import hms.manager.ClinicalManager;
import hms.manager.ConsultationRateManager;
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
import hms.model.User;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;
import hms.util.ValidationUtil;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Admin screen for user management: view, register, edit, deactivate,
 * reactivate, delete, and search users of all four roles. All business
 * rules (duplicate usernames, required fields, role validity, active-state
 * persistence) live in UserManager - this panel only collects input, calls
 * it, and displays the result.
 *
 * Visual structure is managed by the NetBeans GUI Builder (see
 * UserManagementPanel.form). Only initComponents() is generated/guarded;
 * the table model, dialogs, and navigation stay hand-written.
 */
public class UserManagementPanel extends javax.swing.JPanel {

    private static final String[] ROLE_OPTIONS = {"Admin Staff", "Medical Manager", "Doctor", "Patient"};
    private static final String[] COLUMN_NAMES = {"ID", "Role", "Username", "Phone", "Email", "Active"};
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final ClinicalManager clinicalManager;
    private final HospitalAssetManager assetManager;
    private final ConsultationRateManager rateManager;
    private final InsuranceNetworkManager insuranceManager;
    private final AssetAllocationManager allocationManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private DefaultTableModel tableModel;

    public UserManagementPanel(UserManager userManager, AppointmentManager appointmentManager,
            ClinicalManager clinicalManager, HospitalAssetManager assetManager, ConsultationRateManager rateManager,
            InsuranceNetworkManager insuranceManager, AssetAllocationManager allocationManager,
            SessionManager sessionManager, MainFrame mainFrame, Runnable onLogout) {
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.clinicalManager = clinicalManager;
        this.assetManager = assetManager;
        this.rateManager = rateManager;
        this.insuranceManager = insuranceManager;
        this.allocationManager = allocationManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
        initComponents();
        initializeBehavior();
        refreshTable(userManager.getUsers());
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
        userScrollPane = new javax.swing.JScrollPane();
        userTable = new javax.swing.JTable();
        buttonPanel = new javax.swing.JPanel();
        registerButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        resetPasswordButton = new javax.swing.JButton();
        toggleStatusButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24));
        setLayout(new java.awt.BorderLayout());

        topPanel.setBackground(new java.awt.Color(255, 255, 255));
        topPanel.setLayout(new java.awt.BorderLayout());

        titleLabel.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        titleLabel.setForeground(new java.awt.Color(33, 33, 33));
        titleLabel.setText("User Management");
        topPanel.add(titleLabel, java.awt.BorderLayout.WEST);

        searchPanel.setBackground(new java.awt.Color(255, 255, 255));

        searchLabel.setText("Search (ID, username, or role):");
        searchPanel.add(searchLabel);

        searchField.setColumns(16);
        searchPanel.add(searchField);

        searchButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        searchButton.setForeground(new java.awt.Color(255, 255, 255));
        searchButton.setBackground(new java.awt.Color(31, 78, 121));
        searchButton.setText("Search");
        searchButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        searchButton.setBorderPainted(false);
        searchButton.setFocusPainted(false);
        searchButton.setOpaque(true);
        searchPanel.add(searchButton);

        clearButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        clearButton.setForeground(new java.awt.Color(255, 255, 255));
        clearButton.setBackground(new java.awt.Color(31, 78, 121));
        clearButton.setText("Clear");
        clearButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        clearButton.setBorderPainted(false);
        clearButton.setFocusPainted(false);
        clearButton.setOpaque(true);
        searchPanel.add(clearButton);

        topPanel.add(searchPanel, java.awt.BorderLayout.EAST);

        add(topPanel, java.awt.BorderLayout.NORTH);

        tableWrapperPanel.setBackground(new java.awt.Color(255, 255, 255));
        tableWrapperPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 0, 16, 0));
        tableWrapperPanel.setLayout(new java.awt.BorderLayout());

        userTable.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        userTable.setRowHeight(24);
        userScrollPane.setViewportView(userTable);

        tableWrapperPanel.add(userScrollPane, java.awt.BorderLayout.CENTER);

        add(tableWrapperPanel, java.awt.BorderLayout.CENTER);

        buttonPanel.setBackground(new java.awt.Color(255, 255, 255));
        buttonPanel.setLayout(new java.awt.GridLayout(2, 3));

        registerButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        registerButton.setForeground(new java.awt.Color(255, 255, 255));
        registerButton.setBackground(new java.awt.Color(31, 78, 121));
        registerButton.setText("Register New User");
        registerButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        registerButton.setBorderPainted(false);
        registerButton.setFocusPainted(false);
        registerButton.setOpaque(true);
        buttonPanel.add(registerButton);

        editButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        editButton.setForeground(new java.awt.Color(255, 255, 255));
        editButton.setBackground(new java.awt.Color(31, 78, 121));
        editButton.setText("Edit Profile");
        editButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        editButton.setBorderPainted(false);
        editButton.setFocusPainted(false);
        editButton.setOpaque(true);
        buttonPanel.add(editButton);

        resetPasswordButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        resetPasswordButton.setForeground(new java.awt.Color(255, 255, 255));
        resetPasswordButton.setBackground(new java.awt.Color(31, 78, 121));
        resetPasswordButton.setText("Reset Password");
        resetPasswordButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        resetPasswordButton.setBorderPainted(false);
        resetPasswordButton.setFocusPainted(false);
        resetPasswordButton.setOpaque(true);
        buttonPanel.add(resetPasswordButton);

        toggleStatusButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        toggleStatusButton.setForeground(new java.awt.Color(255, 255, 255));
        toggleStatusButton.setBackground(new java.awt.Color(31, 78, 121));
        toggleStatusButton.setText("Toggle Account Status");
        toggleStatusButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        toggleStatusButton.setBorderPainted(false);
        toggleStatusButton.setFocusPainted(false);
        toggleStatusButton.setOpaque(true);
        buttonPanel.add(toggleStatusButton);

        deleteButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        deleteButton.setForeground(new java.awt.Color(255, 255, 255));
        deleteButton.setBackground(new java.awt.Color(31, 78, 121));
        deleteButton.setText("Delete Account");
        deleteButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        deleteButton.setBorderPainted(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setOpaque(true);
        buttonPanel.add(deleteButton);

        backButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        backButton.setForeground(new java.awt.Color(255, 255, 255));
        backButton.setBackground(new java.awt.Color(31, 78, 121));
        backButton.setText("Back to Dashboard");
        backButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setOpaque(true);
        buttonPanel.add(backButton);

        add(buttonPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    /** Builds the table model and wires screen behavior. */
    private void initializeBehavior() {
        searchButton.addActionListener(e -> applySearch());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            refreshTable(userManager.getUsers());
        });

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable.setModel(tableModel);
        registerButton.addActionListener(e -> openRegisterDialog());
        editButton.addActionListener(e -> openEditDialog());
        resetPasswordButton.addActionListener(e -> openResetPasswordDialog());
        toggleStatusButton.addActionListener(e -> toggleSelectedStatus());
        deleteButton.addActionListener(e -> deleteSelected());
        backButton.addActionListener(e -> mainFrame.showPanel(
                new AdminDashboardPanel(sessionManager, userManager, appointmentManager, clinicalManager,
                        assetManager, rateManager, insuranceManager, allocationManager, mainFrame, onLogout)));
    }

    private void applySearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            refreshTable(userManager.getUsers());
            return;
        }
        List<User> matches = new ArrayList<>();
        for (User user : userManager.getUsers()) {
            if (user.getUsername().toLowerCase().contains(query)
                    || user.getUserType().toLowerCase().contains(query)
                    || user.getUserId().toLowerCase().contains(query)) {
                matches.add(user);
            }
        }
        refreshTable(matches);
    }

    private void refreshTable(List<User> users) {
        tableModel.setRowCount(0);
        for (User user : users) {
            tableModel.addRow(new Object[]{
                    user.getUserId(),
                    roleDisplayName(user.getUserType()),
                    user.getUsername(),
                    user.getPhone(),
                    user.getEmail(),
                    user.isActive() ? "Yes" : "No"
            });
        }
    }

    private String getSelectedUserId() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return (String) tableModel.getValueAt(row, 0);
    }

    private String requireSelection() {
        String userId = getSelectedUserId();
        if (userId == null) {
            showError("Select a user first.");
        }
        return userId;
    }

    /** True if the given user ID belongs to the Admin currently logged into this screen. */
    private boolean isCurrentUser(String userId) {
        User currentUser = sessionManager.getCurrentUser();
        return currentUser != null && currentUser.getUserId().equals(userId);
    }

    /** Toggles the selected user's active state, mirroring the "Toggle Active/Availability" pattern used elsewhere. */
    private void toggleSelectedStatus() {
        String userId = requireSelection();
        if (userId == null) {
            return;
        }
        User user = userManager.findById(userId);
        if (user == null) {
            showError("User not found: " + userId);
            return;
        }
        if (user.isActive() && isCurrentUser(userId)) {
            showError("You cannot deactivate your own account while logged in.");
            return;
        }
        try {
            if (user.isActive()) {
                userManager.deactivateUser(userId);
            } else {
                userManager.reactivateUser(userId);
            }
            refreshTable(userManager.getUsers());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteSelected() {
        String userId = requireSelection();
        if (userId == null) {
            return;
        }
        if (isCurrentUser(userId)) {
            showError("You cannot delete your own account while logged in.");
            return;
        }
        User user = userManager.findById(userId);
        if (user == null) {
            showError("User not found: " + userId);
            return;
        }
        String blockingReason = findDependencyBlockingDeletion(user);
        if (blockingReason != null) {
            showError(blockingReason);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete user " + userId + "? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            userManager.deleteUser(userId);
            refreshTable(userManager.getUsers());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    /**
     * Checks the clinical and administrative records this screen already has access to for any
     * row that still points at this user, so deleting the user would leave that row referring to
     * an account that no longer exists. Returns a clear message naming the reason, or null if
     * deletion is safe. Deliberately narrow: it only inspects data already reachable from this
     * panel's existing manager references, and never rewrites those managers' own logic.
     */
    private String findDependencyBlockingDeletion(User user) {
        if (user instanceof Doctor) {
            String doctorId = ((Doctor) user).getDoctorId();
            int appointmentCount = 0;
            for (Appointment appointment : appointmentManager.getAppointments()) {
                if (appointment.getDoctor() != null && doctorId.equals(appointment.getDoctor().getDoctorId())) {
                    appointmentCount++;
                }
            }
            int recordCount = 0;
            for (MedicalRecord record : clinicalManager.getRecords()) {
                if (record.getDoctor() != null && doctorId.equals(record.getDoctor().getDoctorId())) {
                    recordCount++;
                }
            }
            int requestCount = 0;
            for (MedicalRequest request : clinicalManager.getAllRequests()) {
                if (request.getDoctor() != null && doctorId.equals(request.getDoctor().getDoctorId())) {
                    requestCount++;
                }
            }
            int feedbackCount = clinicalManager.getDoctorFeedback(doctorId).size();
            if (appointmentCount > 0 || recordCount > 0 || requestCount > 0 || feedbackCount > 0) {
                return "Cannot delete this Doctor: " + appointmentCount + " appointment(s), " + recordCount
                        + " medical record(s), " + requestCount + " medical request(s), and " + feedbackCount
                        + " feedback entr(y/ies) still refer to this account. Deactivate the account instead.";
            }
        } else if (user instanceof Patient) {
            String patientId = ((Patient) user).getPatientId();
            int appointmentCount = 0;
            for (Appointment appointment : appointmentManager.getAppointments()) {
                if (appointment.getPatient() != null && patientId.equals(appointment.getPatient().getPatientId())) {
                    appointmentCount++;
                }
            }
            int recordCount = clinicalManager.getPatientHistory(patientId).size();
            if (appointmentCount > 0 || recordCount > 0) {
                return "Cannot delete this Patient: " + appointmentCount + " appointment(s) and " + recordCount
                        + " medical record(s) still refer to this account. Deactivate the account instead.";
            }
        } else if (user instanceof AdminStaff) {
            String staffId = ((AdminStaff) user).getStaffId();
            int assetCount = 0;
            for (HospitalAsset asset : assetManager.getAssets()) {
                if (asset.getManagedBy() != null && staffId.equals(asset.getManagedBy().getStaffId())) {
                    assetCount++;
                }
            }
            int rateCount = 0;
            for (ConsultationRate rate : rateManager.getRates()) {
                if (rate.getConfiguredBy() != null && staffId.equals(rate.getConfiguredBy().getStaffId())) {
                    rateCount++;
                }
            }
            int networkCount = 0;
            for (InsuranceNetwork network : insuranceManager.getNetworks()) {
                if (network.getManagedBy() != null && staffId.equals(network.getManagedBy().getStaffId())) {
                    networkCount++;
                }
            }
            int allocationCount = 0;
            for (AssetAllocation allocation : allocationManager.getAllocations()) {
                if (staffId.equals(allocation.getAllocatedByAdminId())) {
                    allocationCount++;
                }
            }
            int requestCount = 0;
            for (MedicalRequest request : clinicalManager.getAllRequests()) {
                if (staffId.equals(request.getProcessedBy())) {
                    requestCount++;
                }
            }
            if (assetCount > 0 || rateCount > 0 || networkCount > 0 || allocationCount > 0 || requestCount > 0) {
                return "Cannot delete this Admin Staff account: " + assetCount + " hospital asset(s), " + rateCount
                        + " consultation rate(s), " + networkCount + " insurance network(s), " + allocationCount
                        + " asset allocation(s), and " + requestCount
                        + " processed request(s) still refer to this account. Deactivate the account instead.";
            }
        }
        return null;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String roleDisplayName(String userType) {
        switch (userType) {
            case "ADMIN_STAFF":
                return "Admin Staff";
            case "MEDICAL_MANAGER":
                return "Medical Manager";
            case "DOCTOR":
                return "Doctor";
            case "PATIENT":
                return "Patient";
            default:
                return userType;
        }
    }

    // ---- register dialog ----

    private void openRegisterDialog() {
        JDialog dialog = new JDialog(mainFrame, "Register New User", true);
        dialog.setLayout(new GridBagLayout());

        JComboBox<String> roleCombo = new JComboBox<>(ROLE_OPTIONS);
        JTextField usernameField = new JTextField(16);
        JPasswordField passwordField = new JPasswordField(16);
        JTextField phoneField = new JTextField(16);
        JTextField emailField = new JTextField(16);
        List<String> specialties = UserManager.getValidSpecialties();
        JComboBox<String> specialtyCombo = new JComboBox<>(specialties.toArray(new String[0]));
        JTextField dobField = new JTextField(16);
        JLabel specialtyLabel = new JLabel("Specialty:");
        JLabel dobLabel = new JLabel("Date of Birth (yyyy-MM-dd):");

        roleCombo.addActionListener(e -> updateExtraFieldVisibility(
                (String) roleCombo.getSelectedItem(), specialtyLabel, specialtyCombo, dobLabel, dobField));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(dialog, gbc, "Role:", roleCombo);
        addFormRow(dialog, gbc, "Username:", usernameField);
        addFormRow(dialog, gbc, "Password:", passwordField);
        addFormRow(dialog, gbc, "Phone:", phoneField);
        addFormRow(dialog, gbc, "Email:", emailField);
        addFormRow(dialog, gbc, specialtyLabel, specialtyCombo);
        addFormRow(dialog, gbc, dobLabel, dobField);

        updateExtraFieldVisibility(
                (String) roleCombo.getSelectedItem(), specialtyLabel, specialtyCombo, dobLabel, dobField);

        JButton saveButton = new JButton("Register");
        UIStyles.applyPrimaryButtonStyle(saveButton);
        JButton cancelButton = new JButton("Cancel");
        UIStyles.applyPrimaryButtonStyle(cancelButton);
        cancelButton.addActionListener(e -> dialog.dispose());

        saveButton.addActionListener(e -> {
            try {
                User newUser = buildUserFromForm((String) roleCombo.getSelectedItem(),
                        usernameField.getText().trim(),
                        new String(passwordField.getPassword()),
                        phoneField.getText().trim(),
                        emailField.getText().trim(),
                        (String) specialtyCombo.getSelectedItem(),
                        dobField.getText().trim());
                userManager.registerUser(newUser);
                dialog.dispose();
                refreshTable(userManager.getUsers());
            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(ex.getMessage());
            }
        });

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        dialog.add(cancelButton, gbc);
        gbc.gridx = 1;
        dialog.add(saveButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updateExtraFieldVisibility(String role, JLabel specialtyLabel, JComboBox<String> specialtyCombo,
            JLabel dobLabel, JTextField dobField) {
        boolean isDoctor = "Doctor".equals(role);
        boolean isPatient = "Patient".equals(role);
        specialtyLabel.setVisible(isDoctor);
        specialtyCombo.setVisible(isDoctor);
        dobLabel.setVisible(isPatient);
        dobField.setVisible(isPatient);
    }

    private User buildUserFromForm(String role, String username, String password, String phone,
            String email, String specialty, String dobText) {
        switch (role) {
            case "Admin Staff":
                return new AdminStaff(null, username, password, phone, email, "ADMIN_STAFF", null);
            case "Medical Manager":
                return new MedicalManager(null, username, password, phone, email, "MEDICAL_MANAGER", null);
            case "Doctor":
                return new Doctor(null, username, password, phone, email, "DOCTOR", null, specialty);
            case "Patient":
                Date dob = parseDateOfBirth(dobText);
                return new Patient(null, username, password, phone, email, "PATIENT", null, dob);
            default:
                throw new IllegalArgumentException("Unknown role: " + role);
        }
    }

    private Date parseDateOfBirth(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return ValidationUtil.parseStrictDate(text);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Date of birth must be in yyyy-MM-dd format");
        }
    }

    private void addFormRow(JDialog dialog, GridBagConstraints gbc, String labelText, Component field) {
        addFormRow(dialog, gbc, new JLabel(labelText), field);
    }

    private void addFormRow(JDialog dialog, GridBagConstraints gbc, JLabel label, Component field) {
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        dialog.add(label, gbc);
        gbc.gridx = 1;
        dialog.add(field, gbc);
        gbc.gridy++;
    }

    // ---- edit dialog ----

    private void openEditDialog() {
        String userId = requireSelection();
        if (userId == null) {
            return;
        }
        User user = userManager.findById(userId);
        if (user == null) {
            showError("User not found: " + userId);
            return;
        }

        JDialog dialog = new JDialog(mainFrame, "Edit User " + userId, true);
        dialog.setLayout(new GridBagLayout());

        boolean isDoctor = user instanceof Doctor;
        boolean isPatient = user instanceof Patient;

        JTextField usernameField = new JTextField(user.getUsername(), 16);
        JTextField phoneField = new JTextField(user.getPhone(), 16);
        JTextField emailField = new JTextField(user.getEmail(), 16);

        List<String> specialties = UserManager.getValidSpecialties();
        JComboBox<String> specialtyCombo = new JComboBox<>(specialties.toArray(new String[0]));
        if (isDoctor) {
            specialtyCombo.setSelectedItem(((Doctor) user).getSpecialty());
        }

        JTextField dobField = new JTextField(16);
        if (isPatient) {
            Date dateOfBirth = ((Patient) user).getDateOfBirth();
            if (dateOfBirth != null) {
                dobField.setText(DATE_FORMAT.format(dateOfBirth));
            }
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(dialog, gbc, "Username:", usernameField);
        addFormRow(dialog, gbc, "Phone:", phoneField);
        addFormRow(dialog, gbc, "Email:", emailField);
        if (isDoctor) {
            addFormRow(dialog, gbc, "Specialty:", specialtyCombo);
        }
        if (isPatient) {
            addFormRow(dialog, gbc, "Date of Birth (yyyy-MM-dd):", dobField);
        }

        JButton saveButton = new JButton("Save");
        UIStyles.applyPrimaryButtonStyle(saveButton);
        JButton cancelButton = new JButton("Cancel");
        UIStyles.applyPrimaryButtonStyle(cancelButton);
        cancelButton.addActionListener(e -> dialog.dispose());

        saveButton.addActionListener(e -> {
            try {
                // Parsing the date of birth text can throw before UserManager is ever called, and
                // updateManagedUser() itself validates everything else before changing anything -
                // so a bad specialty or date of birth can never leave a saved username/phone/email
                // change behind.
                String specialty = isDoctor ? (String) specialtyCombo.getSelectedItem() : null;
                Date dateOfBirth = isPatient ? parseDateOfBirth(dobField.getText().trim()) : null;
                userManager.updateManagedUser(userId, usernameField.getText().trim(),
                        phoneField.getText().trim(), emailField.getText().trim(), specialty, dateOfBirth);
                dialog.dispose();
                refreshTable(userManager.getUsers());
            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(ex.getMessage());
            }
        });

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        dialog.add(cancelButton, gbc);
        gbc.gridx = 1;
        dialog.add(saveButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ---- reset password dialog ----

    private void openResetPasswordDialog() {
        String userId = requireSelection();
        if (userId == null) {
            return;
        }
        User user = userManager.findById(userId);
        if (user == null) {
            showError("User not found: " + userId);
            return;
        }

        JDialog dialog = new JDialog(mainFrame, "Reset Password for " + user.getUsername(), true);
        dialog.setLayout(new GridBagLayout());

        JPasswordField newPasswordField = new JPasswordField(16);
        JPasswordField confirmPasswordField = new JPasswordField(16);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(dialog, gbc, "New Password:", newPasswordField);
        addFormRow(dialog, gbc, "Confirm Password:", confirmPasswordField);

        JButton saveButton = new JButton("Reset Password");
        UIStyles.applyPrimaryButtonStyle(saveButton);
        JButton cancelButton = new JButton("Cancel");
        UIStyles.applyPrimaryButtonStyle(cancelButton);
        cancelButton.addActionListener(e -> dialog.dispose());

        saveButton.addActionListener(e -> {
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            if (!newPassword.equals(confirmPassword)) {
                showError("Passwords do not match.");
                return;
            }
            try {
                userManager.resetPassword(userId, newPassword);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Password reset successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(ex.getMessage());
            }
        });

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        dialog.add(cancelButton, gbc);
        gbc.gridx = 1;
        dialog.add(saveButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JButton registerButton;
    private javax.swing.JButton resetPasswordButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JPanel tableWrapperPanel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JButton toggleStatusButton;
    private javax.swing.JPanel topPanel;
    private javax.swing.JScrollPane userScrollPane;
    private javax.swing.JTable userTable;
    // End of variables declaration//GEN-END:variables
}
