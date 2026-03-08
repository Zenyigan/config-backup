# Backup Library for Java Applications

A lightweight Java library for creating backups of application configuration state at startup.
It supports both file- and directory-based sources, such as databases or JSON configuration files,
and manages automatic cleanup of outdated backups.

## Requirements

The library has no dependencies and can be used with JDK 8 or later.

## Execution

The backup process runs automatically at application startup.
It consists of two phases:

- Update all defined sources
- Delete outdated backups that are no longer needed

Failures during the process are thrown as exceptions and should be caught and logged by the application.
If deletion fails in phase 2, application startups may create more backups than configured.
Any failure will stop the backup process immediately.

## Retention Policy

Backup retention can be configured by specifying:

- the minimum number of backup items to retain (≥ 1)
- the minimum number of days to retain backups (≥ 0)

Defaults (if not configured):

- retain at least 10 backup items
- retain backups for at least the last 7 days

## Usage

### Minimal configuration (not recommended)

```java
import com.github.zenyigan.configbackup.ConfigBackup;
import java.io.File;

public class JavaPlainMinimalExample {
    public static void main(String[] args) {
        ConfigBackup.source(new File(".../h2.db")).execute();

        // startup code of the app
    }
}
```

This setup works for very simple use cases but is not recommended:

- The backup directory defaults to `.configbackup` in the user’s home directory.
- The backup item name defaults to `default-0`.
- A backup failure will terminate the application.
This may be acceptable in some cases, but the error is not logged automatically.

A backup created with this configuration will be stored at:

    <user-home>/.configbackup/default-0/<timestamp e.g. 20250901-191819-057>/h2.db

If multiple applications use this library with minimal configuration, all first configured sources will be stored under: 

    <user-home-dir>/.configbackup/default-0/

### Typical configuration

```java
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
```

This example sets a specific backup directory for the app, so that backups of different apps do not overlap.
Two files are included in the backup: a database file and a JSON configuration.
In the catch block, only the exception is printed here.
A real application would log this or display the issue in a UI.

The sources for the backup in this example are files, but they could just as well be directories.
In that case, the directory with all its files (recursively) would be backed up.

When defining the sources for the backup, names are specified ("database", "json-config"),
which ensures that the first source is stored under "database" instead of "default-0".
This makes the generated backup structure easier to understand.

Thus, after two backup runs, a structure like the following is created in .myApp/backups, for example:

```
|____backups
| |____database
| | |____20250901-185946-362
| | | |____h2.db
| | |____20250928-123958-912
| | | |____h2.db
| |____json-config
| | |____20250901-185946-362
| | | |____config.json
| | |____20250928-123958-912
| | | |____config.json
```
