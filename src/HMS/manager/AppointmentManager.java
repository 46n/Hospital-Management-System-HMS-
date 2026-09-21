package hms.manager;

import hms.data.TextFileUtil;
import hms.model.AdminStaff;
import hms.model.Appointment;
import hms.model.Doctor;
import hms.model.DoctorShift;
import hms.model.Patient;
import hms.util.IdGenerator;
import hms.util.ValidationUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** Coordinates appointment booking rules, backed by appointments.txt. */
public class AppointmentManager {

    private static final String DATA_FILE = "data/appointments.txt";
    private static final String HEADER = "#appointmentId|patientId|doctorId|appointmentDate|appointmentTime|status";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final List<Appointment> appointments = new ArrayList<>();
    private final UserManager userManager;
    private final DoctorShiftManager shiftManager;
    private final File dataFile;

    public AppointmentManager(UserManager userManager, DoctorShiftManager shiftManager) {
        this(userManager, shiftManager, new File(DATA_FILE));
    }

    public AppointmentManager(UserManager userManager, DoctorShiftManager shiftManager, File dataFile) {
        if (userManager == null) {
            throw new IllegalArgumentException("User manager is required");
        }
        if (shiftManager == null) {
            throw new IllegalArgumentException("Doctor shift manager is required");
        }
        this.userManager = userManager;
        this.shiftManager = shiftManager;
        this.dataFile = dataFile;
        load();
    }

    public Appointment book(String patientId, String doctorId, Date date, LocalTime time) {
        ValidationUtil.requireNonBlank(patientId, "Patient");
        ValidationUtil.requireNonBlank(doctorId, "Doctor");
        if (date == null || time == null) {
            throw new IllegalArgumentException("Appointment date and time are required");
        }
        Calendar todayMidnight = Calendar.getInstance();
        todayMidnight.set(Calendar.HOUR_OF_DAY, 0);
        todayMidnight.set(Calendar.MINUTE, 0);
        todayMidnight.set(Calendar.SECOND, 0);
        todayMidnight.set(Calendar.MILLISECOND, 0);
        if (date.before(todayMidnight.getTime())) {
            throw new IllegalArgumentException("Cannot book an appointment in the past");
        }
        Patient patient = userManager.findPatientById(patientId);
        if (patient == null) {
            throw new IllegalArgumentException("Patient not found: " + patientId);
        }
        Doctor doctor = userManager.findDoctorById(doctorId);
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor not found: " + doctorId);
        }
        if (!isWithinShift(doctorId, date, time)) {
            throw new IllegalArgumentException("Requested time is outside this doctor's working shift");
        }
        if (isSlotTaken(doctorId, date, time)) {
            throw new IllegalArgumentException("Doctor already has an appointment at that date and time");
        }
        if (hasConflict(patientId, date, time)) {
            throw new IllegalArgumentException("Patient already has an appointment at that date and time");
        }
        Appointment appointment = new Appointment(nextId(), date, time, "CONFIRMED");
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointments.add(appointment);
        patient.addAppointment(appointment);
        doctor.addAppointment(appointment);
        save();
        return appointment;
    }

    public void cancel(String appointmentId, String requestingUserId) {
        Appointment appointment = requireById(appointmentId);
        if (!isOwnedBy(appointment, requestingUserId)) {
            throw new IllegalArgumentException("Only the patient, doctor, or an admin can cancel this appointment");
        }
        rejectIfClosed(appointment);
        appointment.cancel();
        save();
    }

    public void reschedule(String appointmentId, Date newDate, LocalTime newTime) {
        Appointment appointment = requireById(appointmentId);
        rejectIfClosed(appointment);
        String doctorId = appointment.getDoctor().getDoctorId();
        if (!isWithinShift(doctorId, newDate, newTime)) {
            throw new IllegalArgumentException("Requested time is outside this doctor's working shift");
        }
        if (isSlotTaken(doctorId, newDate, newTime)) {
            throw new IllegalArgumentException("Doctor already has an appointment at that date and time");
        }
        appointment.reschedule(newDate, newTime);
        save();
    }

    public Appointment findById(String appointmentId) {
        if (appointmentId == null) {
            return null;
        }
        for (Appointment appointment : appointments) {
            if (appointmentId.equals(appointment.getAppointmentId())) {
                return appointment;
            }
        }
        return null;
    }

    public List<Appointment> listForPatient(String patientId) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment appointment : appointments) {
            if (appointment.getPatient() != null && patientId.equals(appointment.getPatient().getPatientId())) {
                result.add(appointment);
            }
        }
        return result;
    }

    public List<Appointment> listForDoctor(String doctorId) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment appointment : appointments) {
            if (appointment.getDoctor() != null && doctorId.equals(appointment.getDoctor().getDoctorId())) {
                result.add(appointment);
            }
        }
        return result;
    }

    public List<Appointment> getAppointments() {
        return new ArrayList<>(appointments);
    }

    /**
     * Re-writes appointments.txt to reflect an in-memory status change made
     * elsewhere (e.g. ClinicalManager marking an appointment COMPLETED when
     * its medical record is finalized).
     */
    public void persistStatusChange() {
        save();
    }

    private void rejectIfClosed(Appointment appointment) {
        if ("COMPLETED".equals(appointment.getStatus()) || "CANCELLED".equals(appointment.getStatus())) {
            throw new IllegalArgumentException(
                    "Cannot modify an appointment that is already " + appointment.getStatus().toLowerCase());
        }
    }

    private boolean isOwnedBy(Appointment appointment, String userId) {
        if (userId == null) {
            return false;
        }
        if (appointment.getPatient() != null && userId.equals(appointment.getPatient().getPatientId())) {
            return true;
        }
        if (appointment.getDoctor() != null && userId.equals(appointment.getDoctor().getDoctorId())) {
            return true;
        }
        return userManager.findById(userId) instanceof AdminStaff;
    }

    /** The source of truth for "is this doctor even working at this time" - reads DoctorShiftManager directly. */
    private boolean isWithinShift(String doctorId, Date date, LocalTime time) {
        for (DoctorShift shift : shiftManager.getShiftsForDoctor(doctorId)) {
            boolean sameDay = DATE_FORMAT.format(shift.getShiftDate()).equals(DATE_FORMAT.format(date));
            boolean withinHours = !time.isBefore(shift.getStartTime()) && time.isBefore(shift.getEndTime());
            if (sameDay && withinHours) {
                return true;
            }
        }
        return false;
    }

    private boolean isSlotTaken(String doctorId, Date date, LocalTime time) {
        for (Appointment appointment : appointments) {
            if (appointment.getDoctor() == null || !doctorId.equals(appointment.getDoctor().getDoctorId())) {
                continue;
            }
            if (!"CANCELLED".equals(appointment.getStatus()) && isSameSlot(appointment, date, time)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasConflict(String patientId, Date date, LocalTime time) {
        for (Appointment appointment : appointments) {
            if (appointment.getPatient() == null || !patientId.equals(appointment.getPatient().getPatientId())) {
                continue;
            }
            if (!"CANCELLED".equals(appointment.getStatus()) && isSameSlot(appointment, date, time)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSameSlot(Appointment appointment, Date date, LocalTime time) {
        return DATE_FORMAT.format(appointment.getAppointmentDate()).equals(DATE_FORMAT.format(date))
                && appointment.getAppointmentTime().equals(time);
    }

    private Appointment requireById(String appointmentId) {
        Appointment appointment = findById(appointmentId);
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment not found: " + appointmentId);
        }
        return appointment;
    }

    private String nextId() {
        List<String> ids = new ArrayList<>();
        for (Appointment appointment : appointments) {
            ids.add(appointment.getAppointmentId());
        }
        return IdGenerator.next("AP", ids);
    }

    private void load() {
        for (String line : TextFileUtil.readLines(dataFile)) {
            Appointment appointment = parseLine(line);
            if (appointment != null) {
                appointments.add(appointment);
            }
        }
    }

    private Appointment parseLine(String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length < 6) {
            System.err.println("Skipping malformed appointment record: " + line);
            return null;
        }
        try {
            Patient patient = userManager.findPatientById(fields[1]);
            Doctor doctor = userManager.findDoctorById(fields[2]);
            if (patient == null || doctor == null) {
                System.err.println("Skipping appointment with unknown patient/doctor: " + line);
                return null;
            }
            Date date = DATE_FORMAT.parse(fields[3]);
            LocalTime time = LocalTime.parse(fields[4], TIME_FORMAT);
            Appointment appointment = new Appointment(fields[0], date, time, fields[5]);
            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
            patient.addAppointment(appointment);
            doctor.addAppointment(appointment);
            return appointment;
        } catch (ParseException | DateTimeParseException e) {
            System.err.println("Skipping appointment with invalid date/time: " + line);
            return null;
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (Appointment appointment : appointments) {
            lines.add(toLine(appointment));
        }
        TextFileUtil.writeLines(dataFile, HEADER, lines);
    }

    private String toLine(Appointment appointment) {
        return String.join("|",
                appointment.getAppointmentId(),
                appointment.getPatient().getPatientId(),
                appointment.getDoctor().getDoctorId(),
                DATE_FORMAT.format(appointment.getAppointmentDate()),
                appointment.getAppointmentTime().format(TIME_FORMAT),
                appointment.getStatus());
    }
    public List<LocalTime> getAvailableSlots(
        String doctorId,
        Date selectedDate) {

    if (doctorId == null || doctorId.trim().isEmpty()) {
        throw new IllegalArgumentException(
                "Doctor is required."
        );
    }

    if (selectedDate == null) {
        throw new IllegalArgumentException(
                "Appointment date is required."
        );
    }

    final int SLOT_MINUTES = 30;

    LocalDate targetDate = selectedDate
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();

    if (targetDate.isBefore(LocalDate.now())) {
        throw new IllegalArgumentException(
                "Appointments cannot be booked in the past."
        );
    }

    List<LocalTime> availableSlots = new ArrayList<>();

    for (DoctorShift shift
            : shiftManager.getShiftsForDoctor(doctorId)) {

        LocalDate shiftDate = shift.getShiftDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        if (!shiftDate.equals(targetDate)) {
            continue;
        }

        LocalTime slot = roundUpToHalfHour(
                shift.getStartTime()
        );

        LocalTime end = shift.getEndTime();

        while (slot.isBefore(end)) {

            long remainingMinutes =
                    Duration.between(slot, end).toMinutes();

            if (remainingMinutes < SLOT_MINUTES) {
                break;
            }

            if (targetDate.equals(LocalDate.now())
                    && !slot.isAfter(LocalTime.now())) {

                slot = slot.plusMinutes(SLOT_MINUTES);
                continue;
            }

            boolean taken = false;

            for (Appointment appointment
                    : listForDoctor(doctorId)) {

                boolean cancelled =
                        appointment.getStatus() != null
                        && "CANCELLED".equalsIgnoreCase(
                                appointment.getStatus().toString()
                        );

                if (!cancelled
                        && isSameSlot(
                                appointment,
                                selectedDate,
                                slot)) {

                    taken = true;
                    break;
                }
            }

            if (!taken) {
                availableSlots.add(slot);
            }

            slot = slot.plusMinutes(SLOT_MINUTES);
        }
    }

    return availableSlots;
}
    private LocalTime roundUpToHalfHour(LocalTime time) {

    int minute = time.getMinute();

    if (minute == 0 || minute == 30) {
        return time.withSecond(0).withNano(0);
    }

    if (minute < 30) {
        return time.withMinute(30)
                .withSecond(0)
                .withNano(0);
    }

    return time.plusHours(1)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
}
}
