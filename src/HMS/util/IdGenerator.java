package hms.util;

import java.util.List;

/**
 * Generates short, readable, prefixed IDs (e.g. PT001) by scanning the
 * highest existing suffix for a prefix and incrementing it.
 */
public final class IdGenerator {

    private IdGenerator() {
    }

    public static String next(String prefix, List<String> existingIds) {
        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("Prefix is required");
        }
        int max = 0;
        if (existingIds != null) {
            for (String id : existingIds) {
                if (id == null || !id.startsWith(prefix)) {
                    continue;
                }
                try {
                    int value = Integer.parseInt(id.substring(prefix.length()));
                    if (value > max) {
                        max = value;
                    }
                } catch (NumberFormatException ignored) {
                    // Non-numeric suffix; not one of ours, skip it.
                }
            }
        }
        return prefix + String.format("%03d", max + 1);
    }
}
