package hms.manager;

import hms.data.TextFileUtil;
import hms.model.Appointment;
import hms.model.AssessmentResult;
import hms.model.AssessmentType;
import hms.model.Doctor;
import hms.model.Feedback;
import hms.model.MedicalRecord;
import hms.model.MedicalRequest;
import hms.model.Patient;
import hms.model.Prescription;
import hms.util.IdGenerator;
import hms.util.ValidationUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Coordinates clinical documentation: medical records, prescriptions,
 * lab/imaging requests, assessment results, and patient feedback.
 */
public class ClinicalManager {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final String RECORDS_FILE = "data/medical_records.txt";
    private static final String RECORDS_HEADER =
            "#recordId|appointmentId|patientId|doctorId|vitalSigns|consultationNotes|diagnosis|recordDate";

    private static final String PRESCRIPTIONS_FILE = "data/prescriptions.txt";
    private static final String PRESCRIPTIONS_HEADER =
            "#prescriptionId|recordId|doctorId|patientId|medicationDetails|dosage|instructions|prescribedDate";

    private static final String REQUESTS_FILE = "data/medical_requests.txt";
    private static final String REQUESTS_HEADER = "#requestId|recordId|doctorId|requestType|requestDate|status";

    private static final String RESULTS_FILE = "data/assessment_results.txt";
    private static final String RESULTS_HEADER =
            "#resultId|recordId|assessmentName|value|grade|resultDate|recordedByDoctorId|assessmentTypeId";

    private static final String FEEDBACK_FILE = "data/feedback.txt";
    private static final String FEEDBACK_HEADER = "#feedbackId|patientId|doctorId|rating|comment|feedbackDate|appointmentId";

    private final List<MedicalRecord> records = new ArrayList<>();
    private final List<Feedback> feedbackEntries = new ArrayList<>();

    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final AssessmentTypeManager typeManager;

    private final File recordsFile;
    private final File prescriptionsFile;
    private final File requestsFile;
    private final File resultsFile;
    private final File feedbackFile;

    public ClinicalManager(UserManager userManager, AppointmentManager appointmentManager,
            AssessmentTypeManager typeManager) {
        this(userManager, appointmentManager, typeManager, new File(RECORDS_FILE), new File(PRESCRIPTIONS_FILE),
                new File(REQUESTS_FILE), new File(RESULTS_FILE), new File(FEEDBACK_FILE));
    }

    public ClinicalManager(UserManager userManager, AppointmentManager appointmentManager,
            AssessmentTypeManager typeManager, File recordsFile, File prescriptionsFile, File requestsFile,
            File resultsFile, File feedbackFile) {
        if (userManager == null || appointmentManager == null || typeManager == null) {
            throw new IllegalArgumentException(
                    "User manager, appointment manager, and assessment type manager are required");
        }
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.typeManager = typeManager;
        this.recordsFile = recordsFile;
        this.prescriptionsFile = prescriptionsFile;
        this.requestsFile = requestsFile;
        this.resultsFile = resultsFile;
        this.feedbackFile = feedbackFile;
        loadRecords();
        loadPrescriptions();
        loadRequests();
        loadResults();
        loadFeedback();
    }

    public MedicalRecord openRecord(String appointmentId, String doctorId) {
        Appointment appointment = appointmentManager.findById(appointmentId);
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment not found: " + appointmentId);
        }
        if (appointment.getDoctor() == null || !doctorId.equals(appointment.getDoctor().getDoctorId())) {
            throw new IllegalArgumentException("Only the assigned doctor can open this consultation");
        }
        MedicalRecord record = new MedicalRecord(nextRecordId(), "", "", "", new Date());
        record.setAppointment(appointment);
        record.setPatient(appointment.getPatient());
        record.setDoctor(appointment.getDoctor());
        appointment.setMedicalRecord(record);
        records.add(record);
        saveRecords();
        return record;
    }

    /** Reuses the appointment's existing record if one was already opened, instead of creating a duplicate. */
    public MedicalRecord getOrOpenRecord(String appointmentId, String doctorId) {
        MedicalRecord existing = findRecordByAppointmentId(appointmentId);
        if (existing != null) {
            return existing;
        }
        return openRecord(appointmentId, doctorId);
    }

    public void updateVitalsAndNotes(String recordId, String vitalSigns, String diagnosis, String consultationNotes) {
        MedicalRecord record = requireRecord(recordId);
        record.setVitalSigns(vitalSigns);
        record.setDiagnosis(diagnosis);
        record.setConsultationNotes(consultationNotes);
        saveRecords();
    }

    public Prescription addPrescription(String recordId, String medicationDetails, String dosage, String instructions) {
        MedicalRecord record = requireRecord(recordId);
        Prescription prescription = new Prescription(nextPrescriptionId(), medicationDetails, dosage, instructions, new Date());
        prescription.validate();
        prescription.setMedicalRecord(record);
        prescription.setDoctor(record.getDoctor());
        prescription.setPatient(record.getPatient());
        record.addPrescription(prescription);
        savePrescriptions();
        return prescription;
    }

    public MedicalRequest addRequest(String recordId, String requestType) {
        MedicalRecord record = requireRecord(recordId);
        ValidationUtil.requireNonBlank(requestType, "Request type");
        MedicalRequest request = new MedicalRequest(nextRequestId(), requestType, new Date(), "PENDING");
        request.setDoctor(record.getDoctor());
        request.setMedicalRecord(record);
        record.addRequest(request);
        saveRequests();
        return request;
    }

    /**
     * Records a result against a Medical-Manager-defined AssessmentType, which is the
     * single source of truth for the category, grading rule, and charge - the value is
     * validated by AssessmentType.validateValue() rather than any check duplicated here.
     */
    public AssessmentResult addAssessmentResult(String recordId, String assessmentTypeId, String value, String grade) {
        MedicalRecord record = requireRecord(recordId);
        AssessmentType type = requireActiveAssessmentType(assessmentTypeId);
        type.validateValue(value);

        AssessmentResult result = new AssessmentResult();
        result.setResultId(nextResultId());
        result.setAssessmentType(type);
        result.setAssessmentName(type.getName());
        result.setValue(value);
        result.setGrade(grade);
        result.setResultDate(new Date());
        result.setRecordedBy(record.getDoctor());
        result.setMedicalRecord(record);
        record.addAssessmentResult(result);
        saveResults();
        return result;
    }

    private AssessmentType requireActiveAssessmentType(String assessmentTypeId) {
        AssessmentType type = typeManager.findById(assessmentTypeId);
        if (type == null) {
            throw new IllegalArgumentException("Assessment type not found: " + assessmentTypeId);
        }
        if (!type.isActive()) {
            throw new IllegalArgumentException("Assessment type is not active: " + type.getName());
        }
        return type;
    }

    public void finalizeRecord(String recordId) {
        MedicalRecord record = requireRecord(recordId);
        record.finalizeRecord();
        saveRecords();
        appointmentManager.persistStatusChange();
    }

    public Feedback submitFeedback(String patientId, String appointmentId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        Appointment appointment = appointmentManager.findById(appointmentId);
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment not found: " + appointmentId);
        }
        if (appointment.getPatient() == null || !patientId.equals(appointment.getPatient().getPatientId())) {
            throw new IllegalArgumentException("Only the patient of this appointment can leave feedback");
        }
        if (!"COMPLETED".equals(appointment.getStatus())) {
            throw new IllegalArgumentException("Feedback can only be left after the consultation is completed");
        }
        if (hasFeedbackForAppointment(appointmentId)) {
            throw new IllegalArgumentException("Feedback has already been submitted for this appointment");
        }
        Feedback feedback = new Feedback();
        feedback.setFeedbackId(nextFeedbackId());
        feedback.setAppointmentId(appointmentId);
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.setFeedbackDate(new Date());
        feedback.setPatient(appointment.getPatient());
        feedback.setDoctor(appointment.getDoctor());
        feedbackEntries.add(feedback);
        saveFeedback();
        return feedback;
    }

    public List<MedicalRecord> getPatientHistory(String patientId) {
        List<MedicalRecord> history = new ArrayList<>();
        for (MedicalRecord record : records) {
            if (record.getPatient() != null && patientId.equals(record.getPatient().getPatientId())
                    && record.getAppointment() != null && "COMPLETED".equals(record.getAppointment().getStatus())) {
                history.add(record);
            }
        }
        return history;
    }

    public List<Feedback> getDoctorFeedback(String doctorId) {
        List<Feedback> result = new ArrayList<>();
        for (Feedback feedback : feedbackEntries) {
            if (feedback.getDoctor() != null && doctorId.equals(feedback.getDoctor().getDoctorId())) {
                result.add(feedback);
            }
        }
        return result;
    }

    public List<MedicalRecord> getRecords() {
        return new ArrayList<>(records);
    }

    private boolean hasFeedbackForAppointment(String appointmentId) {
        for (Feedback feedback : feedbackEntries) {
            if (appointmentId.equals(feedback.getAppointmentId())) {
                return true;
            }
        }
        return false;
    }

    private MedicalRecord requireRecord(String recordId) {
        MedicalRecord record = findRecordById(recordId);
        if (record == null) {
            throw new IllegalArgumentException("Medical record not found: " + recordId);
        }
        return record;
    }

    private MedicalRecord findRecordById(String recordId) {
        for (MedicalRecord record : records) {
            if (record.getRecordId().equals(recordId)) {
                return record;
            }
        }
        return null;
    }

    private MedicalRecord findRecordByAppointmentId(String appointmentId) {
        for (MedicalRecord record : records) {
            if (record.getAppointment() != null && appointmentId.equals(record.getAppointment().getAppointmentId())) {
                return record;
            }
        }
        return null;
    }

    private String nextRecordId() {
        List<String> ids = new ArrayList<>();
        for (MedicalRecord record : records) {
            ids.add(record.getRecordId());
        }
        return IdGenerator.next("MR", ids);
    }

    private String nextPrescriptionId() {
        List<String> ids = new ArrayList<>();
        for (MedicalRecord record : records) {
            for (Prescription prescription : record.getPrescriptions()) {
                ids.add(prescription.getPrescriptionId());
            }
        }
        return IdGenerator.next("RX", ids);
    }

    private String nextRequestId() {
        List<String> ids = new ArrayList<>();
        for (MedicalRecord record : records) {
            for (MedicalRequest request : record.getMedicalRequests()) {
                ids.add(request.getRequestId());
            }
        }
        return IdGenerator.next("MQ", ids);
    }

    private String nextResultId() {
        List<String> ids = new ArrayList<>();
        for (MedicalRecord record : records) {
            for (AssessmentResult result : record.getAssessmentResults()) {
                ids.add(result.getResultId());
            }
        }
        return IdGenerator.next("AR", ids);
    }

    private String nextFeedbackId() {
        List<String> ids = new ArrayList<>();
        for (Feedback feedback : feedbackEntries) {
            ids.add(feedback.getFeedbackId());
        }
        return IdGenerator.next("FB", ids);
    }

    // ---- persistence: medical records ----

    private void loadRecords() {
        for (String line : TextFileUtil.readLines(recordsFile)) {
            String[] fields = line.split("\\|", -1);
            if (fields.length < 8) {
                System.err.println("Skipping malformed medical record: " + line);
                continue;
            }
            try {
                Appointment appointment = appointmentManager.findById(fields[1]);
                Patient patient = userManager.findPatientById(fields[2]);
                Doctor doctor = userManager.findDoctorById(fields[3]);
                if (appointment == null || patient == null || doctor == null) {
                    System.err.println("Skipping medical record with unknown reference: " + line);
                    continue;
                }
                MedicalRecord record = new MedicalRecord(
                        fields[0], fields[4], fields[5], fields[6], DATE_FORMAT.parse(fields[7]));
                record.setAppointment(appointment);
                record.setPatient(patient);
                record.setDoctor(doctor);
                appointment.setMedicalRecord(record);
                records.add(record);
            } catch (ParseException e) {
                System.err.println("Skipping medical record with invalid date: " + line);
            }
        }
    }

    private void saveRecords() {
        List<String> lines = new ArrayList<>();
        for (MedicalRecord record : records) {
            lines.add(String.join("|",
                    record.getRecordId(),
                    record.getAppointment().getAppointmentId(),
                    record.getPatient().getPatientId(),
                    record.getDoctor().getDoctorId(),
                    TextFileUtil.sanitize(record.getVitalSigns()),
                    TextFileUtil.sanitize(record.getConsultationNotes()),
                    TextFileUtil.sanitize(record.getDiagnosis()),
                    DATE_FORMAT.format(record.getRecordDate())));
        }
        TextFileUtil.writeLines(recordsFile, RECORDS_HEADER, lines);
    }

    // ---- persistence: prescriptions ----

    private void loadPrescriptions() {
        for (String line : TextFileUtil.readLines(prescriptionsFile)) {
            String[] fields = line.split("\\|", -1);
            if (fields.length < 8) {
                System.err.println("Skipping malformed prescription record: " + line);
                continue;
            }
            try {
                MedicalRecord record = findRecordById(fields[1]);
                if (record == null) {
                    System.err.println("Skipping prescription with unknown record: " + line);
                    continue;
                }
                Prescription prescription = new Prescription(
                        fields[0], fields[4], fields[5], fields[6], DATE_FORMAT.parse(fields[7]));
                prescription.setMedicalRecord(record);
                prescription.setDoctor(userManager.findDoctorById(fields[2]));
                prescription.setPatient(userManager.findPatientById(fields[3]));
                record.addPrescription(prescription);
            } catch (ParseException e) {
                System.err.println("Skipping prescription with invalid date: " + line);
            }
        }
    }

    private void savePrescriptions() {
        List<String> lines = new ArrayList<>();
        for (MedicalRecord record : records) {
            for (Prescription prescription : record.getPrescriptions()) {
                lines.add(String.join("|",
                        prescription.getPrescriptionId(),
                        record.getRecordId(),
                        prescription.getDoctor().getDoctorId(),
                        prescription.getPatient().getPatientId(),
                        TextFileUtil.sanitize(prescription.getMedicationDetails()),
                        TextFileUtil.sanitize(prescription.getDosage()),
                        TextFileUtil.sanitize(prescription.getInstructions()),
                        DATE_FORMAT.format(prescription.getPrescribedDate())));
            }
        }
        TextFileUtil.writeLines(prescriptionsFile, PRESCRIPTIONS_HEADER, lines);
    }

    // ---- persistence: medical requests ----

    private void loadRequests() {
        for (String line : TextFileUtil.readLines(requestsFile)) {
            String[] fields = line.split("\\|", -1);
            if (fields.length < 6) {
                System.err.println("Skipping malformed medical request: " + line);
                continue;
            }
            try {
                MedicalRecord record = findRecordById(fields[1]);
                if (record == null) {
                    System.err.println("Skipping medical request with unknown record: " + line);
                    continue;
                }
                MedicalRequest request = new MedicalRequest(fields[0], fields[3], DATE_FORMAT.parse(fields[4]), fields[5]);
                request.setDoctor(userManager.findDoctorById(fields[2]));
                request.setMedicalRecord(record);
                record.addRequest(request);
            } catch (ParseException e) {
                System.err.println("Skipping medical request with invalid date: " + line);
            }
        }
    }

    private void saveRequests() {
        List<String> lines = new ArrayList<>();
        for (MedicalRecord record : records) {
            for (MedicalRequest request : record.getMedicalRequests()) {
                lines.add(String.join("|",
                        request.getRequestId(),
                        record.getRecordId(),
                        request.getDoctor().getDoctorId(),
                        TextFileUtil.sanitize(request.getRequestType()),
                        DATE_FORMAT.format(request.getRequestDate()),
                        request.getStatus()));
            }
        }
        TextFileUtil.writeLines(requestsFile, REQUESTS_HEADER, lines);
    }

    // ---- persistence: assessment results ----

    private void loadResults() {
        for (String line : TextFileUtil.readLines(resultsFile)) {
            String[] fields = line.split("\\|", -1);
            if (fields.length < 7) {
                System.err.println("Skipping malformed assessment result: " + line);
                continue;
            }
            try {
                MedicalRecord record = findRecordById(fields[1]);
                if (record == null) {
                    System.err.println("Skipping assessment result with unknown record: " + line);
                    continue;
                }
                AssessmentResult result = new AssessmentResult(
                        fields[0], fields[2], fields[3], fields[4], DATE_FORMAT.parse(fields[5]));
                result.setRecordedBy(userManager.findDoctorById(fields[6]));
                result.setMedicalRecord(record);
                if (fields.length >= 8 && !fields[7].isEmpty()) {
                    result.setAssessmentType(typeManager.findById(fields[7]));
                }
                record.addAssessmentResult(result);
            } catch (ParseException e) {
                System.err.println("Skipping assessment result with invalid date: " + line);
            }
        }
    }

    private void saveResults() {
        List<String> lines = new ArrayList<>();
        for (MedicalRecord record : records) {
            for (AssessmentResult result : record.getAssessmentResults()) {
                lines.add(String.join("|",
                        result.getResultId(),
                        record.getRecordId(),
                        TextFileUtil.sanitize(result.getAssessmentName()),
                        TextFileUtil.sanitize(result.getValue()),
                        TextFileUtil.emptyIfNull(result.getGrade()),
                        DATE_FORMAT.format(result.getResultDate()),
                        result.getRecordedBy() == null ? "" : result.getRecordedBy().getDoctorId(),
                        result.getAssessmentType() == null ? "" : result.getAssessmentType().getAssessmentTypeId()));
            }
        }
        TextFileUtil.writeLines(resultsFile, RESULTS_HEADER, lines);
    }

    // ---- persistence: feedback ----

    private void loadFeedback() {
        for (String line : TextFileUtil.readLines(feedbackFile)) {
            String[] fields = line.split("\\|", -1);
            if (fields.length < 6) {
                System.err.println("Skipping malformed feedback record: " + line);
                continue;
            }
            try {
                Patient patient = userManager.findPatientById(fields[1]);
                Doctor doctor = userManager.findDoctorById(fields[2]);
                if (patient == null || doctor == null) {
                    System.err.println("Skipping feedback with unknown patient/doctor: " + line);
                    continue;
                }
                Feedback feedback = new Feedback();
                feedback.setFeedbackId(fields[0]);
                feedback.setRating(Integer.parseInt(fields[3]));
                feedback.setComment(fields[4]);
                feedback.setFeedbackDate(DATE_FORMAT.parse(fields[5]));
                feedback.setPatient(patient);
                feedback.setDoctor(doctor);
                if (fields.length >= 7 && !fields[6].isEmpty()) {
                    feedback.setAppointmentId(fields[6]);
                }
                feedbackEntries.add(feedback);
            } catch (ParseException | NumberFormatException e) {
                System.err.println("Skipping feedback with invalid data: " + line);
            }
        }
    }

    private void saveFeedback() {
        List<String> lines = new ArrayList<>();
        for (Feedback feedback : feedbackEntries) {
            lines.add(String.join("|",
                    feedback.getFeedbackId(),
                    feedback.getPatient().getPatientId(),
                    feedback.getDoctor().getDoctorId(),
                    String.valueOf(feedback.getRating()),
                    TextFileUtil.sanitize(feedback.getComment()),
                    DATE_FORMAT.format(feedback.getFeedbackDate()),
                    feedback.getAppointmentId() == null ? "" : feedback.getAppointmentId()));
        }
        TextFileUtil.writeLines(feedbackFile, FEEDBACK_HEADER, lines);
    }
}
