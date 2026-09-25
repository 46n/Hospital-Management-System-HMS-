package hms.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Small, shared field-level checks reused across manager classes, matching
 * the validation style already used inline in the model classes.
 */
public final class ValidationUtil {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^(?=.*[A-Za-z])[A-Za-z0-9_]{4,20}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{7,15}$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private ValidationUtil() {
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    public static void requireNonNegative(double value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
    }

    /**
     * A genuine, finite monetary amount: not NaN, not positive/negative infinity, and not
     * negative. NaN and infinity both fail a plain "value &lt; 0" check (NaN compares false to
     * everything, and positive infinity is not negative), so they need their own check here
     * rather than relying on requireNonNegative alone. Zero is accepted as valid - a rate may
     * legitimately be free of charge.
     */
    public static void requireValidAmount(double value, String fieldName) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(fieldName + " must be a finite number");
        }
        requireNonNegative(value, fieldName);
    }

    /**
     * A genuine percentage value: not NaN, not positive/negative infinity, and between 0 and 100
     * inclusive. NaN and infinity both fail a plain "0 &lt;= value &lt;= 100" check in different
     * ways (NaN compares false to every comparison, and infinity is always outside the range
     * anyway) - checking finiteness explicitly first gives a clearer error message than letting
     * NaN silently slip through a range check that never rejects it.
     */
    public static void requireValidPercentage(double value, String fieldName) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(fieldName + " must be a finite number");
        }
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException(fieldName + " must be between 0 and 100");
        }
    }

    /** Letters, digits, and underscores only, 4-20 characters, no spaces, at least one letter. */
    public static void requireValidUsername(String username, String fieldName) {
        requireNonBlank(username, fieldName);
        if (!USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new IllegalArgumentException(
                    fieldName + " must be 4-20 letters, digits, or underscores, with no spaces, "
                            + "and must include at least one letter");
        }
    }

    /** Basic "name@domain.tld" shape check - not a full RFC validator, just catches obvious typos. */
    public static void requireValidEmail(String email, String fieldName) {
        requireNonBlank(email, fieldName);
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException(fieldName + " must be a valid email address");
        }
    }

    /** 7-15 digits, with an optional leading +. */
    public static void requireValidPhone(String phone, String fieldName) {
        requireNonBlank(phone, fieldName);
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new IllegalArgumentException(
                    fieldName + " must be 7-15 digits, optionally starting with +");
        }
    }

    /**
     * Parses a yyyy-MM-dd date strictly: the whole string must match that exact shape, so
     * trailing characters (e.g. "2000-01-01junk") and non-canonical forms (e.g. "2000-1-1") are
     * rejected before parsing is even attempted, and the date itself must be a real calendar
     * date (lenient=false), so an impossible date like "2026-02-30" is rejected too, not
     * silently rolled over to March.
     */
    public static Date parseStrictDate(String text) throws ParseException {
        if (text == null || !DATE_PATTERN.matcher(text).matches()) {
            throw new ParseException("Date must be in yyyy-MM-dd format", 0);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setLenient(false);
        return format.parse(text);
    }
}
