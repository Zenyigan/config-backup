package com.github.zenyigan.configbackup.example;

import com.github.zenyigan.configbackup.ConfigBackup;
import java.io.File;
import java.nio.file.Paths;

public class JavaPlainExample {
  public static void main(String[] args) {
    // choose application specific backup dir and files/directories to back up:
    File backupDir = Paths.get(System.getProperty("user.home"), ".myApp", "backups").toFile();
    File database = Paths.get(System.getProperty("user.home"), ".myApp", "h2.db").toFile();
    File appConfig = Paths.get(System.getProperty("user.home"), ".myApp", "config.json").toFile();

    try {
      ConfigBackup.builder()
          .backupDestinationDirectory(backupDir)
          .source("database", database)
          .source("json-config", appConfig)
          .build()
          .execute();
    } catch (Exception e) {
      // Handle and log backup failure
      e.printStackTrace();
    }

    // startup code of the app
  }
}
