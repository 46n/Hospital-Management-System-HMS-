package hms.manager;

import hms.data.TextFileUtil;
import hms.model.Doctor;
import hms.model.DoctorShift;
import hms.model.MedicalManager;
import hms.model.User;
import hms.util.IdGenerator;
import java.time.LocalDate;
import java.time.ZoneId;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Coordinates doctor shift/roster scheduling, backed by doctor_shifts.txt.
 * A Medical Manager may only schedule shifts for doctors assigned to them.
 */
public class DoctorShiftManager {

    private static final String DATA_FILE = "data/doctor_shifts.txt";
    private static final String HEADER = "#shiftId|doctorId|shiftDate|startTime|endTime|createdByManagerId";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final double HIGH_WORKLOAD_HOURS = 40.0;

    private final List<DoctorShift> shifts = new ArrayList<>();
    private final UserManager userManager;
    private final File dataFile;

    public DoctorShiftManager(UserManager userManager) {
        this(userManager, new File(DATA_FILE));
    }

    public DoctorShiftManager(UserManager userManager, File dataFile) {
        if (userManager == null) {
            throw new IllegalArgumentException("User manager is required");
        }
        this.userManager = userManager;
        this.dataFile = dataFile;
        load();
    }
    
    
    private void validateShiftDateTime(Date shiftDate, LocalTime startTime) {
    if (shiftDate == null) {
        throw new IllegalArgumentException("Shift date is required");
    }if (startTime == null) {throw new IllegalArgumentException("Start time is required");
    }
    LocalDate selectedDate = shiftDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
    LocalDateTime selectedDateTime =
            LocalDateTime.of(selectedDate, startTime);
    LocalDateTime now = LocalDateTime.now();
    if (selectedDateTime.isBefore(now)) {
        throw new IllegalArgumentException(
                "Shift start date and time cannot be in the past"
        );
    }
}

    public DoctorShift createShift(String managerId, String doctorId, Date shiftDate,
            LocalTime startTime, LocalTime endTime) {
            MedicalManager manager = requireMedicalManager(managerId);
            Doctor doctor = requireSupervisedDoctor(manager, doctorId);
            validateShiftDateTime(shiftDate, startTime);
            validateTimes(startTime, endTime);
        if (hasConflict(doctorId, shiftDate, startTime, endTime, null)) {
            throw new IllegalArgumentException("This doctor already has a shift that overlaps that time");
        }
        
        DoctorShift shift = new DoctorShift(IdGenerator.next("SH", collectIds()), shiftDate, startTime, endTime);
        shift.setDoctor(doctor);
        shift.setCreatedBy(manager);
        shifts.add(shift);
        save();
        return shift;
    }

    public void updateShift( String shiftId,Date shiftDate,LocalTime startTime, LocalTime endTime) {
    DoctorShift shift = requireShift(shiftId);
    validateShiftDateTime(shiftDate, startTime);
    validateTimes(startTime, endTime);
    if (hasConflict(shift.getDoctor().getDoctorId(),shiftDate,startTime,endTime,shiftId)) {
        throw new IllegalArgumentException( "This doctor already has a shift that overlaps that time" );
    }
    shift.setShiftDate(shiftDate);
    shift.setStartTime(startTime);
    shift.setEndTime(endTime);
    save();
}
    

    public void removeShift(String shiftId) {
        DoctorShift shift = requireShift(shiftId);
        shifts.remove(shift);
        save();
    }

    public DoctorShift findById(String shiftId) {
        if (shiftId == null) {
            return null;
        }
        for (DoctorShift shift : shifts) {
            if (shiftId.equals(shift.getShiftId())) {
                return shift;
            }
        }
        return null;
    }

    public List<DoctorShift> getShiftsForDoctor(String doctorId) {
        List<DoctorShift> result = new ArrayList<>();
        for (DoctorShift shift : shifts) {
            if (shift.getDoctor() != null && doctorId.equals(shift.getDoctor().getDoctorId())) {
                result.add(shift);
            }
        }
        return result;
    }

    public List<DoctorShift> getShiftsForManager(String managerId) {
        List<DoctorShift> result = new ArrayList<>();
        for (DoctorShift shift : shifts) {
            Doctor doctor = shift.getDoctor();
            if (doctor != null && doctor.getShiftManager() != null
                    && managerId.equals(doctor.getShiftManager().getManagerId())) {
                result.add(shift);
            }
        }
        return result;
    }
    
    public List<Doctor> getManagedDoctors(String managerId) {

    MedicalManager manager =
            requireMedicalManager(managerId);

    return new ArrayList<>(
            manager.getManagedDoctors()
    );
}


    public int getUpcomingShiftCount(
            String doctorId,
            int days) {

        return getUpcomingShifts(
                doctorId,
                days
        ).size();
    }


    public double getUpcomingShiftHours(
            String doctorId,
            int days) {

        long totalMinutes = 0;

        for (DoctorShift shift :
                getUpcomingShifts(doctorId, days)) {

            if (shift.getStartTime() == null
                    || shift.getEndTime() == null) {
                continue;
            }

            if (!shift.getStartTime()
                    .isBefore(shift.getEndTime())) {
                continue;
            }

            totalMinutes += java.time.Duration.between(
                    shift.getStartTime(),
                    shift.getEndTime()
            ).toMinutes();
        }

        double hours = totalMinutes / 60.0;

        return Math.round(hours * 10.0) / 10.0;
    }

    public String getWorkloadStatus(
            String doctorId,
            int days) {
        int shiftCount =
                getUpcomingShiftCount(
                        doctorId,
                        days
                );
        if (shiftCount == 0) {
            return "No Shift";
        }
        double hours =
                getUpcomingShiftHours(
                        doctorId,
                        days
                );
        if (hours > HIGH_WORKLOAD_HOURS) {
            return "High Workload";
        }
        return "Normal";
    }


    public List<String> getSchedulingAlerts(
            String managerId,
            int days) {
        List<String> alerts =
                new ArrayList<>();
        for (Doctor doctor :
                getManagedDoctors(managerId)) {
            if (!doctor.isActive()) {
                continue;
            }
            String status =
                    getWorkloadStatus(
                            doctor.getDoctorId(),
                            days
                    );
            if ("No Shift".equals(status)) {
                alerts.add(doctor.getDoctorId() + " - " + doctor.getUsername() + " has no upcoming shifts " + "in the next " + days+ " days."
                );
            } else if ("High Workload"
                    .equals(status)) {
                double hours =
                        getUpcomingShiftHours(
                                doctor.getDoctorId(),
                                days
                        );
                alerts.add( doctor.getDoctorId()  + " - "  + doctor.getUsername()+ " has a high scheduled workload of " 
                        + hours + " hours in the next " + days + " days."
                );
            }
        }

        return alerts;
    }


    private List<DoctorShift> getUpcomingShifts(
            String doctorId,
            int days) {

        if (days <= 0) {
            throw new IllegalArgumentException(
                    "Monitoring period must be greater than zero"
            );
        }

        Doctor doctor =
                userManager.findDoctorById(doctorId);

        if (doctor == null) {
            throw new IllegalArgumentException(
                    "Doctor not found: " + doctorId
            );
        }

        LocalDate startDate =
                LocalDate.now();

        LocalDate endDate =
                startDate.plusDays(days);

        List<DoctorShift> result =
                new ArrayList<>();

        for (DoctorShift shift :
                getShiftsForDoctor(doctorId)) {

            if (shift.getShiftDate() == null) {
                continue;
            }

            LocalDate shiftDate =
                    shift.getShiftDate()
                            .toInstant()
                            .atZone(
                                    ZoneId.systemDefault()
                            )
                            .toLocalDate();

            boolean withinPeriod =
                    !shiftDate.isBefore(startDate)
                    && shiftDate.isBefore(endDate);

            if (withinPeriod) {
                result.add(shift);
            }
        }

        return result;
    }

    private void validateTimes(LocalTime startTime, LocalTime endTime) {
    if (startTime == null || endTime == null) {
        throw new IllegalArgumentException(
                "Start and end time are required"
        );
    }
    if (!startTime.isBefore(endTime)) {
        throw new IllegalArgumentException(
                "End time must be after start time"
        );
    }
    long durationMinutes =
            java.time.Duration
                    .between(startTime, endTime)
                    .toMinutes();
    if (durationMinutes < 30) {
        throw new IllegalArgumentException(
                "Doctor shift must be at least 30 minutes"
        );
    }
    if (durationMinutes > 8 * 60) {
        throw new IllegalArgumentException(
                "Doctor shift cannot exceed 8 hours"
        );
    }
}

    private boolean hasConflict(String doctorId, Date shiftDate, LocalTime startTime, LocalTime endTime,
            String excludingShiftId) {
        String targetDateKey = DATE_FORMAT.format(shiftDate);
        for (DoctorShift shift : shifts) {
            if (shift.getShiftId().equals(excludingShiftId)) {
                continue;
            }
            if (shift.getDoctor() == null || !doctorId.equals(shift.getDoctor().getDoctorId())) {
                continue;
            }
            if (!DATE_FORMAT.format(shift.getShiftDate()).equals(targetDateKey)) {
                continue;
            }
            boolean overlaps = startTime.isBefore(shift.getEndTime()) && shift.getStartTime().isBefore(endTime);
            if (overlaps) {
                return true;
            }
        }
        return false;
    }

    private MedicalManager requireMedicalManager(String managerId) {
        User user = userManager.findById(managerId);
        if (!(user instanceof MedicalManager)) {
            throw new IllegalArgumentException("Medical Manager not found: " + managerId);
        }
        return (MedicalManager) user;
    }

    private Doctor requireSupervisedDoctor(MedicalManager manager, String doctorId) {
        Doctor doctor = userManager.findDoctorById(doctorId);
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor not found: " + doctorId);
        }
        if (doctor.getShiftManager() == null
                || !manager.getManagerId().equals(doctor.getShiftManager().getManagerId())) {
            throw new IllegalArgumentException("Doctor is not assigned to this Medical Manager");
        }
        return doctor;
    }

    private DoctorShift requireShift(String shiftId) {
        DoctorShift shift = findById(shiftId);
        if (shift == null) {
            throw new IllegalArgumentException("Shift not found: " + shiftId);
        }
        return shift;
    }

    private List<String> collectIds() {
        List<String> ids = new ArrayList<>();
        for (DoctorShift shift : shifts) {
            ids.add(shift.getShiftId());
        }
        return ids;
    }

    private void load() {
        for (String line : TextFileUtil.readLines(dataFile)) {
            DoctorShift shift = parseLine(line);
            if (shift != null) {
                shifts.add(shift);
            }
        }
    }

    private DoctorShift parseLine(String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length < 6) {
            System.err.println("Skipping malformed doctor shift record: " + line);
            return null;
        }
        try {
            Doctor doctor = userManager.findDoctorById(fields[1]);
            if (doctor == null) {
                System.err.println("Skipping doctor shift with unknown doctor: " + line);
                return null;
            }
            Date shiftDate = DATE_FORMAT.parse(fields[2]);
            LocalTime startTime = LocalTime.parse(fields[3], TIME_FORMAT);
            LocalTime endTime = LocalTime.parse(fields[4], TIME_FORMAT);
            DoctorShift shift = new DoctorShift(fields[0], shiftDate, startTime, endTime);
            shift.setDoctor(doctor);
            if (!fields[5].isEmpty()) {
                User user = userManager.findById(fields[5]);
                if (user instanceof MedicalManager) {
                    shift.setCreatedBy((MedicalManager) user);
                }
            }
            return shift;
        } catch (ParseException | DateTimeParseException e) {
            System.err.println("Skipping doctor shift with invalid date/time: " + line);
            return null;
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (DoctorShift shift : shifts) {
            lines.add(toLine(shift));
        }
        TextFileUtil.writeLines(dataFile, HEADER, lines);
    }

    private String toLine(DoctorShift shift) {
        return String.join("|",
                shift.getShiftId(),
                shift.getDoctor().getDoctorId(),
                DATE_FORMAT.format(shift.getShiftDate()),
                shift.getStartTime().format(TIME_FORMAT),
                shift.getEndTime().format(TIME_FORMAT),
                shift.getCreatedBy() == null ? "" : shift.getCreatedBy().getManagerId());
    }
}
