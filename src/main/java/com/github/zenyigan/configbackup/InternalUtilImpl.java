package com.github.zenyigan.configbackup;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class InternalUtilImpl implements InternalUtil {
  private static final String TIME_PATTERN = "yyyyMMdd-HHmmss-SSS";
  private static final DateTimeFormatter timeFormatter =
      DateTimeFormatter.ofPattern(TIME_PATTERN).withZone(ZoneOffset.UTC);

  @Override
  public void copy(File source, File destination) {
    if (!source.exists()) {
      throw new ConfigBackupException("Source does not exist: " + source);
    }
    if (source.isFile()) {
      copyFile(source, destination);
    } else {
      if (source.isDirectory()) {
        copyDirectory(source, destination);
      } else {
        throw new ConfigBackupException(
            "This source cannot be detected as file or directory: " + source);
      }
    }
  }

  @Override
  public void delete(File source) {
    deleteRecursively(source);
  }

  @Override
  public File defaultDestinationRoot() {
    String home = System.getProperty("user.home");
    if (home == null) {
      throw new ConfigBackupException("Property user.home is not set");
    }
    File homeDir = new File(home);
    if (!homeDir.exists() || !homeDir.isDirectory()) {
      throw new ConfigBackupException(
          "Directory for user.home does not exist: " + homeDir.getAbsolutePath());
    }
    return new File(homeDir, ".configbackup");
  }

  @Override
  public String nowUtcName() {
    return utcName(Instant.now());
  }

  @Override
  public String utcName(Instant instant) {
    return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).format(timeFormatter);
  }

  @Override
  public Instant parseUtcName(String timestamp) {
    try {
      return timeFormatter.parse(timestamp, Instant::from);
    } catch (DateTimeException e) {
      throw new ConfigBackupException(
          "Invalid timestamp format: '" + timestamp + "'. Expected pattern: " + TIME_PATTERN, e);
    }
  }

  private void deleteRecursively(File f) {
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
      throw new ConfigBackupException("Error deleting " + f.getAbsolutePath());
    }
  }

  private void copyFile(File source, File destination) {
    try {
      Path srcPath = source.toPath();
      Path destPath = destination.toPath();

      if (Files.exists(destPath)) {
        throw new ConfigBackupException("Destination already exists: " + destPath);
      }

      Files.copy(srcPath, destPath);
    } catch (IOException e) {
      throw new ConfigBackupException(
          "Error copying " + source.getAbsolutePath() + " to " + destination.getAbsolutePath(), e);
    }
  }

  private void copyDirectory(File dirSource, File dirDestination) {
    try {
      Path srcPath = dirSource.toPath();
      Path destPath = dirDestination.toPath();

      if (Files.exists(destPath)) {
        throw new ConfigBackupException("Destination already exists: " + destPath);
      }

      Files.walkFileTree(
          srcPath,
          new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
              Path targetDir = destPath.resolve(srcPath.relativize(dir));
              Files.createDirectory(targetDir); // Exception, if already exists
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
              Path targetFile = destPath.resolve(srcPath.relativize(file));
              Files.copy(file, targetFile); // Exception, if already exists
              return FileVisitResult.CONTINUE;
            }
          });
    } catch (IOException e) {
      throw new ConfigBackupException(
          "Error copying "
              + dirSource.getAbsolutePath()
              + " to "
              + dirDestination.getAbsolutePath(),
          e);
    }
  }
}
