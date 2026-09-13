package hms.util;

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
}
