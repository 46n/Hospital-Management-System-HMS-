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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Coordinates consultation rate configuration, backed by consultation_rates.txt. */
public class ConsultationRateManager {

    private static final String DATA_FILE = "data/consultation_rates.txt";
    private static final String HEADER =
            "#rateId|consultationType|baseAmount|effectiveDate|active|configuredByStaffId";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final Pattern GENERATED_RATE_ID_PATTERN = Pattern.compile("^CR(\\d+)$");

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
        ValidationUtil.requireValidAmount(baseAmount, "Base amount");
        if (effectiveDate == null) {
            throw new IllegalArgumentException("Effective date is required");
        }
        AdminStaff staff = requireAdminStaff(configuredByStaffId);

        ConsultationRate rate = new ConsultationRate(
                IdGenerator.next("CR", collectIds()), consultationType.trim(), baseAmount, effectiveDate, true);
        rate.setConfiguredBy(staff);
        rates.add(rate);
        save();
        return rate;
    }

    /** The rate billing should use right now: equivalent to getCurrentActiveRate(new Date()). */
    public ConsultationRate getCurrentActiveRate() {
        return getCurrentActiveRate(new Date());
    }

    /**
     * The rate billing should use as of the given reference date: the active rate with the
     * latest effective date that is not after asOf. A future-effective or inactive rate is never
     * selected. If two or more eligible rates share the same latest effective date, the one with
     * the numerically greatest generated rate ID wins ("the most recently created rate of that
     * date"), so this is a fixed rule that never depends on list iteration order. See
     * isNewerRateId() for exactly how two rate IDs are compared.
     */
    public ConsultationRate getCurrentActiveRate(Date asOf) {
        if (asOf == null) {
            throw new IllegalArgumentException("Reference date is required");
        }
        ConsultationRate selected = null;
        for (ConsultationRate rate : rates) {
            if (!rate.isActive()) {
                continue;
            }
            if (rate.getEffectiveDate().after(asOf)) {
                continue;
            }
            if (selected == null) {
                selected = rate;
                continue;
            }
            int dateComparison = rate.getEffectiveDate().compareTo(selected.getEffectiveDate());
            if (dateComparison > 0) {
                selected = rate;
            } else if (dateComparison == 0 && isNewerRateId(rate.getRateId(), selected.getRateId())) {
                selected = rate;
            }
        }
        return selected;
    }

    /**
     * True if candidateId should win a same-effective-date tie-break against currentId. A
     * generated rate ID is "CR" followed by a plain incrementing number with no fixed width, so
     * "CR999" is followed by "CR1000" - not by a zero-padded "CR1000" of the same length as
     * "CR999". Comparing the numeric suffixes as numbers (not as text) is what makes CR1000
     * correctly outrank CR999; a plain string comparison would get this backwards, since the
     * character '1' sorts before '9'.
     *
     * If either ID does not have that exact "CR" + digits shape - e.g. legacy data loaded from a
     * file that used a different ID scheme, or a suffix too long to fit a long - this falls back
     * to an ordinary string comparison. That fallback is still a fixed, deterministic answer and
     * never throws; it just is not guaranteed to reflect creation order for IDs outside the
     * normal generated shape.
     */
    private static boolean isNewerRateId(String candidateId, String currentId) {
        Matcher candidateMatch = GENERATED_RATE_ID_PATTERN.matcher(candidateId);
        Matcher currentMatch = GENERATED_RATE_ID_PATTERN.matcher(currentId);
        if (candidateMatch.matches() && currentMatch.matches()) {
            try {
                long candidateNumber = Long.parseLong(candidateMatch.group(1));
                long currentNumber = Long.parseLong(currentMatch.group(1));
                return candidateNumber > currentNumber;
            } catch (NumberFormatException e) {
                // Suffix too long to fit a long - fall through to the string-based fallback below.
            }
        }
        return candidateId.compareTo(currentId) > 0;
    }

    public void updateRate(String rateId, String consultationType, double baseAmount, Date effectiveDate) {
        ConsultationRate rate = requireRate(rateId);
        ValidationUtil.requireNonBlank(consultationType, "Consultation type");
        ValidationUtil.requireValidAmount(baseAmount, "Base amount");
        if (effectiveDate == null) {
            throw new IllegalArgumentException("Effective date is required");
        }
        rate.setConsultationType(consultationType.trim());
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
        List<String> loadedIds = new ArrayList<>();
        for (String line : TextFileUtil.readLines(dataFile)) {
            ConsultationRate rate = parseLine(line, loadedIds);
            if (rate != null) {
                loadedIds.add(rate.getRateId());
                rates.add(rate);
            }
        }
    }

    private ConsultationRate parseLine(String line, List<String> loadedIds) {
        String[] fields = line.split("\\|", -1);
        if (fields.length != 6) {
            System.err.println("Skipping malformed consultation rate record: " + line);
            return null;
        }
        String rateId = fields[0].trim();
        String consultationType = fields[1].trim();
        String amountText = fields[2].trim();
        String dateText = fields[3].trim();
        String activeText = fields[4].trim();
        String configuredByStaffId = fields[5].trim();

        if (rateId.isEmpty()) {
            System.err.println("Skipping consultation rate with a blank rate ID: " + line);
            return null;
        }
        if (loadedIds.contains(rateId)) {
            System.err.println("Skipping duplicate consultation rate ID: " + line);
            return null;
        }
        if (consultationType.isEmpty()) {
            System.err.println("Skipping consultation rate with a blank consultation type: " + line);
            return null;
        }

        double baseAmount;
        try {
            baseAmount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            System.err.println("Skipping consultation rate with a non-numeric base amount \"" + amountText + "\": " + line);
            return null;
        }
        if (Double.isNaN(baseAmount) || Double.isInfinite(baseAmount) || baseAmount < 0) {
            System.err.println("Skipping consultation rate with an invalid base amount \"" + amountText + "\": " + line);
            return null;
        }

        Date effectiveDate;
        try {
            effectiveDate = ValidationUtil.parseStrictDate(dateText);
        } catch (ParseException e) {
            System.err.println("Skipping consultation rate with an invalid effective date \"" + dateText + "\": " + line);
            return null;
        }

        if (!activeText.equalsIgnoreCase("true") && !activeText.equalsIgnoreCase("false")) {
            System.err.println("Skipping consultation rate with an invalid active value \"" + activeText + "\": " + line);
            return null;
        }
        boolean active = Boolean.parseBoolean(activeText);

        AdminStaff configuredBy = null;
        if (!configuredByStaffId.isEmpty()) {
            User user = userManager.findById(configuredByStaffId);
            if (!(user instanceof AdminStaff)) {
                System.err.println("Skipping consultation rate with an unknown or non-admin configuring staff ID \""
                        + configuredByStaffId + "\": " + line);
                return null;
            }
            configuredBy = (AdminStaff) user;
        }

        ConsultationRate rate = new ConsultationRate(rateId, consultationType, baseAmount, effectiveDate, active);
        rate.setConfiguredBy(configuredBy);
        return rate;
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
