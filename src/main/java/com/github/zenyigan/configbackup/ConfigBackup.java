package com.github.zenyigan.configbackup;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public final class ConfigBackup {
  private final List<ConfigSourceDefinition> sources;
  private final File destinationRootDir;
  private final int minimalBackupItemsToKeep;
  private final int minimalBackupDaysToKeep;
  private final InternalUtil internalUtil = new InternalUtilImpl();

  public static ConfigBackup source(File source) {
    return new ConfigBackupBuilder().source(source).build();
  }

  public static ConfigBackupBuilder builder() {
    return new ConfigBackupBuilder();
  }

  ConfigBackup(
      List<ConfigSourceDefinition> sources,
      File destination,
      int minimalBackupItemsToKeep,
      int minimalBackupDaysToKeep) {
    if (sources == null) {
      throw new IllegalArgumentException("sources must not be null");
    }
    this.sources = Collections.unmodifiableList(sources);
    if (this.sources.isEmpty()) {
      throw new ConfigBackupException("At least one source definition is required");
    }

    Set<String> names = new HashSet<>();
    for (ConfigSourceDefinition def : this.sources) {
      if (!names.add(def.definitionName())) {
        throw new ConfigBackupException("Duplicate definitionName: " + def.definitionName());
      }
    }

    if (destination == null) {
      throw new ConfigBackupException("A destination directory for backups must be provided");
    } else {
      this.destinationRootDir = destination;
    }

    if (minimalBackupItemsToKeep < 1) {
      throw new IllegalArgumentException("minimalBackupItemsToKeep must at least be 1");
    }
    this.minimalBackupItemsToKeep = minimalBackupItemsToKeep;

    if (minimalBackupDaysToKeep < 0) {
      throw new IllegalArgumentException("minimalBackupDaysToKeep must at least be 0");
    }
    this.minimalBackupDaysToKeep = minimalBackupDaysToKeep;
  }

  public void execute() {
    File rd = destinationRootDir();
    if (!rd.exists()) {
      if (!destinationRootDir().mkdirs()) {
        throw new ConfigBackupException(
            "destinationRootDir " + destinationRootDir() + " for backup cannot be created");
      }
    }
    if (!rd.isDirectory()) {
      throw new ConfigBackupException(
          "destinationRootDir "
              + destinationRootDir()
              + " for backup exists but is not a directory");
    }

    backupPhase();
    cleanupPhase();
  }

  private void backupPhase() {
    String tsFilename = internalUtil.nowUtcName();
    for (ConfigSourceDefinition source : sources()) {
      backupSource(tsFilename, source);
    }
  }

  private void cleanupPhase() {
    for (ConfigSourceDefinition source : sources()) {
      cleanupSource(source);
    }
  }

  private void cleanupSource(ConfigSourceDefinition source) {
    File backupRootDir = new File(destinationRootDir(), source.definitionName());
    if (!backupRootDir.exists() || !backupRootDir.isDirectory()) {
      throw new ConfigBackupException(
          "Failed to cleanup "
              + source
              + ": Backup root directory "
              + backupRootDir
              + " does not exist or is no directory");
    }

    List<File> backupFiles = loadBackupFilesSortedDescending(source, backupRootDir);

    int startIndexToClear = findStartIndexToClear(backupFiles);
    deleteOldBackups(backupFiles, startIndexToClear, source);
  }

  private List<File> loadBackupFilesSortedDescending(
      ConfigSourceDefinition source, File backupRootDir) {
    File[] filesArray = backupRootDir.listFiles();
    if (filesArray == null) {
      throw new ConfigBackupException(
          "Failed to cleanup " + source + ": IO error reading directory");
    }
    List<File> backupFiles = new ArrayList<>(Arrays.asList(filesArray));
    backupFiles.sort(
        (f1, f2) -> {
          try {
            Instant i1 = internalUtil.parseUtcName(f1.getName());
            Instant i2 = internalUtil.parseUtcName(f2.getName());
            return i2.compareTo(i1);
          } catch (ConfigBackupException e) {
            throw new ConfigBackupException(
                "Failed to cleanup "
                    + source
                    + ": Unexpected filenames. Comparing file "
                    + f1.getName()
                    + " with "
                    + f2.getName(),
                e);
          }
        });
    return backupFiles;
  }

  private int findStartIndexToClear(List<File> backupFiles) {
    int startIndexToClear = minimalBackupItemsToKeep();
    Instant cutoff = Instant.now().minus(Duration.ofDays(minimalBackupDaysToKeep()));
    while (startIndexToClear < backupFiles.size()) {
      Instant fileInstant = internalUtil.parseUtcName(backupFiles.get(startIndexToClear).getName());
      if (!fileInstant.isAfter(cutoff)) {
        break;
      }
      startIndexToClear++;
    }
    return startIndexToClear;
  }

  private void deleteOldBackups(
      List<File> backupFiles, int startIndexToClear, ConfigSourceDefinition source) {
    for (int i = startIndexToClear; i < backupFiles.size(); i++) {
      try {
        internalUtil.delete(backupFiles.get(i));
      } catch (ConfigBackupException e) {
        throw new ConfigBackupException("Failed to cleanup " + source, e);
      }
    }
  }

  private void backupSource(String tsFilename, ConfigSourceDefinition source) {
    File backupDirectory =
        new File(new File(destinationRootDir(), source.definitionName()), tsFilename);
    if (backupDirectory.exists()) {
      throw new ConfigBackupException(
          "Failed to backup "
              + source
              + ": Destination directory "
              + backupDirectory
              + " already exists");
    } else {
      if (!backupDirectory.mkdirs()) {
        throw new ConfigBackupException(
            "Failed to backup "
                + source
                + ": Destination directory "
                + backupDirectory
                + " cannot be created");
      } else {
        if (source.source().exists()) {
          File destFile = new File(backupDirectory, source.source().getName());
          try {
            internalUtil.copy(source.source(), destFile);
          } catch (Exception e) {
            throw new ConfigBackupException(
                "Failed to backup " + source + " to " + destFile + " during file operations", e);
          }
        }
      }
    }
  }

  public File destinationRootDir() {
    return destinationRootDir;
  }

  public int minimalBackupItemsToKeep() {
    return minimalBackupItemsToKeep;
  }

  public int minimalBackupDaysToKeep() {
    return minimalBackupDaysToKeep;
  }

  public List<ConfigSourceDefinition> sources() {
    return sources;
  }
}
