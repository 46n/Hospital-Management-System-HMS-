package hms.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Mechanical text-file reading and writing shared by every manager class.
 * Knows nothing about hospital entities; managers do the line parsing.
 */
public final class TextFileUtil {

    private TextFileUtil() {
    }

    /**
     * Reads every non-blank, non-comment line from a file.
     * A missing file is treated as "no records yet" rather than an error.
     */
    public static List<String> readLines(File file) {
        List<String> lines = new ArrayList<>();
        if (file == null || !file.exists()) {
            return lines;
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Could not read file " + file.getPath() + ": " + e.getMessage());
        }
        return lines;
    }

    /**
     * Overwrites a file with a header comment followed by one line per record.
     * Creates the parent data directory if it does not exist yet.
     */
    public static void writeLines(File file, String header, List<String> lines) {
        if (file == null) {
            throw new IllegalArgumentException("File is required");
        }
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            if (header != null && !header.isEmpty()) {
                writer.write(header);
                writer.newLine();
            }
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Could not save file " + file.getPath() + ": " + e.getMessage());
        }
    }

    /**
     * Keeps free-text fields on a single line and out of the pipe delimiter's way.
     */
    public static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", "/").replace("\r", " ").replace("\n", " ").trim();
    }

    public static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }
}
