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
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
     * Reads every non-blank, non-comment line from a file. A missing file is treated as "no
     * records yet" rather than an error. Any other read failure is swallowed the same way -
     * legacy behavior kept only for the older managers that already relied on it. Admin managers
     * must use {@link #readLinesChecked} instead, so an unreadable data file is never mistaken
     * for an empty one.
     */
    public static List<String> readLines(File file) {
        try {
            return readLinesChecked(file);
        } catch (IOException e) {
            System.err.println("Could not read file " + file.getPath() + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Same as {@link #readLines}, but distinguishes "no file yet" (an empty list) from "a file is
     * there but could not be read" (throws IOException) - a directory in the file's place counts
     * as the latter.
     */
    public static List<String> readLinesChecked(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        if (file == null || !file.exists()) {
            return lines;
        }
        if (file.isDirectory()) {
            throw new IOException("Expected a data file but found a directory: " + file.getPath());
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
        }
        return lines;
    }

    /**
     * Overwrites a file with a header comment followed by one line per record. Swallows any
     * write failure - legacy behavior kept only for the older managers that already relied on it.
     * Admin managers must use {@link #writeLinesChecked} instead, so a caller can roll back an
     * in-memory change that never actually got saved.
     */
    public static void writeLines(File file, String header, List<String> lines) {
        try {
            writeLinesChecked(file, header, lines);
        } catch (IOException e) {
            System.err.println("Could not save file " + (file == null ? "null" : file.getPath()) + ": " + e.getMessage());
        }
    }

    /**
     * Same as {@link #writeLines}, but rethrows the write failure instead of swallowing it, so a
     * caller that must not report success when the file was not actually saved can catch it.
     *
     * Safe replacement, step by step:
     * 1. Write the complete new content to a temporary sibling file.
     * 2. Only once that write fully succeeds, move the temp file over the real destination.
     * 3. If anything above fails, delete the temp file and leave the destination untouched.
     * The destination is never opened for writing directly, so a write that fails partway through
     * can never truncate or corrupt it - the old file survives exactly as it was.
     */
    public static void writeLinesChecked(File file, String header, List<String> lines) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File is required");
        }
        File parent = file.getParentFile();
        ensureDirectoryExists(parent);

        File tempFile = new File(parent, file.getName() + ".tmp" + System.nanoTime());
        try {
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
                if (header != null && !header.isEmpty()) {
                    writer.write(header);
                    writer.newLine();
                }
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            replaceWithTempFile(tempFile, file);
        } catch (IOException e) {
            Files.deleteIfExists(tempFile.toPath());
            throw e;
        }
    }

    /** Creates the parent data directory if it does not exist yet, and checks that it worked. */
    private static void ensureDirectoryExists(File directory) throws IOException {
        if (directory == null || directory.exists()) {
            return;
        }
        boolean created = directory.mkdirs();
        if (!created && !directory.exists()) {
            throw new IOException("Could not create directory: " + directory.getPath());
        }
    }

    /** Moves the completed temp file over the destination, preferring an atomic move when available. */
    private static void replaceWithTempFile(File tempFile, File destination) throws IOException {
        try {
            Files.move(tempFile.toPath(), destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
