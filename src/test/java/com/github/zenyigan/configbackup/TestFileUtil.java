package com.github.zenyigan.configbackup;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestFileUtil {
  static void withTempDir(Consumer<File> action) {
    File dir = createTempDir();
    try {
      action.accept(dir);
    } finally {
      deleteRecursively(dir);
    }
  }

  static File createTempDir() {
    try {
      return Files.createTempDirectory("configbackup-").toFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static void writeFileContent(File f, int size) {
    try {
      byte[] data = new byte[size];
      new SecureRandom().nextBytes(data);

      try (FileOutputStream fos = new FileOutputStream(f)) {
        fos.write(data);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static void assertDirectoryContainsOnly(File directory, String... expectedFileNames) {
    assertDirectoryContainsOnly(directory, new HashSet<>(Arrays.asList(expectedFileNames)));
  }

  static void assertDirectoryContainsOnly(File directory, Set<String> expectedFileNames) {
    assertThat(directory.exists())
        .as("Expected file does not exist: %s", directory.getAbsolutePath())
        .isTrue();
    assertThat(directory.isDirectory())
        .as("Expected a directory but was: %s", directory.getAbsolutePath())
        .isTrue();

    String[] actualFileNames = directory.list();
    assertThat(actualFileNames)
        .as("Could not list files in directory: %s", directory.getAbsolutePath())
        .isNotNull();

    Set<String> actual = new HashSet<>(Arrays.asList(actualFileNames));

    assertThat(actual)
        .withFailMessage(
            "Expected %s to contain only %s but was %s",
            directory.getAbsolutePath(), expectedFileNames, actual)
        .isEqualTo(expectedFileNames);
  }

  public static void assertFileContentEquals(File actual, File expected) {
    try {
      byte[] expectedBytes = Files.readAllBytes(expected.toPath());
      byte[] actualBytes = Files.readAllBytes(actual.toPath());

      assertThat(actualBytes)
          .as(
              "File contents differ: expected=%s, actual=%s",
              expected.getAbsolutePath(), actual.getAbsolutePath())
          .isEqualTo(expectedBytes);

    } catch (IOException e) {
      throw new RuntimeException("Could not compare files", e);
    }
  }

  public static List<Path> listRelativePaths(File dir) throws IOException {
    try (Stream<Path> stream = Files.walk(dir.toPath())) {
      return stream
          .filter(p -> !p.equals(dir.toPath()))
          .map(p -> dir.toPath().relativize(p))
          .sorted(Comparator.naturalOrder())
          .collect(Collectors.toList());
    }
  }

  public static void assertDirectoriesEqual(File actualDir, File expectedDir) {
    assertThat(expectedDir.isDirectory())
        .as("Expected a directory but was: %s", expectedDir)
        .isTrue();
    assertThat(actualDir.isDirectory()).as("Expected a directory but was: %s", actualDir).isTrue();

    try {
      List<Path> expectedPaths = listRelativePaths(expectedDir);
      List<Path> actualPaths = listRelativePaths(actualDir);

      assertThat(actualPaths)
          .as(
              "Directory structure differs between %s and %s",
              expectedDir.getAbsolutePath(), actualDir.getAbsolutePath())
          .isEqualTo(expectedPaths);

      for (Path relPath : expectedPaths) {
        File expectedFile = expectedDir.toPath().resolve(relPath).toFile();
        File actualFile = actualDir.toPath().resolve(relPath).toFile();
        if (expectedFile.isFile() != actualFile.isFile()) {
          throw new AssertionError(
              "File type mismatch at "
                  + relPath
                  + " (expected: "
                  + typeOf(expectedFile)
                  + ", actual: "
                  + typeOf(actualFile)
                  + ")");
        }
        if (expectedFile.isFile()) {
          assertFileContentEquals(actualFile, expectedFile);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not compare directories", e);
    }
  }

  private static String typeOf(File f) {
    return f.isDirectory() ? "directory" : "file";
  }

  static void deleteRecursively(File f) {
    if (f == null) throw new IllegalArgumentException("f must not be null");
    if (f.isDirectory()) {
      File[] children = f.listFiles();
      if (children != null) {
        for (File child : children) {
          deleteRecursively(child);
        }
      }
    }
    if (!f.delete()) {
      throw new RuntimeException("Error deleting " + f.getAbsolutePath());
    }
  }
}
