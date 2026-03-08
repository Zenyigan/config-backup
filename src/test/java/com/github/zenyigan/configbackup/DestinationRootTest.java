package com.github.zenyigan.configbackup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DestinationRootTest {
  private String oldUserHome;

  @BeforeEach
  void setUp() {
    oldUserHome = System.getProperty("user.home");
  }

  @AfterEach
  void tearDown() {
    // Property zurücksetzen
    if (oldUserHome != null) {
      System.setProperty("user.home", oldUserHome);
    } else {
      System.clearProperty("user.home");
    }
  }

  @Test
  void emptyUserHomeIsDetected() {
    System.clearProperty("user.home");
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> ConfigBackup.source(new File("")))
        .withMessageContaining("Property user.home is not set");
  }

  @Test
  void nonExistingUserHomeIsDetected() {
    String userHome = "user-home";
    System.setProperty("user.home", userHome);
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> ConfigBackup.source(new File("")))
        .withMessageContaining("Directory for user.home does not exist")
        .withMessageContaining(userHome);
  }

  @Test
  void existingUserHomeSetsDefaultDestination() {
    TestFileUtil.withTempDir(
        dir -> {
          String userHome = dir.getAbsolutePath();
          System.setProperty("user.home", userHome);
          assertThat(ConfigBackup.source(new File("")).destinationRootDir())
              .isEqualTo(new File(dir, ".configbackup"));
        });
  }

  @Test
  void configuredDestinationIsConfigured() {
    TestFileUtil.withTempDir(
        dir -> {
          System.setProperty("user.home", "notSet");
          assertThat(
                  ConfigBackup.builder()
                      .backupDestinationDirectory(dir)
                      .source(new File(""))
                      .build()
                      .destinationRootDir())
              .isEqualTo(dir);
        });
  }
}
