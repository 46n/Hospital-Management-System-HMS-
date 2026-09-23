/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package hms.ui.medicalmanager;
import hms.manager.AssessmentTypeManager;
import hms.manager.SessionManager;
import hms.model.AssessmentType;
import hms.model.MedicalManager;
import hms.model.User;
import hms.ui.common.UIStyles;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author abd
 */
public class AssessmentTypeManagementNew extends javax.swing.JPanel {
    
    private SessionManager sessionManager;
    private AssessmentTypeManager typeManager;
    private Runnable onBack;

    private String editingAssessmentId = null;

    /**
     * Creates new form AssessmentTypeManagementNew
     */
    public AssessmentTypeManagementNew() {
      initComponents();
      applyButtonStyles();
  }

    public AssessmentTypeManagementNew(
            SessionManager sessionManager,
            AssessmentTypeManager typeManager,
            Runnable onBack) {

        initComponents();
        applyButtonStyles();

        this.sessionManager = sessionManager;
        this.typeManager = typeManager;
        this.onBack = onBack;

        loadCategories();
        loadGradingRules();
        refreshAssessmentTable();
    }

    private void applyButtonStyles() {
    UIStyles.applyPrimaryButtonStyle(btnAddType);
    UIStyles.applyPrimaryButtonStyle(btnEditSelected);
    UIStyles.applyPrimaryButtonStyle(btnToggleActive);
    UIStyles.applyPrimaryButtonStyle(btnDeleteSelected);
    UIStyles.applyPrimaryButtonStyle(btnBackDashboard);
    UIStyles.applyPrimaryButtonStyle(btnClearFields);
    UIStyles.applyPrimaryButtonStyle(btnSaveAssessment);
}

    private void loadCategories() {

    cmbCategory.removeAllItems();

    for (String category
            : AssessmentTypeManager.getValidCategories()) {

        cmbCategory.addItem(category);
    }

    cmbCategory.setSelectedIndex(-1);
}
    
    private void loadGradingRules() {

    cmbGradingRule.removeAllItems();

    for (String rule
            : AssessmentTypeManager.getValidGradingRules()) {

        cmbGradingRule.addItem(rule);
    }

    cmbGradingRule.setSelectedIndex(-1);
}
    
    private void clearAssessmentFields() {
    AssessmentNameTf.setText("");
    BaseChargeTf.setText("");
    cmbCategory.setSelectedIndex(-1);
    cmbGradingRule.setSelectedIndex(-1);
    bgActive.clearSelection();
    }
    
    
    
    private void prepareNewAssessment() {
    editingAssessmentId = null;
    clearAssessmentFields();
    // New assessment types are active by default
    rbActiveYes.setSelected(true);
    AssessmentNameTf.requestFocus();
    }
    
    private void saveAssessment() {
    try {
        String name = AssessmentNameTf.getText().trim();
        Object selectedCategory = cmbCategory.getSelectedItem();
        Object selectedRule = cmbGradingRule.getSelectedItem();
        if (selectedCategory == null) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Please select a category."
            );
            return;
        }

        if (selectedRule == null) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Please select a grading rule."
            );
            return;
        }

        if (!rbActiveYes.isSelected()
                && !rbActiveNo.isSelected()) {

            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Please select Active: Yes or No."
            );
            return;
        }

        String chargeText =
                BaseChargeTf.getText().trim();

        if (chargeText.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Please enter the base charge."
            );
            return;
        }
        double baseCharge = Double.parseDouble(chargeText);
        String category = selectedCategory.toString();
        String gradingRule =
                selectedRule.toString();
        boolean active = rbActiveYes.isSelected();
        if (editingAssessmentId == null) {
            MedicalManager manager = getCurrentManager();
            AssessmentType created = typeManager.createType( name, category,gradingRule, baseCharge, manager.getManagerId());
            typeManager.setActive( created.getAssessmentTypeId(),active
            );
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Assessment type created successfully."
            );
        } else {
            typeManager.updateType(
                    editingAssessmentId,
                    name,
                    category,
                    gradingRule,
                    baseCharge
            );
            typeManager.setActive(
                    editingAssessmentId,
                    active
            );
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Assessment type updated successfully."
            );
        }
        refreshAssessmentTable();
        prepareNewAssessment();
    } catch (NumberFormatException ex) {
        javax.swing.JOptionPane.showMessageDialog(
                this,
                "Base charge must be a valid number.",
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE
        );
    } catch (IllegalArgumentException ex) {
        javax.swing.JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE
        );
    }
}
    
    
    private void loadSelectedAssessment() {
    int selectedRow = tblAssessmentTypes.getSelectedRow();
    if (selectedRow == -1) {
        javax.swing.JOptionPane.showMessageDialog(
                this,
                "Please select an assessment type first."
        );
        return;
    }
    int modelRow =
            tblAssessmentTypes.convertRowIndexToModel(
                    selectedRow
            );
    String typeId =
            tblAssessmentTypes.getModel()
                    .getValueAt(modelRow, 0)
                    .toString();
    AssessmentType type =
            typeManager.findById(typeId);
    if (type == null) {
        javax.swing.JOptionPane.showMessageDialog(
                this,
                "Assessment type could not be found."
        );
        return;
    }
    editingAssessmentId =
            type.getAssessmentTypeId();

    AssessmentNameTf.setText(
            type.getName()
    );
    cmbCategory.setSelectedItem(
            type.getCategory()
    );
    cmbGradingRule.setSelectedItem(
            type.getGradingRule()
    );
    BaseChargeTf.setText(
            String.valueOf(type.getBaseCharge())
    );
    if (type.isActive()) {
        rbActiveYes.setSelected(true);
    } else {
        rbActiveNo.setSelected(true);
    }
    AssessmentNameTf.requestFocus();
}
    
    
    
    
    private void deleteSelectedAssessment() {

    int selectedRow =
            tblAssessmentTypes.getSelectedRow();

    if (selectedRow == -1) {

        javax.swing.JOptionPane.showMessageDialog(
                this,
                "Please select an assessment type first."
        );

        return;
    }

    int modelRow =
            tblAssessmentTypes.convertRowIndexToModel(
                    selectedRow
            );

    String typeId =
            tblAssessmentTypes.getModel()
                    .getValueAt(modelRow, 0)
                    .toString();

    int choice =
            javax.swing.JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete this assessment type?",
                    "Confirm Delete",
                    javax.swing.JOptionPane.YES_NO_OPTION
            );

    if (choice != javax.swing.JOptionPane.YES_OPTION) {
        return;
    }

    try {

        typeManager.deleteType(typeId);

        editingAssessmentId = null;

        prepareNewAssessment();
        refreshAssessmentTable();

        javax.swing.JOptionPane.showMessageDialog(
                this,
                "Assessment type deleted successfully."
        );

    } catch (IllegalArgumentException ex) {

        javax.swing.JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE
        );
    }
}
    
    private void toggleSelectedAssessmentActive() {
    int selectedRow =
            tblAssessmentTypes.getSelectedRow();
    if (selectedRow == -1) {
        javax.swing.JOptionPane.showMessageDialog(
                this,
                "Please select an assessment type first."
        );

        return;
    }
    int modelRow =
            tblAssessmentTypes.convertRowIndexToModel(
                    selectedRow
            );
    String typeId =
            tblAssessmentTypes.getModel()
                    .getValueAt(modelRow, 0)
                    .toString();
    AssessmentType type =
            typeManager.findById(typeId);

    if (type == null) {
        return;
    }
    typeManager.setActive(
            typeId,
            !type.isActive()
    );
    refreshAssessmentTable();
}
    
    
    private void goBack() {

    if (onBack != null) {
        onBack.run();
    }
}
    
    private MedicalManager getCurrentManager() {

    User currentUser = sessionManager.getCurrentUser();

    if (!(currentUser instanceof MedicalManager)) {
        throw new IllegalStateException(
                "Current user is not a Medical Manager"
        );
    }

    return (MedicalManager) currentUser;
}
    
    
    private void refreshAssessmentTable() {
        

    DefaultTableModel model =
            (DefaultTableModel) tblAssessmentTypes.getModel();

    model.setRowCount(0);

    for (AssessmentType type : typeManager.getTypes()) {

        model.addRow(new Object[]{
            type.getAssessmentTypeId(),
            type.getName(),
            type.getCategory(),
            type.getGradingRule(),
            type.getBaseCharge(),
            type.isActive() ? "Yes" : "No"
        });
    }
}
    
    
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgActive = new javax.swing.ButtonGroup();
        lblTitle = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblAssessmentTypes = new javax.swing.JTable();
        btnAddType = new javax.swing.JButton();
        btnEditSelected = new javax.swing.JButton();
        btnToggleActive = new javax.swing.JButton();
        btnDeleteSelected = new javax.swing.JButton();
        btnBackDashboard = new javax.swing.JButton();
        pnlAssessmentDetails = new javax.swing.JPanel();
        lblDetailsTitle = new javax.swing.JLabel();
        lblAssessmentName = new javax.swing.JLabel();
        AssessmentNameTf = new javax.swing.JTextField();
        lblCategory = new javax.swing.JLabel();
        cmbCategory = new javax.swing.JComboBox<>();
        lblGradingRule = new javax.swing.JLabel();
        cmbGradingRule = new javax.swing.JComboBox<>();
        lblBaseCharge = new javax.swing.JLabel();
        BaseChargeTf = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        rbActiveYes = new javax.swing.JRadioButton();
        rbActiveNo = new javax.swing.JRadioButton();
        btnClearFields = new javax.swing.JButton();
        btnSaveAssessment = new javax.swing.JButton();

        setBackground(new java.awt.Color(204, 204, 204));

        lblTitle.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        lblTitle.setText("Assessment Type Management");

        tblAssessmentTypes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Category", "Grading Rule", "Base Charge", "Active"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblAssessmentTypes);

        btnAddType.setBackground(new java.awt.Color(51, 51, 51));
        btnAddType.setForeground(new java.awt.Color(255, 255, 255));
        btnAddType.setText("Add Assessment");
        btnAddType.addActionListener(this::btnAddTypeActionPerformed);

        btnEditSelected.setBackground(new java.awt.Color(51, 51, 51));
        btnEditSelected.setForeground(new java.awt.Color(255, 255, 255));
        btnEditSelected.setText("Edit Selected");
        btnEditSelected.addActionListener(this::btnEditSelectedActionPerformed);

        btnToggleActive.setBackground(new java.awt.Color(51, 51, 51));
        btnToggleActive.setForeground(new java.awt.Color(255, 255, 255));
        btnToggleActive.setText("Toggle Active");
        btnToggleActive.addActionListener(this::btnToggleActiveActionPerformed);

        btnDeleteSelected.setBackground(new java.awt.Color(51, 51, 51));
        btnDeleteSelected.setForeground(new java.awt.Color(255, 255, 255));
        btnDeleteSelected.setText("Delete Assessment");
        btnDeleteSelected.addActionListener(this::btnDeleteSelectedActionPerformed);

        btnBackDashboard.setBackground(new java.awt.Color(51, 51, 51));
        btnBackDashboard.setForeground(new java.awt.Color(255, 255, 255));
        btnBackDashboard.setText("Back to Dashboard");
        btnBackDashboard.addActionListener(this::btnBackDashboardActionPerformed);

        lblDetailsTitle.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        lblDetailsTitle.setText("Assessment Type Details");

        lblAssessmentName.setText("Name:");

        AssessmentNameTf.addActionListener(this::AssessmentNameTfActionPerformed);

        lblCategory.setText("Category:");

        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblGradingRule.setText("Grading Rule:");

        cmbGradingRule.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblBaseCharge.setText("Base Charge:");

        jLabel1.setText("Active:");

        bgActive.add(rbActiveYes);
        rbActiveYes.setText("Yes");

        bgActive.add(rbActiveNo);
        rbActiveNo.setText("No");

        btnClearFields.setBackground(new java.awt.Color(51, 51, 51));
        btnClearFields.setForeground(new java.awt.Color(255, 255, 255));
        btnClearFields.setText("Clear");
        btnClearFields.addActionListener(this::btnClearFieldsActionPerformed);

        btnSaveAssessment.setBackground(new java.awt.Color(51, 51, 51));
        btnSaveAssessment.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveAssessment.setText("Save Assessment");
        btnSaveAssessment.addActionListener(this::btnSaveAssessmentActionPerformed);

        javax.swing.GroupLayout pnlAssessmentDetailsLayout = new javax.swing.GroupLayout(pnlAssessmentDetails);
        pnlAssessmentDetails.setLayout(pnlAssessmentDetailsLayout);
        pnlAssessmentDetailsLayout.setHorizontalGroup(
            pnlAssessmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlAssessmentDetailsLayout.createSequentialGroup()
                .addGroup(pnlAssessmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlAssessmentDetailsLayout.createSequentialGroup()
                        .addGroup(pnlAssessmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlAssessmentDetailsLayout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addComponent(lblDetailsTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlAssessmentDetailsLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblGradingRule))
                            .addGroup(pnlAssessmentDetailsLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblAssessmentName)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(AssessmentNameTf, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(pnlAssessmentDetailsLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cmbGradingRule, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlAssessmentDetailsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlAssessmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlAssessmentDetailsLayout.createSequentialGroup()
                                .addComponent(lblCategory)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cmbCategory, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlAssessmentDetailsLayout.createSequentialGroup()
                                .addGroup(pnlAssessmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblBaseCharge)
                                    .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlAssessmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(BaseChargeTf)
                                    .addGroup(pnlAssessmentDetailsLayout.createSequentialGroup()
                                        .addGroup(pnlAssessmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(rbActiveNo)
                                            .addComponent(rbActiveYes))
                                        .addGap(0, 0, Short.MAX_VALUE)))))))
                .addGap(77, 77, 77))
            .addGroup(pnlAssessmentDetailsLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(btnClearFields)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSaveAssessment)
                .addGap(37, 37, 37))
        );
        pnlAssessmentDetailsLayout.setVerticalGroup(
            pnlAssessmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAssessmentDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblDetailsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlAssessmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAssessmentName)
                    .addComponent(AssessmentNameTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlAssessmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCategory)
                    .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAssessmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGradingRule)
                    .addComponent(cmbGradingRule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAssessmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BaseChargeTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBaseCharge))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlAssessmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(rbActiveYes))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbActiveNo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlAssessmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSaveAssessment, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClearFields, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 578, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnAddType)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnEditSelected, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnToggleActive)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnDeleteSelected)))
                        .addGap(27, 27, 27)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnBackDashboard)
                            .addComponent(pnlAssessmentDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 313, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlAssessmentDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE))
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddType, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditSelected, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnToggleActive, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteSelected, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBackDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(57, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTypeActionPerformed
        // TODO add your handling code here:
        prepareNewAssessment();
    }//GEN-LAST:event_btnAddTypeActionPerformed

    private void btnEditSelectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditSelectedActionPerformed
        // TODO add your handling code here:
        loadSelectedAssessment();
    }//GEN-LAST:event_btnEditSelectedActionPerformed

    private void AssessmentNameTfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AssessmentNameTfActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_AssessmentNameTfActionPerformed

    private void btnClearFieldsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearFieldsActionPerformed
        // TODO add your handling code here:
        clearAssessmentFields();
    }//GEN-LAST:event_btnClearFieldsActionPerformed

    private void btnSaveAssessmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveAssessmentActionPerformed
        // TODO add your handling code here:
        saveAssessment();
    }//GEN-LAST:event_btnSaveAssessmentActionPerformed

    private void btnDeleteSelectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteSelectedActionPerformed
        // TODO add your handling code here:
        deleteSelectedAssessment();
    }//GEN-LAST:event_btnDeleteSelectedActionPerformed

    private void btnToggleActiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnToggleActiveActionPerformed
        // TODO add your handling code here:
        toggleSelectedAssessmentActive();
    }//GEN-LAST:event_btnToggleActiveActionPerformed

    private void btnBackDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackDashboardActionPerformed
        // TODO add your handling code here:
        goBack();
    }//GEN-LAST:event_btnBackDashboardActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField AssessmentNameTf;
    private javax.swing.JTextField BaseChargeTf;
    private javax.swing.ButtonGroup bgActive;
    private javax.swing.JButton btnAddType;
    private javax.swing.JButton btnBackDashboard;
    private javax.swing.JButton btnClearFields;
    private javax.swing.JButton btnDeleteSelected;
    private javax.swing.JButton btnEditSelected;
    private javax.swing.JButton btnSaveAssessment;
    private javax.swing.JButton btnToggleActive;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbGradingRule;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAssessmentName;
    private javax.swing.JLabel lblBaseCharge;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblDetailsTitle;
    private javax.swing.JLabel lblGradingRule;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlAssessmentDetails;
    private javax.swing.JRadioButton rbActiveNo;
    private javax.swing.JRadioButton rbActiveYes;
    private javax.swing.JTable tblAssessmentTypes;
    // End of variables declaration//GEN-END:variables
}
