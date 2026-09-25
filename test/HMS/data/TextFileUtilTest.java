package hms.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Standalone, dependency-free Step 8 tests for TextFileUtil: safe atomic writes
 * (a failed write must never truncate or partially overwrite an existing valid
 * file) and checked reads (a missing file is a valid empty dataset, but an
 * unreadable file or a directory in its place is a load failure, not silently
 * an empty one). Same hand-rolled runner style as the manager test files,
 * since the project has no JUnit on its classpath. Every test works against
 * its own temporary directory (never the real data/ directory) and deletes it
 * afterward.
 *
 * Run with (from the project root, after compiling src to build/classes):
 *   javac -d build/test-classes -cp build/classes test/HMS/data/TextFileUtilTest.java
 *   java -cp build/classes;build/test-classes hms.data.TextFileUtilTest
 */
public class TextFileUtilTest {

    private int passed = 0;
    private int failed = 0;

    public static void main(String[] args) throws Exception {
        TextFileUtilTest suite = new TextFileUtilTest();

        suite.run("testWriteLinesCheckedWritesContentSuccessfully", suite::testWriteLinesCheckedWritesContentSuccessfully);
        suite.run("testWriteLinesCheckedCanReplaceExistingContent", suite::testWriteLinesCheckedCanReplaceExistingContent);
        suite.run("testWriteLinesCheckedDoesNotTouchExistingFileOnFailure",
                suite::testWriteLinesCheckedDoesNotTouchExistingFileOnFailure);
        suite.run("testWriteLinesCheckedLeavesNoStrayTempFileAfterFailure",
                suite::testWriteLinesCheckedLeavesNoStrayTempFileAfterFailure);
        suite.run("testWriteLinesCheckedPropagatesFailureWhenParentCannotBeADirectory",
                suite::testWriteLinesCheckedPropagatesFailureWhenParentCannotBeADirectory);
        suite.run("testWriteLinesSwallowsFailureWithoutCorruptingExistingFile",
                suite::testWriteLinesSwallowsFailureWithoutCorruptingExistingFile);
        suite.run("testReadLinesCheckedTreatsMissingFileAsEmptyList", suite::testReadLinesCheckedTreatsMissingFileAsEmptyList);
        suite.run("testReadLinesCheckedThrowsForDirectoryInPlaceOfFile",
                suite::testReadLinesCheckedThrowsForDirectoryInPlaceOfFile);
        suite.run("testReadLinesTreatsMissingFileAsEmptyList", suite::testReadLinesTreatsMissingFileAsEmptyList);
        suite.run("testReadLinesSwallowsDirectoryInPlaceOfFile", suite::testReadLinesSwallowsDirectoryInPlaceOfFile);

        System.out.println();
        System.out.println(suite.passed + " passed, " + suite.failed + " failed");
        if (suite.failed > 0) {
            System.exit(1);
        }
    }

    private interface TestCase {
        void run() throws Exception;
    }

    private void run(String name, TestCase test) {
        try {
            test.run();
            System.out.println("PASS  " + name);
            passed++;
        } catch (Throwable t) {
            System.out.println("FAIL  " + name + " - " + t.getMessage());
            failed++;
        }
    }

    // ---- assertions ----

    private static void assertEquals(Object expected, Object actual, String message) {
        boolean equal = expected == null ? actual == null : expected.equals(actual);
        if (!equal) {
            throw new AssertionError(message + " (expected <" + expected + "> but was <" + actual + ">)");
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static <T extends Exception> void assertThrows(Class<T> type, ThrowingRunnable action, String message) {
        try {
            action.run();
        } catch (Exception e) {
            if (type.isInstance(e)) {
                return;
            }
            throw new AssertionError(message + " (threw " + e.getClass().getSimpleName() + " instead)");
        }
        throw new AssertionError(message + " (nothing was thrown)");
    }

    /** Ordinary recursion instead of a stream pipeline: delete every file inside, then the folder itself. */
    private static void deleteRecursively(Path root) throws IOException {
        File folder = root.toFile();
        if (!folder.exists()) {
            return;
        }
        File[] children = folder.listFiles();
        if (children == null) {
            throw new IOException("Could not list directory contents: " + folder);
        }
        for (File child : children) {
            if (child.isDirectory()) {
                deleteRecursively(child.toPath());
            } else {
                Files.deleteIfExists(child.toPath());
            }
        }
        Files.deleteIfExists(root);
    }

    // ---- tests: safe writes ----

    private void testWriteLinesCheckedWritesContentSuccessfully() throws Exception {
        Path tempDir = Files.createTempDirectory("hms-textfileutil-test-");
        try {
            File target = new File(tempDir.toFile(), "data.txt");
            TextFileUtil.writeLinesChecked(target, "#header", Arrays.asList("a|b|c", "d|e|f"));
            List<String> content = Files.readAllLines(target.toPath(), StandardCharsets.UTF_8);
            assertEquals(Arrays.asList("#header", "a|b|c", "d|e|f"), content, "the file must contain the header and both lines");
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private void testWriteLinesCheckedCanReplaceExistingContent() throws Exception {
        Path tempDir = Files.createTempDirectory("hms-textfileutil-test-");
        try {
            File target = new File(tempDir.toFile(), "data.txt");
            TextFileUtil.writeLinesChecked(target, "#header", Arrays.asList("first|row"));
            TextFileUtil.writeLinesChecked(target, "#header", Arrays.asList("second|row"));
            List<String> content = Files.readAllLines(target.toPath(), StandardCharsets.UTF_8);
            assertEquals(Arrays.asList("#header", "second|row"), content, "a second write must fully replace the first");
        } finally {
            deleteRecursively(tempDir);
        }
    }

    /**
     * Reviewer requirement: a failed write must not truncate or partially overwrite an existing
     * valid data file. Holding the destination file open (a well-known Windows behavior: an open
     * file cannot be renamed or replaced by another handle) forces the temp-file-to-destination
     * replace step to fail, without ever letting writeLinesChecked open the destination itself.
     */
    private void testWriteLinesCheckedDoesNotTouchExistingFileOnFailure() throws Exception {
        Path tempDir = Files.createTempDirectory("hms-textfileutil-test-");
        try {
            File target = new File(tempDir.toFile(), "data.txt");
            TextFileUtil.writeLinesChecked(target, "#header", Arrays.asList("original|row"));
            List<String> originalContent = Files.readAllLines(target.toPath(), StandardCharsets.UTF_8);

            try (FileOutputStream lock = new FileOutputStream(target, true)) {
                assertThrows(IOException.class,
                        () -> TextFileUtil.writeLinesChecked(target, "#header", Arrays.asList("new|row")),
                        "writeLinesChecked must propagate a failure to replace a locked destination file");
            }

            List<String> contentAfterFailure = Files.readAllLines(target.toPath(), StandardCharsets.UTF_8);
            assertEquals(originalContent, contentAfterFailure,
                    "the existing valid file content must be completely unchanged after a failed write");
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private void testWriteLinesCheckedLeavesNoStrayTempFileAfterFailure() throws Exception {
        Path tempDir = Files.createTempDirectory("hms-textfileutil-test-");
        try {
            File target = new File(tempDir.toFile(), "data.txt");
            TextFileUtil.writeLinesChecked(target, "#header", Arrays.asList("original|row"));

            try (FileOutputStream lock = new FileOutputStream(target, true)) {
                assertThrows(IOException.class,
                        () -> TextFileUtil.writeLinesChecked(target, "#header", Arrays.asList("new|row")),
                        "the write must fail while the destination is locked");
            }

            File[] remainingFiles = tempDir.toFile().listFiles();
            assertTrue(remainingFiles != null && remainingFiles.length == 1,
                    "no leftover temporary file should remain after a failed write - found: "
                            + (remainingFiles == null ? "null" : Arrays.toString(remainingFiles)));
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private void testWriteLinesCheckedPropagatesFailureWhenParentCannotBeADirectory() throws Exception {
        Path tempDir = Files.createTempDirectory("hms-textfileutil-test-");
        try {
            // A regular file used as a "directory" makes any write beneath it fail - a portable
            // way to force an IOException without touching OS-specific permissions.
            File blocker = new File(tempDir.toFile(), "blocker");
            Files.write(blocker.toPath(), Arrays.asList("not a directory"), StandardCharsets.UTF_8);
            File unwritableTarget = new File(blocker, "data.txt");

            assertThrows(IOException.class,
                    () -> TextFileUtil.writeLinesChecked(unwritableTarget, "#header", Arrays.asList("a|b|c")),
                    "writeLinesChecked must propagate a write failure instead of hiding it");
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private void testWriteLinesSwallowsFailureWithoutCorruptingExistingFile() throws Exception {
        Path tempDir = Files.createTempDirectory("hms-textfileutil-test-");
        try {
            File target = new File(tempDir.toFile(), "data.txt");
            TextFileUtil.writeLinesChecked(target, "#header", Arrays.asList("original|row"));
            List<String> originalContent = Files.readAllLines(target.toPath(), StandardCharsets.UTF_8);

            try (FileOutputStream lock = new FileOutputStream(target, true)) {
                TextFileUtil.writeLines(target, "#header", Arrays.asList("new|row"));
            }

            List<String> contentAfterFailure = Files.readAllLines(target.toPath(), StandardCharsets.UTF_8);
            assertEquals(originalContent, contentAfterFailure,
                    "the legacy writeLines() must not throw, but must also not corrupt the existing file on failure");
        } finally {
            deleteRecursively(tempDir);
        }
    }

    // ---- tests: checked reads ----

    private void testReadLinesCheckedTreatsMissingFileAsEmptyList() throws Exception {
        Path tempDir = Files.createTempDirectory("hms-textfileutil-test-");
        try {
            File missing = new File(tempDir.toFile(), "does-not-exist.txt");
            List<String> lines = TextFileUtil.readLinesChecked(missing);
            assertTrue(lines.isEmpty(), "a missing file must load as an empty list, not an error");
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private void testReadLinesCheckedThrowsForDirectoryInPlaceOfFile() throws Exception {
        Path tempDir = Files.createTempDirectory("hms-textfileutil-test-");
        try {
            File directory = new File(tempDir.toFile(), "should-be-a-file");
            assertTrue(directory.mkdirs(), "test setup: could not create the directory standing in for the data file");
            assertThrows(IOException.class,
                    () -> TextFileUtil.readLinesChecked(directory),
                    "a directory in place of the expected data file must be reported as a read failure, not an empty dataset");
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private void testReadLinesTreatsMissingFileAsEmptyList() throws Exception {
        Path tempDir = Files.createTempDirectory("hms-textfileutil-test-");
        try {
            File missing = new File(tempDir.toFile(), "does-not-exist.txt");
            List<String> lines = TextFileUtil.readLines(missing);
            assertTrue(lines.isEmpty(), "the legacy readLines() must also treat a missing file as an empty list");
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private void testReadLinesSwallowsDirectoryInPlaceOfFile() throws Exception {
        Path tempDir = Files.createTempDirectory("hms-textfileutil-test-");
        try {
            File directory = new File(tempDir.toFile(), "should-be-a-file");
            assertTrue(directory.mkdirs(), "test setup: could not create the directory standing in for the data file");
            List<String> lines = TextFileUtil.readLines(directory);
            assertTrue(lines.isEmpty(),
                    "the legacy readLines() intentionally swallows the same failure and returns an empty list");
        } finally {
            deleteRecursively(tempDir);
        }
    }
}
