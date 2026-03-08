package com.github.zenyigan.configbackup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class ConfigBackupBuilder {
  private final InternalUtil internalUtil = new InternalUtilImpl();

  private final List<ConfigSourceDefinition> definitions = new ArrayList<>();
  private File destination;
  private int minimalBackupItemsToKeep = 10;
  private int minimalBackupDaysToKeep = 7;

  public ConfigBackupBuilder source(File source) {
    return source("default-" + definitions.size(), source);
  }

  public ConfigBackupBuilder source(ConfigSourceDefinition definition) {
    if (definition == null) {
      throw new IllegalArgumentException("definition must not be null");
    }
    definitions.add(definition);
    return this;
  }

  public ConfigBackupBuilder source(String name, File source) {
    definitions.add(new ConfigSourceDefinition(name, source));
    return this;
  }

  public ConfigBackupBuilder backupDestinationDirectory(File destinationDir) {
    if (destinationDir == null) {
      throw new IllegalArgumentException("destinationDir must not be null");
    }
    destination = destinationDir;
    return this;
  }

  public ConfigBackupBuilder minimalBackupItemsToKeep(int minimalBackupItemsToKeep) {
    if (minimalBackupItemsToKeep < 1) {
      throw new IllegalArgumentException("minimalBackupItemsToKeep must at least be 1");
    }
    this.minimalBackupItemsToKeep = minimalBackupItemsToKeep;
    return this;
  }

  public ConfigBackupBuilder minimalBackupDaysToKeep(int minimalBackupDaysToKeep) {
    if (minimalBackupDaysToKeep < 0) {
      throw new IllegalArgumentException("minimalBackupDaysToKeep must at least be 0");
    }
    this.minimalBackupDaysToKeep = minimalBackupDaysToKeep;
    return this;
  }

  public ConfigBackup build() {
    File finalDestination =
        (destination == null) ? internalUtil.defaultDestinationRoot() : destination;
    return new ConfigBackup(
        new ArrayList<>(definitions),
        finalDestination,
        minimalBackupItemsToKeep,
        minimalBackupDaysToKeep);
  }
}
