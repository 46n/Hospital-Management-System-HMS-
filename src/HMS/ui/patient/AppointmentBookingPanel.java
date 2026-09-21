package hms.ui.patient;

import hms.manager.AppointmentManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalManager;
import hms.manager.DoctorShiftManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.Appointment;
import hms.model.Doctor;
import hms.model.DoctorShift;
import hms.model.Patient;
import hms.model.User;
import hms.ui.common.AppointmentTableUtil;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Patient screen: browse available doctors, see the shifts that make up
 * their bookable hours, submit a booking request, and see the resulting
 * appointment list. All booking rules (existence, shift coverage,
 * double-booking, duplicate slot) live in AppointmentManager - this panel
 * only collects input and displays the result.
 */
public class AppointmentBookingPanel extends JPanel {

    private static final String[] DOCTOR_COLUMNS = {"ID", "Username", "Specialty"};
    private static final String[] SHIFT_COLUMNS = {"Date", "Start", "End"};
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final DoctorShiftManager shiftManager;
    private final ClinicalManager clinicalManager;
    private final BillingManager billingManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private DefaultTableModel doctorTableModel;
    private JTable doctorTable;
    private DefaultTableModel shiftTableModel;
    private DefaultTableModel appointmentTableModel;
    private JTable appointmentTable;
    private JTextField dateField;
    private JTextField timeField;

    public AppointmentBookingPanel(UserManager userManager, AppointmentManager appointmentManager,
            DoctorShiftManager shiftManager, ClinicalManager clinicalManager, BillingManager billingManager,
            SessionManager sessionManager, MainFrame mainFrame, Runnable onLogout) {
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.shiftManager = shiftManager;
        this.clinicalManager = clinicalManager;
        this.billingManager = billingManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
        buildLayout();
        refreshDoctorTable();
        refreshAppointmentTable();
    }

    private void buildLayout() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        JLabel titleLabel = new JLabel("Book an Appointment");
        UIStyles.applyHeadingStyle(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        centerPanel.setBackground(Theme.BACKGROUND_COLOR);
        centerPanel.add(createDoctorPanel());
        centerPanel.add(createAppointmentPanel());
        add(centerPanel, BorderLayout.CENTER);

        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createDoctorPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        panel.setBackground(Theme.BACKGROUND_COLOR);
        panel.add(createDoctorListPanel());
        panel.add(createShiftPanel());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BACKGROUND_COLOR);
        wrapper.add(panel, BorderLayout.CENTER);
        wrapper.add(createBookingFormPanel(), BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel createDoctorListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND_COLOR);

        JLabel label = new JLabel("Available Doctors");
        label.setFont(Theme.DEFAULT_FONT);
        panel.add(label, BorderLayout.NORTH);

        doctorTableModel = new DefaultTableModel(DOCTOR_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        doctorTable = new JTable(doctorTableModel);
        doctorTable.setRowHeight(24);
        doctorTable.setFont(Theme.DEFAULT_FONT);
        doctorTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshShiftTable();
            }
        });
        panel.add(new JScrollPane(doctorTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createShiftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND_COLOR);

        JLabel label = new JLabel("Selected Doctor's Working Shifts");
        label.setFont(Theme.DEFAULT_FONT);
        panel.add(label, BorderLayout.NORTH);

        shiftTableModel = new DefaultTableModel(SHIFT_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable shiftTable = new JTable(shiftTableModel);
        shiftTable.setRowHeight(24);
        shiftTable.setFont(Theme.DEFAULT_FONT);
        panel.add(new JScrollPane(shiftTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBookingFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Theme.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;

        formPanel.add(new JLabel("Date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        dateField = new JTextField(10);
        formPanel.add(dateField, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Time (HH:mm):"), gbc);
        gbc.gridx = 3;
        timeField = new JTextField(6);
        formPanel.add(timeField, gbc);

        gbc.gridx = 4;
        JButton bookButton = new JButton("Book Selected Doctor");
        UIStyles.applyPrimaryButtonStyle(bookButton);
        bookButton.addActionListener(e -> bookSelectedDoctor());
        formPanel.add(bookButton, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BACKGROUND_COLOR);
        JLabel hint = new JLabel("Pick a date and a time that falls within one of the shifts shown above.");
        hint.setFont(Theme.DEFAULT_FONT);
        wrapper.add(hint, BorderLayout.NORTH);
        wrapper.add(formPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createAppointmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND_COLOR);

        JLabel label = new JLabel("My Appointments");
        label.setFont(Theme.DEFAULT_FONT);
        panel.add(label, BorderLayout.NORTH);

        appointmentTableModel = new DefaultTableModel(AppointmentTableUtil.COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointmentTable = new JTable(appointmentTableModel);
        appointmentTable.setRowHeight(24);
        appointmentTable.setFont(Theme.DEFAULT_FONT);
        panel.add(new JScrollPane(appointmentTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        actionPanel.setBackground(Theme.BACKGROUND_COLOR);
        JButton cancelApptButton = new JButton("Cancel Selected");
        JButton rescheduleButton = new JButton("Reschedule Selected");
        UIStyles.applyPrimaryButtonStyle(cancelApptButton);
        UIStyles.applyPrimaryButtonStyle(rescheduleButton);
        cancelApptButton.addActionListener(e -> cancelSelected());
        rescheduleButton.addActionListener(e -> openRescheduleDialog());
        actionPanel.add(cancelApptButton);
        actionPanel.add(rescheduleButton);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.BACKGROUND_COLOR);
        JButton backButton = new JButton("Back to Dashboard");
        UIStyles.applyPrimaryButtonStyle(backButton);
        backButton.addActionListener(e -> mainFrame.showPanel(new PatientDashboardNew(
                sessionManager, userManager, appointmentManager, shiftManager, clinicalManager, billingManager,
                mainFrame, onLogout)));
        panel.add(backButton);
        return panel;
    }

    private List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        for (User user : userManager.getUsers()) {
            if (user instanceof Doctor && user.isActive()) {
                doctors.add((Doctor) user);
            }
        }
        return doctors;
    }

    private void refreshDoctorTable() {
        doctorTableModel.setRowCount(0);
        for (Doctor doctor : getAllDoctors()) {
            doctorTableModel.addRow(new Object[]{doctor.getDoctorId(), doctor.getUsername(), doctor.getSpecialty()});
        }
    }

    private void refreshShiftTable() {
        shiftTableModel.setRowCount(0);
        int row = doctorTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        String doctorId = (String) doctorTableModel.getValueAt(row, 0);
        String todayStr = DATE_FORMAT.format(new Date());
        for (DoctorShift shift : shiftManager.getShiftsForDoctor(doctorId)) {
            if (DATE_FORMAT.format(shift.getShiftDate()).compareTo(todayStr) >= 0) {
                shiftTableModel.addRow(new Object[]{
                        DATE_FORMAT.format(shift.getShiftDate()),
                        shift.getStartTime().format(TIME_FORMAT),
                        shift.getEndTime().format(TIME_FORMAT)
                });
            }
        }
    }

    private void refreshAppointmentTable() {
        appointmentTableModel.setRowCount(0);
        Patient patient = currentPatient();
        for (Appointment appointment : appointmentManager.listForPatient(patient.getPatientId())) {
            appointmentTableModel.addRow(AppointmentTableUtil.toRow(appointment));
        }
    }

    private Patient currentPatient() {
        return (Patient) sessionManager.getCurrentUser();
    }

    private void bookSelectedDoctor() {
        int row = doctorTable.getSelectedRow();
        if (row < 0) {
            showError("Select a doctor first.");
            return;
        }
        String doctorId = (String) doctorTableModel.getValueAt(row, 0);

        Date date;
        LocalTime time;
        try {
            date = DATE_FORMAT.parse(dateField.getText().trim());
        } catch (ParseException e) {
            showError("Date must be in yyyy-MM-dd format.");
            return;
        }
        try {
            time = LocalTime.parse(timeField.getText().trim(), TIME_FORMAT);
        } catch (DateTimeParseException e) {
            showError("Time must be in HH:mm format.");
            return;
        }

        try {
            appointmentManager.book(currentPatient().getPatientId(), doctorId, date, time);
            JOptionPane.showMessageDialog(this, "Appointment booked.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dateField.setText("");
            timeField.setText("");
            refreshAppointmentTable();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void cancelSelected() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            showError("Select an appointment first.");
            return;
        }
        String appointmentId = (String) appointmentTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel appointment " + appointmentId + "? This cannot be undone.",
                "Confirm Cancel", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            appointmentManager.cancel(appointmentId, currentPatient().getPatientId());
            refreshAppointmentTable();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void openRescheduleDialog() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            showError("Select an appointment first.");
            return;
        }
        String appointmentId = (String) appointmentTableModel.getValueAt(row, 0);
        String currentDate = (String) appointmentTableModel.getValueAt(row, 3);
        String currentTime = (String) appointmentTableModel.getValueAt(row, 4);

        JDialog dialog = new JDialog(mainFrame, "Reschedule Appointment", true);
        dialog.setLayout(new GridBagLayout());

        JTextField newDateField = new JTextField(currentDate, 12);
        JTextField newTimeField = new JTextField(currentTime, 8);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;

        dialog.add(new JLabel("New Date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        dialog.add(newDateField, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        dialog.add(new JLabel("New Time (HH:mm):"), gbc);
        gbc.gridx = 1;
        dialog.add(newTimeField, gbc);
        gbc.gridy++;

        JButton saveButton = new JButton("Reschedule");
        UIStyles.applyPrimaryButtonStyle(saveButton);
        JButton cancelButton = new JButton("Cancel");
        UIStyles.applyPrimaryButtonStyle(cancelButton);
        cancelButton.addActionListener(e -> dialog.dispose());

        saveButton.addActionListener(e -> {
            Date newDate;
            LocalTime newTime;
            try {
                newDate = DATE_FORMAT.parse(newDateField.getText().trim());
            } catch (ParseException ex) {
                showError("Date must be in yyyy-MM-dd format.");
                return;
            }
            try {
                newTime = LocalTime.parse(newTimeField.getText().trim(), TIME_FORMAT);
            } catch (DateTimeParseException ex) {
                showError("Time must be in HH:mm format.");
                return;
            }
            try {
                appointmentManager.reschedule(appointmentId, newDate, newTime);
                dialog.dispose();
                refreshAppointmentTable();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });

        gbc.gridx = 0;
        dialog.add(cancelButton, gbc);
        gbc.gridx = 1;
        dialog.add(saveButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
