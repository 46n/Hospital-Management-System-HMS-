package hms.manager;

import hms.data.TextFileUtil;
import hms.model.AdminStaff;
import hms.model.ConsultationRate;
import hms.model.User;
import hms.util.IdGenerator;
import hms.util.ValidationUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Coordinates consultation rate configuration, backed by consultation_rates.txt. */
public class ConsultationRateManager {

    private static final String DATA_FILE = "data/consultation_rates.txt";
    private static final String HEADER =
            "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final List<ConsultationRate> rates = new ArrayList<>();
    private final UserManager userManager;
    private final File dataFile;

    public ConsultationRateManager(UserManager userManager) {
        this(userManager, new File(DATA_FILE));
    }

    public ConsultationRateManager(UserManager userManager, File dataFile) {
        if (userManager == null) {
            throw new IllegalArgumentException("User manager is required");
        }
        this.userManager = userManager;
        this.dataFile = dataFile;
        load();
    }

    public ConsultationRate createRate(String consultationType, double baseAmount, Date effectiveDate,
            String configuredByStaffId) {
        ValidationUtil.requireNonBlank(consultationType, "Consultation type");
        ValidationUtil.requireNonNegative(baseAmount, "Base amount");
        if (effectiveDate == null) {
            throw new IllegalArgumentException("Effective date is required");
        }
        AdminStaff staff = requireAdminStaff(configuredByStaffId);

        ConsultationRate rate = new ConsultationRate(
                IdGenerator.next("CR", collectIds()), consultationType, baseAmount, effectiveDate, true);
        rate.setConfiguredBy(staff);
        rates.add(rate);
        save();
        return rate;
    }
    public ConsultationRate getCurrentActiveRate() {

    ConsultationRate selected = null;

    for (ConsultationRate rate : rates) {

        if (!rate.isActive()) {
            continue;
        }

        if (rate.getEffectiveDate().after(new Date())) {
            continue;
        }

        if (selected == null
                || rate.getEffectiveDate()
                        .after(selected.getEffectiveDate())) {

            selected = rate;
        }
    }

    return selected;
}

    public void updateRate(String rateId, String consultationType, double baseAmount, Date effectiveDate) {
        ConsultationRate rate = requireRate(rateId);
        ValidationUtil.requireNonBlank(consultationType, "Consultation type");
        ValidationUtil.requireNonNegative(baseAmount, "Base amount");
        if (effectiveDate == null) {
            throw new IllegalArgumentException("Effective date is required");
        }
        rate.setConsultationType(consultationType);
        rate.setBaseAmount(baseAmount);
        rate.setEffectiveDate(effectiveDate);
        save();
    }

    public void setActive(String rateId, boolean active) {
        ConsultationRate rate = requireRate(rateId);
        rate.setActive(active);
        save();
    }

    public void deleteRate(String rateId) {
        ConsultationRate rate = requireRate(rateId);
        rates.remove(rate);
        save();
    }

    public ConsultationRate findById(String rateId) {
        if (rateId == null) {
            return null;
        }
        for (ConsultationRate rate : rates) {
            if (rateId.equals(rate.getRateId())) {
                return rate;
            }
        }
        return null;
    }

    public List<ConsultationRate> getRates() {
        return new ArrayList<>(rates);
    }

    private AdminStaff requireAdminStaff(String staffId) {
        User user = userManager.findById(staffId);
        if (!(user instanceof AdminStaff)) {
            throw new IllegalArgumentException("Admin Staff not found: " + staffId);
        }
        return (AdminStaff) user;
    }

    private ConsultationRate requireRate(String rateId) {
        ConsultationRate rate = findById(rateId);
        if (rate == null) {
            throw new IllegalArgumentException("Consultation rate not found: " + rateId);
        }
        return rate;
    }

    private List<String> collectIds() {
        List<String> ids = new ArrayList<>();
        for (ConsultationRate rate : rates) {
            ids.add(rate.getRateId());
        }
        return ids;
    }

    private void load() {
        for (String line : TextFileUtil.readLines(dataFile)) {
            ConsultationRate rate = parseLine(line);
            if (rate != null) {
                rates.add(rate);
            }
        }
    }

    private ConsultationRate parseLine(String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length < 6) {
            System.err.println("Skipping malformed consultation rate record: " + line);
            return null;
        }
        try {
            ConsultationRate rate = new ConsultationRate(fields[0], fields[1],
                    Double.parseDouble(fields[2]), DATE_FORMAT.parse(fields[3]), Boolean.parseBoolean(fields[4]));
            if (!fields[5].isEmpty()) {
                User user = userManager.findById(fields[5]);
                if (user instanceof AdminStaff) {
                    rate.setConfiguredBy((AdminStaff) user);
                }
            }
            return rate;
        } catch (ParseException | NumberFormatException e) {
            System.err.println("Skipping consultation rate record with invalid data: " + line);
            return null;
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (ConsultationRate rate : rates) {
            lines.add(toLine(rate));
        }
        TextFileUtil.writeLines(dataFile, HEADER, lines);
    }

    private String toLine(ConsultationRate rate) {
        return String.join("|",
                rate.getRateId(),
                TextFileUtil.emptyIfNull(rate.getConsultationType()),
                String.valueOf(rate.getBaseAmount()),
                DATE_FORMAT.format(rate.getEffectiveDate()),
                String.valueOf(rate.isActive()),
                rate.getConfiguredBy() == null ? "" : rate.getConfiguredBy().getStaffId());
    }
}
