package hms.ui.patient;

import hms.manager.AppointmentManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalManager;
import hms.manager.DoctorShiftManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.ui.common.MainFrame;
import hms.ui.common.EditProfileNew;

public class PatientController {

    private final SessionManager sessionManager;
    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final DoctorShiftManager shiftManager;
    private final ClinicalManager clinicalManager;
    private final BillingManager billingManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    public PatientController(
            SessionManager sessionManager,
            UserManager userManager,
            AppointmentManager appointmentManager,
            DoctorShiftManager shiftManager,
            ClinicalManager clinicalManager,
            BillingManager billingManager,
            MainFrame mainFrame,
            Runnable onLogout) {

        this.sessionManager = sessionManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.shiftManager = shiftManager;
        this.clinicalManager = clinicalManager;
        this.billingManager = billingManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
    }
    
    public void openDashboard() {

        showPatientPanel(
            new PatientDashboardNew(
                    sessionManager,
                    userManager,
                    appointmentManager,
                    shiftManager,
                    clinicalManager,
                    billingManager,
                    mainFrame,
                    onLogout
            )
        );
    }
    
    public void openAppointments() {

        AppointmentBookingNew bookingPanel =
            new AppointmentBookingNew(
                    userManager,
                    appointmentManager,
                    shiftManager,
                    clinicalManager,
                    billingManager,
                    sessionManager,
                    mainFrame,
                    onLogout
            );
        bookingPanel.setPreferredSize(
            new java.awt.Dimension(1100, 750)
        );

    javax.swing.JScrollPane scrollPane =
            new javax.swing.JScrollPane(bookingPanel);

    scrollPane.setBorder(null);

    scrollPane.setVerticalScrollBarPolicy(
            javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
    );

    scrollPane.setHorizontalScrollBarPolicy(
            javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
    );

    scrollPane.getVerticalScrollBar().setUnitIncrement(16);

    javax.swing.JPanel bookingWrapper =
            new javax.swing.JPanel(
                    new java.awt.BorderLayout()
            );

    bookingWrapper.setBackground(
            new java.awt.Color(204, 204, 204)
    );

    bookingWrapper.setPreferredSize(
            new java.awt.Dimension(1100, 600)
    );

    bookingWrapper.add(
            scrollPane,
            java.awt.BorderLayout.CENTER
    );

    showPatientPanel(bookingWrapper);
}



    
    
    public void openMedicalHistory() {

    showPatientPanel(
            new ViewMedicalHistoryNew(
                    clinicalManager,
                    appointmentManager,
                    shiftManager,
                    billingManager,
                    sessionManager,
                    userManager,
                    mainFrame,
                    onLogout
            )
        );
    }





    public void openPrescriptionsBills() {

        showPatientPanel(
            new ViewPrescriptionsBillsNew(
                    clinicalManager,
                    appointmentManager,
                    shiftManager,
                    billingManager,
                    userManager,
                    sessionManager,
                    mainFrame,
                    onLogout
                )
        );
    }
    
    public void openFeedback() {

    showPatientPanel(
            new SubmitFeedbackNew(
                    clinicalManager,
                    appointmentManager,
                    shiftManager,
                    billingManager,
                    userManager,
                    sessionManager,
                    mainFrame,
                    onLogout
            )
    );
}
    
    public void openProfile() {

    showPatientPanel(
            new EditProfileNew(
                    sessionManager,
                    userManager,
                    this::openDashboard
            )
    );
}
    

    

    private void showPatientPanel(javax.swing.JPanel panel) {

    javax.swing.JPanel wrapper =
            new javax.swing.JPanel(new java.awt.GridBagLayout());

    wrapper.setBackground(new java.awt.Color(204, 204, 204));

    wrapper.add(panel);

    mainFrame.showPanel(wrapper);
}





    public void logout() {
        sessionManager.logout();

        if (onLogout != null) {
            onLogout.run();
        }
    }
}
